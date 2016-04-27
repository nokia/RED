/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
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

public abstract class AExecutableTableDumper implements ISectionTableDumper {

    private final DumperHelper aDumpHelper;

    private final List<IExecutableSectionElementDumper> dumpers;

    public AExecutableTableDumper(final DumperHelper aDumpHelper, final List<IExecutableSectionElementDumper> dumpers) {
        this.aDumpHelper = aDumpHelper;
        this.dumpers = dumpers;
    }

    protected DumperHelper getDumperHelper() {
        return this.aDumpHelper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void dump(final RobotFile model, final List<Section> sections, final int sectionWithHeaderPos,
            final TableHeader<? extends ARobotSectionTable> th, final List<AModelElement<ARobotSectionTable>> sorted,
            final List<RobotLine> lines) {
        getDumperHelper().dumpHeader(model, th, lines);

        getDumperHelper().dumpEmptyLines(model, lines, (AModelElement<ARobotSectionTable>) th);

        if (!sorted.isEmpty()) {
            final List<Section> execUnits = SectionType.filterByType(sections, sectionWithHeaderPos, getSectionType());
            final int lastIndexToDump = getDumperHelper().getLastSortedToDump(model, execUnits,
                    new ArrayList<AModelElement<ARobotSectionTable>>(sorted));

            for (int execUnitIndex = 0; execUnitIndex <= lastIndexToDump; execUnitIndex++) {
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

                final AModelElement<ARobotSectionTable> execUnit = sorted.get(execUnitIndex);
                @SuppressWarnings("rawtypes")
                final IExecutableStepsHolder execHolder = (IExecutableStepsHolder) execUnit;

                final RobotToken elemDeclaration = execHolder.getHolder().getDeclaration();
                final FilePosition filePosition = elemDeclaration.getFilePosition();
                int fileOffset = -1;
                if (filePosition != null && !filePosition.isNotSet()) {
                    fileOffset = filePosition.getOffset();
                }

                RobotLine currentLine = null;
                if (fileOffset >= 0) {
                    Optional<Integer> lineIndex = model.getRobotLineIndexBy(fileOffset);
                    if (lineIndex.isPresent()) {
                        currentLine = model.getFileContent().get(lineIndex.get());
                    }
                }

                if (currentLine != null) {
                    getDumperHelper().dumpSeparatorsBeforeToken(model, currentLine, elemDeclaration, lines);
                }

                if (!elemDeclaration.isDirty() && currentLine != null) {
                    getDumperHelper().updateLine(model, lines, elemDeclaration);
                    final List<IRobotLineElement> lineElements = currentLine.getLineElements();
                    final int tokenPosIndex = lineElements.indexOf(elemDeclaration);
                    if (lineElements.size() - 1 > tokenPosIndex + 1) {
                        for (int index = tokenPosIndex + 1; index < lineElements.size(); index++) {
                            final IRobotLineElement nextElem = lineElements.get(index);
                            final List<IRobotTokenType> types = nextElem.getTypes();
                            if (types.contains(RobotTokenType.PRETTY_ALIGN_SPACE)
                                    || types.contains(RobotTokenType.ASSIGNMENT)) {
                                getDumperHelper().updateLine(model, lines, nextElem);
                            } else {
                                break;
                            }
                        }
                    }
                } else {
                    getDumperHelper().updateLine(model, lines, elemDeclaration);
                }

                final List<AModelElement<? extends IExecutableStepsHolder<?>>> sortedUnits = getSortedUnits(execHolder);
                for (AModelElement<? extends IExecutableStepsHolder<?>> execElement : sortedUnits) {
                    if (!lines.isEmpty()) {
                        RobotLine lastLine = lines.get(lines.size() - 1);
                        IRobotLineElement endOfLine = lastLine.getEndOfLine();
                        if ((endOfLine == null || endOfLine.getFilePosition().isNotSet()
                                || endOfLine.getTypes().contains(EndOfLineTypes.NON)
                                || endOfLine.getTypes().contains(EndOfLineTypes.EOF))
                                && !lastLine.getLineElements().isEmpty()) {
                            boolean shouldSeparateLine = shouldSeparateLine(execHolder, execElement);

                            if (shouldSeparateLine) {
                                final IRobotLineElement lineSeparator = getDumperHelper().getLineSeparator(model);
                                getDumperHelper().updateLine(model, lines, lineSeparator);
                            }
                        }
                    }

                    IExecutableSectionElementDumper elemDumper = null;
                    for (final IExecutableSectionElementDumper dumper : dumpers) {
                        if (dumper.isServedType(execElement)) {
                            elemDumper = dumper;
                            break;
                        }
                    }

                    elemDumper.dump(model, sections, sectionWithHeaderPos, th, sortedUnits, execElement, lines);
                }

                getDumperHelper().dumpEmptyLines(model, lines, execUnit);
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

    private boolean shouldSeparateLine(
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execUnit,
            final AModelElement<? extends IExecutableStepsHolder<?>> execElement) {
        boolean shouldSeparateLine = true;

        final IRobotLineElement execUnitDec = execUnit.getHolder().getDeclaration();
        if (execUnitDec.getStartOffset() >= 0) {
            final RobotFile model = execUnit.getHolder().getParent().getParent();
            Optional<Integer> robotLineIndexBy = model.getRobotLineIndexBy(execUnitDec.getStartOffset());
            if (robotLineIndexBy.isPresent()) {
                final RobotLine lastLine = model.getFileContent().get(robotLineIndexBy.get());
                final Optional<Integer> execUnitPos = lastLine.getElementPositionInLine(execUnitDec);

                final IRobotLineElement execElemDec = execElement.getDeclaration();
                final Optional<Integer> execElemPos = lastLine.getElementPositionInLine(execElemDec);
                if (execUnitPos.isPresent() && execElemPos.isPresent()) {
                    shouldSeparateLine = false;
                }
            }
        }

        return shouldSeparateLine;
    }

    public abstract List<AModelElement<? extends IExecutableStepsHolder<?>>> getSortedUnits(
            final IExecutableStepsHolder<?> execHolder);
}
