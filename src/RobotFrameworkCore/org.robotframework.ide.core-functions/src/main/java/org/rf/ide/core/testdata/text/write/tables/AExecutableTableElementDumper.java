/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.rf.ide.core.testdata.mapping.table.ElementsUtility;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.read.EndOfLineBuilder.EndOfLineTypes;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.SectionBuilder.Section;

public abstract class AExecutableTableElementDumper implements IExecutableSectionElementDumper {

    private final DumperHelper aDumpHelper;

    private final ElementsUtility anElementHelper;

    private final ModelType servedType;

    private final TableElementDumperHelper elemDumperHelper;

    private final List<IForceFixBeforeDumpTask> afterSortTasks = new ArrayList<>(0);

    public AExecutableTableElementDumper(final DumperHelper aDumpHelper, final ModelType servedType) {
        this.aDumpHelper = aDumpHelper;
        this.anElementHelper = new ElementsUtility();
        this.servedType = servedType;
        this.elemDumperHelper = new TableElementDumperHelper();
    }

    protected void addAfterSortTask(final IForceFixBeforeDumpTask fixTask) {
        this.afterSortTasks.add(fixTask);
    }

    protected DumperHelper getDumperHelper() {
        return this.aDumpHelper;
    }

    protected ElementsUtility getElementHelper() {
        return this.anElementHelper;
    }

    protected TableElementDumperHelper getElementDumperHelper() {
        return this.elemDumperHelper;
    }

    @Override
    public boolean isServedType(final AModelElement<? extends IExecutableStepsHolder<?>> element) {
        return (element.getModelType() == servedType);
    }

