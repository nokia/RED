/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.read.EndOfLineBuilder.EndOfLineTypes;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.SectionBuilder.Section;
import org.rf.ide.core.testdata.text.write.SectionBuilder.SectionType;

public abstract class ANotExecutableTableDumper<T extends ARobotSectionTable> implements ISectionTableDumper<T> {

    private final DumperHelper helper;

    private final List<ISectionElementDumper<T>> dumpers;

    private final boolean shouldDumpHashCommentAfterHeader;

    public ANotExecutableTableDumper(final DumperHelper helper, final List<ISectionElementDumper<T>> dumpers,
            final boolean shouldDumpHashCommentAfterHeader) {
        this.helper = helper;
        this.dumpers = dumpers;
        this.shouldDumpHashCommentAfterHeader = shouldDumpHashCommentAfterHeader;
    }

    @Override
    public void dump(final RobotFile model, final List<Section> sections, final int sectionWithHeaderPos,
            final TableHeader<T> th, final List<? extends AModelElement<T>> sorted, final List<RobotLine> lines) {

        helper.getHeaderDumpHelper().dumpHeader(model, th, lines);

        if (shouldDumpHashCommentAfterHeader) {
            helper.getHashCommentDumper().dumpHashCommentsIfTheyExists(th, null, model, lines);
        }

        if (!sorted.isEmpty()) {
            final List<Section> filteredSections = SectionType.filterByType(sections, sectionWithHeaderPos,
                    getSectionType());
            final int lastIndexToDump = helper.getLastSortedToDump(model, filteredSections, new ArrayList<>(sorted));

            for (int currentIndex = 0; currentIndex <= lastIndexToDump; currentIndex++) {
                addLineSeparatorIfIsRequired(model, lines);

                final AModelElement<T> currentElement = sorted.get(currentIndex);

                final ISectionElementDumper<T> elemDumper = dumpers.stream()
                        .filter(dumper -> dumper.isServedType(currentElement))
                        .findFirst()
                        .orElse(null);
                elemDumper.dump(model, filteredSections, sectionWithHeaderPos, th, sorted, currentElement, lines);

                final AModelElement<T> nextElementToBeDumped = currentIndex < lastIndexToDump
                        ? sorted.get(currentIndex + 1)
                        : null;
                helper.getHashCommentDumper()
                        .dumpHashCommentsIfTheyExists(currentElement, nextElementToBeDumped, model, lines);
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

    protected abstract SectionType getSectionType();

    private void addLineSeparatorIfIsRequired(final RobotFile model, final List<RobotLine> lines) {
        if (!lines.isEmpty()) {
            final RobotLine lastLine = lines.get(lines.size() - 1);
            final IRobotLineElement endOfLine = lastLine.getEndOfLine();
            if ((endOfLine == null || endOfLine.getFilePosition().isNotSet()
                    || endOfLine.getTypes().contains(EndOfLineTypes.NON)
                    || endOfLine.getTypes().contains(EndOfLineTypes.EOF)) && !lastLine.getLineElements().isEmpty()) {
                final IRobotLineElement lineSeparator = helper.getLineSeparator(model);
                helper.getDumpLineUpdater().updateLine(model, lines, lineSeparator);
            }
        }
    }

}
