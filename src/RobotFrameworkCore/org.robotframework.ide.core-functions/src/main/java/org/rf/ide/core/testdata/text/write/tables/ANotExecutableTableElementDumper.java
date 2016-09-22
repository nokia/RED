/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.mapping.table.ElementsUtility;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.read.EndOfLineBuilder.EndOfLineTypes;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.SectionBuilder.Section;

import com.google.common.base.Optional;

public abstract class ANotExecutableTableElementDumper implements ISectionElementDumper {

    private final DumperHelper aDumpHelper;

    private final ElementsUtility anElementHelper;

    private final ModelType servedType;

    private final TableElementDumperHelper elemDumperHelper;

    public ANotExecutableTableElementDumper(final DumperHelper aDumpHelper, final ModelType servedType) {
        this.aDumpHelper = aDumpHelper;
        this.anElementHelper = new ElementsUtility();
        this.servedType = servedType;
        this.elemDumperHelper = new TableElementDumperHelper();
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
    public boolean isServedType(final AModelElement<? extends ARobotSectionTable> element) {
        return (element.getModelType() == servedType);
    }

    public abstract RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<? extends ARobotSectionTable> currentElement);

    @Override
    public void dump(final RobotFile model, final List<Section> sections, final int sectionWithHeaderPos,
            final TableHeader<? extends ARobotSectionTable> th,
            final List<AModelElement<ARobotSectionTable>> sortedSettings,
            final AModelElement<ARobotSectionTable> currentElement, final List<RobotLine> lines) {
        final RobotToken elemDeclaration = currentElement.getDeclaration();
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
            getDumperHelper().getSeparatorDumpHelper().dumpSeparatorsBeforeToken(model, currentLine, elemDeclaration,
                    lines);
        }

        if (!lines.isEmpty()) {
            if (lines.get(lines.size() - 1).getEndOfLine().getTypes().contains(EndOfLineTypes.EOF)) {
                lines.get(lines.size() - 1).setEndOfLine(null, -1, -1);
            }
        }

        if (!lines.isEmpty() && !getDumperHelper().getEmptyLineDumper().isEmptyLine(lines.get(lines.size() - 1))) {
            getDumperHelper().getDumpLineUpdater().updateLine(model, lines, getDumperHelper().getLineSeparator(model));
        }

        IRobotLineElement lastToken = elemDeclaration;
        if (!elemDeclaration.isDirty() && currentLine != null) {
            getDumperHelper().getDumpLineUpdater().updateLine(model, lines, elemDeclaration);
            final List<IRobotLineElement> lineElements = currentLine.getLineElements();
            final int tokenPosIndex = lineElements.indexOf(elemDeclaration);
            if (lineElements.size() - 1 > tokenPosIndex + 1) {
                for (int index = tokenPosIndex + 1; index < lineElements.size(); index++) {
                    final IRobotLineElement nextElem = lineElements.get(index);
                    final List<IRobotTokenType> types = nextElem.getTypes();
                    if (types.contains(RobotTokenType.PRETTY_ALIGN_SPACE)
                            || types.contains(RobotTokenType.ASSIGNMENT)) {
                        getDumperHelper().getDumpLineUpdater().updateLine(model, lines, nextElem);
                        lastToken = nextElem;
                    } else {
                        break;
                    }
                }
            }
        } else {
            Separator sep = getDumperHelper().getSeparator(model, lines, lastToken, elemDeclaration);
            if (sep.getTypes().contains(SeparatorType.PIPE)) {
                String text = sep.getText();
                text = text.substring(text.indexOf('|'));
                sep.setText(text);
                sep.setRaw(text);
                getDumperHelper().getDumpLineUpdater().updateLine(model, lines, sep);
            }
            getDumperHelper().getDumpLineUpdater().updateLine(model, lines, elemDeclaration);
            lastToken = elemDeclaration;
        }

        final RobotElementsComparatorWithPositionChangedPresave sorter = getSorter(currentElement);
        final List<RobotToken> tokens = sorter.getTokensInElement();

        Collections.sort(tokens, sorter);
        // dump as it is
        if (!lastToken.isDirty() && (lastToken.getRaw().equals(lastToken.getText()))
                && !lastToken.getFilePosition().isNotSet()
                && !getElementDumperHelper().getFirstBrokenChainPosition(tokens, true).isPresent() && !tokens.isEmpty()
                && !getElementDumperHelper().isDirtyAnyDirtyInside(tokens)) {
            boolean wasDumped = getElementDumperHelper().dumpAsItIs(getDumperHelper(), model, lastToken, tokens, lines);
            if (wasDumped) {
                return;
            }
        }

        int nrOfTokens = getElementDumperHelper().getLastIndexNotEmptyIndex(tokens) + 1;

        final List<Integer> lineEndPos = new ArrayList<>(getElementDumperHelper().getLineEndPos(model, tokens));

