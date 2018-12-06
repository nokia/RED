/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.rf.ide.core.testdata.text.write.DumpLineUpdater;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.SectionBuilder.Section;
import org.rf.ide.core.testdata.text.write.SectionBuilder.SectionType;

public abstract class AExecutableTableDumper<T extends ARobotSectionTable> implements ISectionTableDumper<T> {

    private final DumperHelper helper;

    private final DumpLineUpdater lineUpdater;

    private final List<IExecutableSectionElementDumper> dumpers;

    public AExecutableTableDumper(final DumperHelper helper, final List<IExecutableSectionElementDumper> dumpers) {
        this.helper = helper;
        this.lineUpdater = new DumpLineUpdater(helper);
        this.dumpers = dumpers;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void dump(final RobotFile model, final List<Section> sections, final int sectionWithHeaderPos,
            final TableHeader<T> th, final List<? extends AModelElement<T>> sorted, final List<RobotLine> lines) {
        helper.getHeaderDumpHelper().dumpHeader(model, th, lines);

        helper.getHashCommentDumper().dumpHashCommentsIfTheyExists(th, null, model, lines);

        if (!sorted.isEmpty()) {
            final List<Section> filteredSections = SectionType.filterByType(sections, sectionWithHeaderPos,
                    getSectionType());
            final int lastIndexToDump = helper.getLastSortedToDump(model, filteredSections, new ArrayList<>(sorted));

            AModelElement<?> last = null;
            for (int currentIndex = 0; currentIndex <= lastIndexToDump; currentIndex++) {
                addLineSeparatorIfIsRequired(model, lines);

                final AModelElement<T> currentElement = sorted.get(currentIndex);
                if (currentIndex > 0) {
                    helper.getHashCommentDumper()
                            .dumpHashCommentsIfTheyExists(sorted.get(currentIndex - 1), currentElement, model, lines);
                }

                @SuppressWarnings("rawtypes")
                final IExecutableStepsHolder execHolder = (IExecutableStepsHolder) currentElement;

                final RobotToken elemDeclaration = execHolder.getHolder().getDeclaration();
                final FilePosition filePosition = elemDeclaration.getFilePosition();
                int fileOffset = -1;
                if (filePosition != null && !filePosition.isNotSet()) {
                    fileOffset = filePosition.getOffset();
                }

                final RobotLine currentLine = getLineForToken(model, fileOffset);

                addSeparatorInTheBeginning(model, lines, elemDeclaration, currentLine);

                if (!elemDeclaration.isDirty() && currentLine != null) {
                    lineUpdater.updateLine(model, lines, elemDeclaration);
                    addSuffixAfterTokenDeclaration(model, lines, elemDeclaration, currentLine);
                } else {
                    lineUpdater.updateLine(model, lines, elemDeclaration);
                }

                final List<AModelElement<? extends IExecutableStepsHolder<?>>> nestedElements = execHolder
                        .getElements();
                for (final AModelElement<? extends IExecutableStepsHolder<?>> nestedElement : nestedElements) {
                    addLineSeparatorIfIsRequiredAfterExecElement(model, lines, execHolder, nestedElement);

                    final IExecutableSectionElementDumper elemDumper = dumpers.stream()
                            .filter(dumper -> dumper.isServedType(nestedElement))
                            .findFirst()
                            .orElse(null);
                    elemDumper.dump(model, sections, sectionWithHeaderPos, th, nestedElements, nestedElement, lines);

                    last = nestedElement;
                }

                if (nestedElements.isEmpty()) {
                    last = null;
                }
                helper.getEmptyLineDumper().dumpEmptyLines(model, lines, currentElement);
            }

            if (last != null) {
                helper.getHashCommentDumper().dumpHashCommentsIfTheyExists(last, null, model, lines);
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

    private void addLineSeparatorIfIsRequiredAfterExecElement(final RobotFile model, final List<RobotLine> lines,
            final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> execHolder,
            final AModelElement<? extends IExecutableStepsHolder<?>> execElement) {
        if (!lines.isEmpty()) {
            final RobotLine lastLine = lines.get(lines.size() - 1);
            final IRobotLineElement endOfLine = lastLine.getEndOfLine();
            if ((endOfLine == null || endOfLine.getFilePosition().isNotSet()
                    || endOfLine.getTypes().contains(EndOfLineTypes.NON)
                    || endOfLine.getTypes().contains(EndOfLineTypes.EOF)) && !lastLine.getLineElements().isEmpty()) {
                final boolean shouldSeparateLine = shouldSeparateLine(execHolder, execElement);

                if (shouldSeparateLine) {
                    final IRobotLineElement lineSeparator = helper.getLineSeparator(model);
                    lineUpdater.updateLine(model, lines, lineSeparator);
                }
            }
        }
    }

    private void addSuffixAfterTokenDeclaration(final RobotFile model, final List<RobotLine> lines,
            final RobotToken elemDeclaration, final RobotLine currentLine) {
        final List<IRobotLineElement> lineElements = currentLine.getLineElements();
        final int tokenPosIndex = lineElements.indexOf(elemDeclaration);
        if (lineElements.size() - 1 > tokenPosIndex + 1) {
            for (int index = tokenPosIndex + 1; index < lineElements.size(); index++) {
                final IRobotLineElement nextElem = lineElements.get(index);
                final List<IRobotTokenType> types = nextElem.getTypes();
                if (types.contains(RobotTokenType.PRETTY_ALIGN_SPACE) || types.contains(RobotTokenType.ASSIGNMENT)) {
                    lineUpdater.updateLine(model, lines, nextElem);
                } else {
                    break;
                }
            }
        }
    }

    private void addSeparatorInTheBeginning(final RobotFile model, final List<RobotLine> lines,
            final RobotToken elemDeclaration, final RobotLine currentLine) {
        if (currentLine != null) {
            helper.getSeparatorDumpHelper().dumpSeparatorsBeforeToken(model, currentLine, elemDeclaration, lines);
        } else if (helper.isSeparatorForExecutableUnitName(
                helper.getSeparator(model, lines, elemDeclaration, elemDeclaration))) {
            if (!helper.wasSeparatorBefore(lines)) {
                final Separator sep = helper.getSeparator(model, lines, elemDeclaration, elemDeclaration);
                if (sep.getText().equals(" | ")) {
                    sep.setText("| ");
                    sep.setRaw("| ");
                }
                helper.getDumpLineUpdater().updateLine(model, lines, sep);
            }
        }
    }

    private RobotLine getLineForToken(final RobotFile model, final int fileOffset) {
        RobotLine currentLine = null;
        if (fileOffset >= 0) {
            final Optional<Integer> lineIndex = model.getRobotLineIndexBy(fileOffset);
            if (lineIndex.isPresent()) {
                currentLine = model.getFileContent().get(lineIndex.get());
            }
        }
        return currentLine;
    }

    private void addLineSeparatorIfIsRequired(final RobotFile model, final List<RobotLine> lines) {
        if (!lines.isEmpty()) {
            final RobotLine lastLine = lines.get(lines.size() - 1);
            final IRobotLineElement endOfLine = lastLine.getEndOfLine();
            if ((endOfLine == null || endOfLine.getFilePosition().isNotSet()
                    || endOfLine.getTypes().contains(EndOfLineTypes.NON)
                    || endOfLine.getTypes().contains(EndOfLineTypes.EOF)) && !lastLine.getLineElements().isEmpty()) {
                final IRobotLineElement lineSeparator = helper.getLineSeparator(model);
                lineUpdater.updateLine(model, lines, lineSeparator);
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
            final Optional<Integer> robotLineIndexBy = model.getRobotLineIndexBy(execUnitDec.getStartOffset());
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
}