    public abstract RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<? extends IExecutableStepsHolder<?>> currentElement);

    @Override
    public void dump(final RobotFile model, final List<Section> sections, final int sectionWithHeaderPos,
            final TableHeader<? extends ARobotSectionTable> th,
            final List<AModelElement<? extends IExecutableStepsHolder<?>>> sortedSettings,
            final AModelElement<? extends IExecutableStepsHolder<?>> currentElement, final List<RobotLine> lines) {
        final RobotToken elemDeclaration = currentElement.getDeclaration();
        final FilePosition filePosition = elemDeclaration.getFilePosition();
        int fileOffset = -1;
        if (filePosition != null && !filePosition.isNotSet()) {
            fileOffset = filePosition.getOffset();
        }

        final RobotLine currentLine = getLineForOffset(model, fileOffset);

        if (!lines.isEmpty()) {
            if (lines.get(lines.size() - 1).getEndOfLine().getTypes().contains(EndOfLineTypes.EOF)) {
                lines.get(lines.size() - 1).setEndOfLine(null, -1, -1);
            }
        }

        IRobotLineElement lastToken = elemDeclaration;
        if (currentLine != null) {
            getDumperHelper().getSeparatorDumpHelper().dumpSeparatorsBeforeToken(model, currentLine, elemDeclaration,
                    lines);
        }

        final List<RobotToken> tokens = prepareTokens(currentElement);

        final int nrOfTokens = getElementDumperHelper().getLastIndexNotEmptyIndex(tokens) + 1;

        if (!elemDeclaration.isDirty() && currentLine != null) {
            getDumperHelper().getDumpLineUpdater().updateLine(model, lines, elemDeclaration);
            lastToken = addSuffixAfterDeclarationElement(model, lines, elemDeclaration, currentLine, lastToken);
        } else {
            final boolean shouldDumpDeclaration = checkIfShouldBeDumpDirectly(elemDeclaration, tokens);

            if (shouldDumpDeclaration) {
                final boolean wasPrettyAlign = handleEmptyAndPrettyAlignForDirectlyDump(model, lines, lastToken);

                addDoubleSeparatorInCaseOfPipeSeparations(model, lines, lastToken);

                if (!getDumperHelper().wasSeparatorBefore(lines) && !wasPrettyAlign) {
                    getDumperHelper().getDumpLineUpdater().updateLine(model, lines,
                            getDumperHelper().getSeparator(model, lines, lastToken, lastToken));
                }

                getDumperHelper().getDumpLineUpdater().updateLine(model, lines, elemDeclaration);
            }
            lastToken = elemDeclaration;
        }

        // dump as it is
        if (canBeDumpedDirectly(lastToken, tokens)) {
            final boolean wasDumped = getElementDumperHelper().dumpAsItIs(getDumperHelper(), model, lastToken, tokens,
                    lines);
            if (wasDumped) {
                return;
            }
        }

        final List<Integer> lineEndPos = new ArrayList<>(getElementDumperHelper().getLineEndPos(model, tokens));

        // just dump now
        handleLineBreak(model, lines, currentLine, lastToken, tokens, lineEndPos);

        for (int tokenId = 0; tokenId < nrOfTokens; tokenId++) {
            final IRobotLineElement tokElem = tokens.get(tokenId);
            if (tokenId == 0 && (tokElem == lastToken || lastToken.getTypes().contains(RobotTokenType.ASSIGNMENT)
                    || lastToken.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE))) {
                lastToken = tokElem;
                continue;
            }

            if (isNewLineWithPreviousLineContinueToken(model, lines, lastToken, tokElem)) {
                lastToken = tokElem;
                continue;
            }

            if (!getDumperHelper().wasSeparatorBefore(lines)) {
                final Separator sep = getDumperHelper().getSeparator(model, lines, lastToken, tokElem);
                getDumperHelper().getDumpLineUpdater().updateLine(model, lines, sep);
                lastToken = sep;
            }

            getDumperHelper().getDumpLineUpdater().updateLine(model, lines, tokElem);
            lastToken = tokElem;

            RobotLine currentLineTok = null;
            if (!tokElem.getFilePosition().isNotSet()) {
                currentLineTok = null;
                if (fileOffset >= 0) {
                    final Optional<Integer> lineIndex = model
                            .getRobotLineIndexBy(tokElem.getFilePosition().getOffset());
                    if (lineIndex.isPresent()) {
                        currentLineTok = model.getFileContent().get(lineIndex.get());
                    }
                }

                lastToken = prettyAlignForCurrentDumpedNotDeclarationToken(model, lines, lastToken, tokElem,
                        currentLineTok);
            }

            final boolean dumpAfterSep = dumpAfterSeparator(tokens, nrOfTokens, tokenId, tokElem);

            if (dumpAfterSep && currentLine != null) {
                getDumperHelper().getSeparatorDumpHelper().dumpSeparatorsAfterToken(model, currentLine, lastToken,
                        lines);
            }

            // check if is not end of line
            handleLastEndOfTheLineBreak(model, lines, currentLine, lastToken, tokens, nrOfTokens, lineEndPos, tokenId);
        }
    }

    private boolean isNewLineWithPreviousLineContinueToken(final RobotFile model, final List<RobotLine> lines,
            final IRobotLineElement lastToken, final IRobotLineElement tokElem) {

        if (tokElem.getText().equals("\n...")) {
            getDumperHelper().getDumpLineUpdater().updateLine(model, lines, getDumperHelper().getLineSeparator(model));

            final RobotToken lineContinueToken = new RobotToken();
            lineContinueToken.setText("...");
            lineContinueToken.setType(RobotTokenType.PREVIOUS_LINE_CONTINUE);

            getDumperHelper().getDumpLineUpdater().updateLine(model, lines,
                    getDumperHelper().getSeparator(model, lines, lastToken, lineContinueToken));

            getDumperHelper().getDumpLineUpdater().updateLine(model, lines, lineContinueToken);

            getDumperHelper().getDumpLineUpdater().updateLine(model, lines,
                    getDumperHelper().getSeparator(model, lines, lastToken, lineContinueToken));

            return true;
        }

        return false;
    }

    private RobotLine getLineForOffset(final RobotFile model, final int fileOffset) {
        RobotLine currentLine = null;
        if (fileOffset >= 0) {
            final Optional<Integer> lineIndex = model.getRobotLineIndexBy(fileOffset);
            if (lineIndex.isPresent()) {
                currentLine = model.getFileContent().get(lineIndex.get());
            }
        }
        return currentLine;
    }

    private void handleLastEndOfTheLineBreak(final RobotFile model, final List<RobotLine> lines,
            final RobotLine currentLine,
            final IRobotLineElement lastToken, final List<RobotToken> tokens, final int nrOfTokens,
            final List<Integer> lineEndPos,
            final int tokenId) {
        if (lineEndPos.contains(tokenId) && tokenId + 1 < nrOfTokens) {
            if (currentLine != null) {
                getDumperHelper().getDumpLineUpdater().updateLine(model, lines, currentLine.getEndOfLine());
            } else {
                // new end of line
            }

            if (!tokens.isEmpty()) {
                final Separator sepNew = getDumperHelper().getSeparator(model, lines, lastToken,
                        tokens.get(tokenId + 1));
                getDumperHelper().getDumpLineUpdater().updateLine(model, lines, sepNew);

                final RobotToken lineContinueToken = new RobotToken();
                lineContinueToken.setText("...");
                lineContinueToken.setType(RobotTokenType.PREVIOUS_LINE_CONTINUE);

                getDumperHelper().getDumpLineUpdater().updateLine(model, lines, lineContinueToken);

            }
        }
    }

    private boolean dumpAfterSeparator(final List<RobotToken> tokens, final int nrOfTokens, final int tokenId,
            final IRobotLineElement tokElem) {
        boolean dumpAfterSep = false;
        if (tokenId + 1 < nrOfTokens) {
            if (!tokElem.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                    && !tokElem.getTypes().contains(RobotTokenType.COMMENT_CONTINUE)) {
                final IRobotLineElement nextElem = tokens.get(tokenId + 1);
                if (nextElem.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                        || nextElem.getTypes().contains(RobotTokenType.COMMENT_CONTINUE)) {
                    dumpAfterSep = true;
                }
            }
        } else {
            dumpAfterSep = true;
        }
        return dumpAfterSep;
    }

    private IRobotLineElement prettyAlignForCurrentDumpedNotDeclarationToken(final RobotFile model,
            final List<RobotLine> lines, final IRobotLineElement lastToken, final IRobotLineElement tokElem,
            final RobotLine currentLineTok) {
        if (currentLineTok != null && !tokElem.isDirty()) {
            final List<IRobotLineElement> lineElements = currentLineTok.getLineElements();
            final int thisTokenPosIndex = lineElements.indexOf(tokElem);
            if (thisTokenPosIndex >= 0) {
                if (lineElements.size() - 1 > thisTokenPosIndex + 1) {
                    final IRobotLineElement nextElem = lineElements.get(thisTokenPosIndex + 1);
                    if (nextElem.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE)
                            || nextElem.getTypes().contains(RobotTokenType.ASSIGNMENT)) {
                        getDumperHelper().getDumpLineUpdater().updateLine(model, lines, nextElem);
                        return nextElem;
                    }
                }
            }
        }
        return lastToken;
    }

    private void handleLineBreak(final RobotFile model, final List<RobotLine> lines, final RobotLine currentLine,
            final IRobotLineElement lastToken, final List<RobotToken> tokens, final List<Integer> lineEndPos) {
        if (tokens.size() > 1 && lineEndPos.contains(0)) {
            if (currentLine != null) {
                getDumperHelper().getDumpLineUpdater().updateLine(model, lines, currentLine.getEndOfLine());
            }

            if (getDumperHelper().isSeparatorForExecutableUnitName(
                    getDumperHelper().getSeparator(model, lines, lastToken, lastToken))) {
                int countSeparatorsBefore = getDumperHelper().countSeparatorsBefore(lines);
                Separator beforeExecRowSep = null;
                if (countSeparatorsBefore == 0) {
                    beforeExecRowSep = getDumperHelper().getSeparator(model, lines, lastToken, lastToken);
                    if (beforeExecRowSep.getText().equals(" | ")) {
                        beforeExecRowSep.setText("| ");
                        beforeExecRowSep.setRaw("| ");
                    }
                    getDumperHelper().getDumpLineUpdater().updateLine(model, lines, beforeExecRowSep);

                    ++countSeparatorsBefore;
                }

                if (countSeparatorsBefore == 1) {
                    Separator separator;
                    if (beforeExecRowSep == null) {
                        separator = getDumperHelper().getSeparator(model, lines, lastToken, lastToken);
                    } else {
                        separator = getDumperHelper().getSeparator(model, lines, beforeExecRowSep, beforeExecRowSep);
                    }

                    getDumperHelper().getDumpLineUpdater().updateLine(model, lines, separator);
                }
            } else {
                final Separator sep = getDumperHelper().getSeparator(model, lines, lastToken, tokens.get(0));
                getDumperHelper().getDumpLineUpdater().updateLine(model, lines, sep);
            }

            final RobotToken lineContinueToken = new RobotToken();
            lineContinueToken.setText("...");
            lineContinueToken.setType(RobotTokenType.PREVIOUS_LINE_CONTINUE);

            getDumperHelper().getDumpLineUpdater().updateLine(model, lines, lineContinueToken);
        }
    }

    private boolean canBeDumpedDirectly(final IRobotLineElement lastToken, final List<RobotToken> tokens) {
        return !lastToken.getFilePosition().isNotSet()
                && !getElementDumperHelper().getFirstBrokenChainPosition(tokens, true).isPresent() && !tokens.isEmpty()
                && !getElementDumperHelper().isDirtyAnyDirtyInside(tokens);
    }

    private void addDoubleSeparatorInCaseOfPipeSeparations(final RobotFile model, final List<RobotLine> lines,
            final IRobotLineElement lastToken) {
        if (getDumperHelper()
                .isSeparatorForExecutableUnitName(getDumperHelper().getSeparator(model, lines, lastToken, lastToken))) {
            int countSeparatorsBefore = getDumperHelper().countSeparatorsBefore(lines);
            if (countSeparatorsBefore == 0) {
                final Separator beforeExecRowSep = getDumperHelper().getSeparator(model, lines, lastToken, lastToken);
                if (beforeExecRowSep.getText().equals(" | ")) {
                    beforeExecRowSep.setText("| ");
                    beforeExecRowSep.setRaw("| ");
                }
                getDumperHelper().getDumpLineUpdater().updateLine(model, lines, beforeExecRowSep);
                ++countSeparatorsBefore;
            }

            if (countSeparatorsBefore == 1) {
                getDumperHelper().getDumpLineUpdater().updateLine(model, lines,
                        getDumperHelper().getSeparator(model, lines, lastToken, lastToken));
            }
        }
    }

    private boolean handleEmptyAndPrettyAlignForDirectlyDump(final RobotFile model, final List<RobotLine> lines,
            final IRobotLineElement lastToken) {
        boolean wasPrettyAlign = false;
        if (!lines.isEmpty()) {
            final RobotLine lastLine = lines.get(lines.size() - 1);
            if (getDumperHelper().getEmptyLineDumper().isEmptyLine(lastLine)) {
                final List<IRobotLineElement> lineElements = lastLine.getLineElements();
                if (!lineElements.isEmpty()) {
                    final IRobotLineElement lastElement = lineElements.get(lineElements.size() - 1);
                    if (lastElement.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE)) {
                        wasPrettyAlign = (lastElement.getStartOffset() + lastElement.getText().length()) == lastToken
                                .getStartOffset();
                        if (!wasPrettyAlign && lastElement.getLineNumber() != lastToken.getLineNumber()) {
                            wasPrettyAlign = true;
                        }
                    }
                }
            } else {
                getDumperHelper().getDumpLineUpdater().updateLine(model, lines,
                        getDumperHelper().getLineSeparator(model));
            }
        }
        return wasPrettyAlign;
    }

    private boolean checkIfShouldBeDumpDirectly(final RobotToken elemDeclaration, final List<RobotToken> tokens) {
        boolean shouldDumpDeclaration = true;
        if (!elemDeclaration.isNotEmpty()) {
            if (tokens.size() > 1) {
                final RobotToken theNextToken = tokens.get(1);
                final List<IRobotTokenType> types = theNextToken.getTypes();
                if (types.contains(RobotTokenType.COMMENT_CONTINUE)
                        || types.contains(RobotTokenType.START_HASH_COMMENT)) {
                    shouldDumpDeclaration = false;
                }
            }
        }
        return shouldDumpDeclaration;
    }

    private List<RobotToken> prepareTokens(final AModelElement<? extends IExecutableStepsHolder<?>> currentElement) {
        final RobotElementsComparatorWithPositionChangedPresave sorter = getSorter(currentElement);
        final List<RobotToken> tokens = sorter.getTokensInElement();

        Collections.sort(tokens, sorter);
        for (final IForceFixBeforeDumpTask task : afterSortTasks) {
            task.fixBeforeDump(currentElement, tokens);
        }
        return tokens;
    }

    private IRobotLineElement addSuffixAfterDeclarationElement(final RobotFile model, final List<RobotLine> lines,
            final RobotToken elemDeclaration, final RobotLine currentLine, final IRobotLineElement lastToken) {
        IRobotLineElement lastTokenToReturn = lastToken;
        final List<IRobotLineElement> lineElements = currentLine.getLineElements();
        final int tokenPosIndex = lineElements.indexOf(elemDeclaration);
        if (lineElements.size() - 1 > tokenPosIndex + 1) {
            for (int index = tokenPosIndex + 1; index < lineElements.size(); index++) {
                final IRobotLineElement nextElem = lineElements.get(index);
                final List<IRobotTokenType> types = nextElem.getTypes();
                if (types.contains(RobotTokenType.PRETTY_ALIGN_SPACE) || types.contains(RobotTokenType.ASSIGNMENT)) {
                    getDumperHelper().getDumpLineUpdater().updateLine(model, lines, nextElem);
                    lastTokenToReturn = nextElem;
                } else {
                    break;
                }
            }
        }
        return lastTokenToReturn;
    }
}