        // just dump now
        if (tokens.size() > 1 && lineEndPos.contains(0)) {
            if (currentLine != null) {
                getDumperHelper().getDumpLineUpdater().updateLine(model, lines, currentLine.getEndOfLine());
            }

            Separator sep = getDumperHelper().getSeparator(model, lines, lastToken, tokens.get(0));
            if (sep.getTypes().contains(SeparatorType.PIPE)) {
                getDumperHelper().getDumpLineUpdater().updateLine(model, lines, sep);
            }

            RobotToken lineContinueToken = new RobotToken();
            lineContinueToken.setRaw("...");
            lineContinueToken.setText("...");
            lineContinueToken.setType(RobotTokenType.PREVIOUS_LINE_CONTINUE);

            getDumperHelper().getDumpLineUpdater().updateLine(model, lines, lineContinueToken);
        }

        for (int tokenId = 0; tokenId < nrOfTokens; tokenId++) {
            final IRobotLineElement tokElem = tokens.get(tokenId);
            Separator sep = getDumperHelper().getSeparator(model, lines, lastToken, tokElem);
            boolean addSep = true;
            if (!lines.isEmpty()) {
                final List<IRobotLineElement> lastLineElems = lines.get(lines.size() - 1).getLineElements();
                if (lastLineElems.get(lastLineElems.size() - 1) instanceof Separator) {
                    addSep = false;
                }
            }

            if (addSep) {
                getDumperHelper().getDumpLineUpdater().updateLine(model, lines, sep);
                lastToken = sep;
            }

            if (tokElem.getText().equals("\n...")) {
                Separator sepGot = getDumperHelper().getSeparator(model, lines, lastToken, tokElem);
                if (sepGot.getTypes().contains(SeparatorType.PIPE)) {
                    String text = sepGot.getText();
                    text = text.substring(text.indexOf('|'));
                    ((RobotToken) tokElem).setText("\n" + text + "...");
                }
            }

            getDumperHelper().getDumpLineUpdater().updateLine(model, lines, tokElem);
            lastToken = tokElem;

            RobotLine currentLineTok = null;
            if (!tokElem.getFilePosition().isNotSet()) {
                currentLineTok = null;
                if (fileOffset >= 0) {
                    Optional<Integer> lineIndex = model.getRobotLineIndexBy(tokElem.getFilePosition().getOffset());
                    if (lineIndex.isPresent()) {
                        currentLineTok = model.getFileContent().get(lineIndex.get());
                    }
                }

                if (currentLineTok != null && !tokElem.isDirty()) {
                    List<IRobotLineElement> lineElements = currentLineTok.getLineElements();
                    int thisTokenPosIndex = lineElements.indexOf(tokElem);
                    if (thisTokenPosIndex >= 0) {
                        if (lineElements.size() - 1 > thisTokenPosIndex + 1) {
                            final IRobotLineElement nextElem = lineElements.get(thisTokenPosIndex + 1);
                            if (nextElem.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE)) {
                                getDumperHelper().getDumpLineUpdater().updateLine(model, lines, nextElem);
                                lastToken = nextElem;
                            }
                        }
                    }
                }
            }

            boolean dumpAfterSep = false;
            if (tokenId + 1 < nrOfTokens) {
                if (!tokElem.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                        && !tokElem.getTypes().contains(RobotTokenType.COMMENT_CONTINUE)) {
                    IRobotLineElement nextElem = tokens.get(tokenId + 1);
                    if (nextElem.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                            || nextElem.getTypes().contains(RobotTokenType.COMMENT_CONTINUE)) {
                        dumpAfterSep = true;
                    }
                }
            } else {
                dumpAfterSep = true;
            }

            if (dumpAfterSep && currentLine != null) {
                getDumperHelper().getSeparatorDumpHelper().dumpSeparatorsAfterToken(model, currentLine, lastToken,
                        lines);
            }

            // check if is not end of line
            if (lineEndPos.contains(tokenId)) {
                if (currentLine != null && (sortedSettings.size() > 1 || tokenId + 1 < nrOfTokens)) {
                    getDumperHelper().getDumpLineUpdater().updateLine(model, lines, currentLine.getEndOfLine());
                } else {
                    // new end of line
                }

                if (!tokens.isEmpty() && tokenId + 1 < nrOfTokens) {
                    Separator sepNew = getDumperHelper().getSeparator(model, lines, lastToken, tokens.get(tokenId + 1));
                    if (sepNew.getTypes().contains(SeparatorType.PIPE)) {
                        getDumperHelper().getDumpLineUpdater().updateLine(model, lines, sepNew);
                    }

                    RobotToken lineContinueToken = new RobotToken();
                    lineContinueToken.setRaw("...");
                    lineContinueToken.setText("...");
                    lineContinueToken.setType(RobotTokenType.PREVIOUS_LINE_CONTINUE);

                    getDumperHelper().getDumpLineUpdater().updateLine(model, lines, lineContinueToken);

                    // updateLine(model, lines, sepNew);
                }
            }
        }
    }

}
