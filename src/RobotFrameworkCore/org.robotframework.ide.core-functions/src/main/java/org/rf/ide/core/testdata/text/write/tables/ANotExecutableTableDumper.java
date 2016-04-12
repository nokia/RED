/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.read.EndOfLineBuilder.EndOfLineTypes;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.SectionBuilder.Section;
import org.rf.ide.core.testdata.text.write.SectionBuilder.SectionType;

import com.google.common.base.Optional;

public abstract class ANotExecutableTableDumper implements ISectionTableDumper {

    private final DumperHelper aDumpHelper;

    private final List<ISectionElementDumper> dumpers;

    public ANotExecutableTableDumper(final DumperHelper aDumpHelper, final List<ISectionElementDumper> dumpers) {
        this.aDumpHelper = aDumpHelper;
        this.dumpers = dumpers;
    }

    protected DumperHelper getDumperHelper() {
        return this.aDumpHelper;
    }

    @Override
    public void dump(final RobotFile model, final List<Section> sections, final int sectionWithHeaderPos,
            final TableHeader<? extends ARobotSectionTable> th, final List<AModelElement<ARobotSectionTable>> sorted,
            final List<RobotLine> lines) {
        getDumperHelper().dumpHeader(model, th, lines);

        if (!sorted.isEmpty()) {
            final List<Section> settingSections = SectionType.filterByType(sections, sectionWithHeaderPos,
                    getSectionType());
            final int lastIndexToDump = getDumperHelper().getLastSortedToDump(model, settingSections,
                    new ArrayList<AModelElement<ARobotSectionTable>>(sorted));

            for (int settingIndex = 0; settingIndex <= lastIndexToDump; settingIndex++) {
                if (!lines.isEmpty()) {
                    RobotLine lastLine = lines.get(lines.size() - 1);
                    IRobotLineElement endOfLine = lastLine.getEndOfLine();
                    if ((endOfLine == null || endOfLine.getFilePosition().isNotSet()
                            || endOfLine.getTypes().contains(EndOfLineTypes.NON)
                            || endOfLine.getTypes().contains(EndOfLineTypes.EOF))
                            && !lastLine.getLineElements().isEmpty()) {
                        final IRobotLineElement lineSeparator = getDumperHelper().getLineSeparator(model);
                        getDumperHelper().updateLine(model, lines, lineSeparator);
                    }
                }

                final AModelElement<ARobotSectionTable> setting = sorted.get(settingIndex);

                ISectionElementDumper elemDumper = null;
                for (final ISectionElementDumper dumper : dumpers) {
                    if (dumper.isServedType(setting)) {
                        elemDumper = dumper;
                        break;
                    }
                }

                elemDumper.dump(model, settingSections, sectionWithHeaderPos, th, sorted, setting, lines);

                dumpEmptyLines(model, lines, setting);
            }

            if (lastIndexToDump == sorted.size() - 1) {
                sorted.clear();
            } else {
                for (int elemIndex = 0; elemIndex <= lastIndexToDump; elemIndex++) {
                    sorted.remove(0);
                }
            }
        }
    }

    private void dumpEmptyLines(final RobotFile model, final List<RobotLine> lines,
            final AModelElement<ARobotSectionTable> setting) {
        final FilePosition fPosEnd = setting.getEndPosition();
        if (!fPosEnd.isNotSet()) {
            if (!lines.isEmpty()) {
                RobotLine lastLine = lines.get(lines.size() - 1);
                IRobotLineElement endOfLine = lastLine.getEndOfLine();
                if ((endOfLine == null || endOfLine.getFilePosition().isNotSet()
                        || endOfLine.getTypes().contains(EndOfLineTypes.NON)
                        || endOfLine.getTypes().contains(EndOfLineTypes.EOF))
                        && !lastLine.getLineElements().isEmpty()) {
                    getDumperHelper().updateLine(model, lines, getDumperHelper().getLineSeparator(model, fPosEnd));
                }
            }

            Optional<Integer> currentLine = model.getRobotLineIndexBy(fPosEnd.getOffset());
            int currentLineNumber;
            if (currentLine.isPresent()) {
                currentLineNumber = currentLine.get();
            } else {
                currentLineNumber = fPosEnd.getLine();
            }

            final List<RobotLine> fileContent = model.getFileContent();
            while (fileContent.size() > currentLineNumber + 1) {
                final RobotLine nextLine = fileContent.get(currentLineNumber + 1);
                if (isEmptyLine(nextLine)) {
                    getDumperHelper().dumpLineDirectly(model, lines, nextLine);
                    currentLineNumber++;
                } else {
                    break;
                }
            }
        }
    }

    private boolean isEmptyLine(final RobotLine line) {
        boolean isEmpty = true;

        for (final IRobotLineElement elem : line.getLineElements()) {
            if (elem instanceof RobotToken) {
                RobotToken tok = (RobotToken) elem;
                if (!containsType(tok, RobotTokenType.PRETTY_ALIGN_SPACE)) {
                    isEmpty = false;
                    break;
                }
            }
        }

        return isEmpty;
    }

    private boolean containsType(final RobotToken token, final IRobotTokenType... types) {
        boolean contains = false;
        final List<IRobotTokenType> accepted = Arrays.asList(types);
        for (final IRobotTokenType type : token.getTypes()) {
            if (accepted.contains(type)) {
                contains = true;
            } else {
                contains = false;
                break;
            }
        }

        return contains;
    }
}
