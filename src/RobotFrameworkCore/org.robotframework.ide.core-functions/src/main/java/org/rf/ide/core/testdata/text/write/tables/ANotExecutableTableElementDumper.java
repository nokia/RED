/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.rf.ide.core.testdata.mapping.table.ElementsUtility;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.RobotLine.PositionCheck;
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

    public ANotExecutableTableElementDumper(final DumperHelper aDumpHelper, final ModelType servedType) {
        this.aDumpHelper = aDumpHelper;
        this.anElementHelper = new ElementsUtility();
        this.servedType = servedType;
    }

    protected DumperHelper getDumperHelper() {
        return this.aDumpHelper;
    }

    protected ElementsUtility getElementHelper() {
        return this.anElementHelper;
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
            getDumperHelper().dumpSeparatorsBeforeToken(model, currentLine, elemDeclaration, lines);
        }

        IRobotLineElement lastToken = elemDeclaration;
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
                        lastToken = nextElem;
                    } else {
                        break;
                    }
                }
            }
        } else {
            getDumperHelper().updateLine(model, lines, elemDeclaration);
            lastToken = elemDeclaration;
        }

        final RobotElementsComparatorWithPositionChangedPresave sorter = getSorter(currentElement);
        final List<RobotToken> tokens = sorter.getTokensInElement();

        Collections.sort(tokens, sorter);
        // dump as it is
        if (!lastToken.getFilePosition().isNotSet() && !getFirstBrokenChainPosition(tokens, true).isPresent()
                && !tokens.isEmpty()) {
            dumpAsItIs(model, lastToken, tokens, lines);
            return;
        }

        int nrOfTokens = tokens.size();

        final List<Integer> lineEndPos = new ArrayList<>(getLineEndPos(model, tokens));
        if (nrOfTokens > 0) {
            boolean wasMyLine = false;
            for (int i = 0; i < nrOfTokens; i++) {
                final RobotToken robotToken = tokens.get(i);
                final FilePosition fp = robotToken.getFilePosition();
                if (!fp.isNotSet()) {
                    if (filePosition.getLine() == fp.getLine()) {
                        wasMyLine = true;
                        break;
                    }
                }
            }

            if (!wasMyLine && currentLine != null) {
                getDumperHelper().updateLine(model, lines, currentLine.getEndOfLine());
                if (!tokens.isEmpty()) {
                    Separator sep = getDumperHelper().getSeparator(model, lines, lastToken, tokens.get(0));
                    if (sep.getTypes().contains(SeparatorType.PIPE)) {
                        getDumperHelper().updateLine(model, lines, sep);
                    }

                    RobotToken lineContinueToken = new RobotToken();
                    lineContinueToken.setRaw("...");
                    lineContinueToken.setText("...");
                    lineContinueToken.setType(RobotTokenType.PREVIOUS_LINE_CONTINUE);

                    getDumperHelper().updateLine(model, lines, lineContinueToken);

                    getDumperHelper().updateLine(model, lines, sep);
                }
            }
        }

        for (int tokenId = 0; tokenId < nrOfTokens; tokenId++) {
            final IRobotLineElement tokElem = tokens.get(tokenId);
            Separator sep = getDumperHelper().getSeparator(model, lines, lastToken, tokElem);
            getDumperHelper().updateLine(model, lines, sep);
            lastToken = sep;

            getDumperHelper().updateLine(model, lines, tokElem);
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
                                getDumperHelper().updateLine(model, lines, nextElem);
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
                getDumperHelper().dumpSeparatorsAfterToken(model, currentLine, lastToken, lines);
            }

            // check if is not end of line
            if (lineEndPos.contains(tokenId)) {
                if (currentLine != null) {
                    getDumperHelper().updateLine(model, lines, currentLine.getEndOfLine());
                } else {
                    // new end of line
                }

                if (!tokens.isEmpty() && tokenId + 1 < nrOfTokens) {
                    Separator sepNew = getDumperHelper().getSeparator(model, lines, lastToken, tokens.get(tokenId + 1));
                    if (sepNew.getTypes().contains(SeparatorType.PIPE)) {
                        getDumperHelper().updateLine(model, lines, sepNew);
                    }

                    RobotToken lineContinueToken = new RobotToken();
                    lineContinueToken.setRaw("...");
                    lineContinueToken.setText("...");
                    lineContinueToken.setType(RobotTokenType.PREVIOUS_LINE_CONTINUE);

                    getDumperHelper().updateLine(model, lines, lineContinueToken);

                    // updateLine(model, lines, sepNew);
                }
            }
        }
    }

    private void dumpAsItIs(final RobotFile model, final IRobotLineElement startToken, final List<RobotToken> tokens,
            final List<RobotLine> lines) {
        final List<IRobotLineElement> dumps = new ArrayList<>(0);
        final int tokSize = tokens.size();
        int startOffset = startToken.getFilePosition().getOffset();

        RobotLine lastLine = null;
        IRobotLineElement lastToken = startToken;
        int meatTokens = 0;
        int offset = startOffset;

        int currentSize = dumps.size();
        boolean removeUpdated = false;

        if (offset == -1) {
            offset = tokens.get(0).getFilePosition().getOffset();
        }
        while (meatTokens < tokSize) {
            lastLine = model.getFileContent().get(model.getRobotLineIndexBy(offset).get());
            List<IRobotLineElement> lastToks = lastLine.getLineElements();
            final int lastToksSize = lastToks.size();

            final int elementPositionInLine;
            if (offset != startOffset) {
                elementPositionInLine = lastLine.getElementPositionInLine(offset, PositionCheck.STARTS).get();
            } else {
                elementPositionInLine = lastLine.getElementPositionInLine(lastToken).get() + 1;
            }
            currentSize = dumps.size();
            removeUpdated = false;
            for (int i = elementPositionInLine; i < lastToksSize; i++) {
                final IRobotLineElement e = lastToks.get(i);
                lastToken = e;
                if (e instanceof Separator) {
                    dumps.add(e);
                } else {
                    RobotToken rt = (RobotToken) e;
                    if (rt == tokens.get(meatTokens)) {
                        dumps.add(rt);
                        meatTokens++;
                    } else {
                        if (rt.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE)
                                || rt.getTypes().contains(RobotTokenType.ASSIGNMENT)) {
                            dumps.add(rt);
                        } else if (isContinue(dumps, rt)) {
                            dumps.add(rt);
                        } else if (startToken == rt) {
                            continue;
                        } else {
                            removeUpdated = true;
                            break;
                        }
                    }
                }
            }

            if (removeUpdated) {
                int dumpSize = dumps.size();
                if (!dumps.isEmpty()) {
                    for (int i = 0; i < dumpSize - currentSize; i++) {
                        dumps.remove(dumps.size() - 1);
                    }
                }
                meatTokens = tokSize;
                break;
            } else {
                lastToken = lastLine.getEndOfLine();
                dumps.add(lastLine.getEndOfLine());
                IRobotLineElement end = dumps.get(dumps.size() - 1);
                offset = end.getStartOffset() + (end.getEndColumn() - end.getStartColumn());
            }
        }

        if (lastLine != null && lastToken != null && !getDumperHelper().isEndOfLine(lastToken)) {
            final List<IRobotLineElement> lineElements = lastLine.getLineElements();
            final int size = lineElements.size();
            final int tokPosInLine = lastLine.getElementPositionInLine(lastToken).get();
            currentSize = dumps.size();
            removeUpdated = false;

            for (int index = tokPosInLine + 1; index < size; index++) {
                IRobotLineElement elem = lineElements.get(index);
                if (elem instanceof Separator) {
                    dumps.add(elem);
                } else {
                    removeUpdated = true;
                }
            }

            if (removeUpdated) {
                int dumpSize = dumps.size();
                if (!dumps.isEmpty()) {
                    for (int i = 0; i < dumpSize - currentSize; i++) {
                        dumps.remove(dumps.size() - 1);
                    }
                }
            } else {
                if (lastLine.getEndOfLine() != lastToken) {
                    dumps.add(lastLine.getEndOfLine());
                }
            }
        }

        for (final IRobotLineElement rle : dumps) {
            getDumperHelper().updateLine(model, lines, rle);
        }
    }

    private Set<Integer> getLineEndPos(final RobotFile model, final List<? extends IRobotLineElement> elems) {
        final Set<Integer> lof = new TreeSet<>();

        int size = elems.size();
        lof.addAll(getLineEndPosByComment(elems));
        lof.addAll(getLineEndPosFromModel(model, elems));
        lof.add(size - 1);

        return lof;
    }

    private Set<Integer> getLineEndPosByComment(final List<? extends IRobotLineElement> elems) {
        final Set<Integer> lof = new HashSet<>();
        int size = elems.size();
        IRobotTokenType type = null;
        for (int index = 0; index < size; index++) {
            final IRobotLineElement el = elems.get(index);
            boolean isComment = el.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                    || el.getTypes().contains(RobotTokenType.COMMENT_CONTINUE);
            RobotTokenType newType = isComment ? RobotTokenType.START_HASH_COMMENT : RobotTokenType.UNKNOWN;

            if (type == null) {
                type = newType;
            } else {
                if (type != newType && !isComment) {
                    lof.add(index - 1);
                }

                type = newType;
            }
        }

        return lof;
    }

    private Set<Integer> getLineEndPosFromModel(final RobotFile model, final List<? extends IRobotLineElement> elems) {
        final Set<Integer> lof = new HashSet<>();

        int size = elems.size();
        if (size > 1) {
            IRobotLineElement prevElement = elems.get(0);
            for (int tokenId = 1; tokenId < size; tokenId++) {
                final IRobotLineElement currentElement = elems.get(tokenId);
                if (isLineContinuedInModel(model, elems, prevElement, currentElement)) {
                    lof.add(tokenId - 1);
                }
            }
        }

        return lof;
    }

    private boolean isLineContinuedInModel(final RobotFile model, final List<? extends IRobotLineElement> elems,
            final IRobotLineElement prevElement, final IRobotLineElement currentElement) {
        boolean result = false;

        int prevLineNumber = prevElement.getLineNumber();
        int curLineNumber = currentElement.getLineNumber();
        if (prevLineNumber != FilePosition.NOT_SET && curLineNumber != FilePosition.NOT_SET
                && prevLineNumber + 1 == curLineNumber) {
            final RobotLine currentLine = model.getFileContent().get(curLineNumber - 1);
            final List<IRobotLineElement> lineElements = currentLine.getLineElements();
            boolean wasLineContinue = false;
            for (final IRobotLineElement rle : lineElements) {
                if (rle instanceof RobotToken) {
                    if (rle.getTypes().size() == 1 && rle.getTypes().contains(RobotTokenType.PREVIOUS_LINE_CONTINUE)) {
                        wasLineContinue = true;
                        continue;
                    } else if (wasLineContinue) {
                        if (rle == currentElement) {
                            result = true;
                            break;
                        } else {
                            if (elems.indexOf(rle) >= 0) {
                                result = false;
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                }
            }
        }

        return result;
    }

    private Optional<Integer> getFirstBrokenChainPosition(final List<? extends IRobotLineElement> elems,
            boolean treatNewAsBrokenChain) {
        Optional<Integer> o = Optional.absent();
        int size = elems.size();
        FilePosition pos = FilePosition.createNotSet();
        for (int index = 0; index < size; index++) {
            final FilePosition current = elems.get(index).getFilePosition();
            if (!current.isNotSet()) {
                if (!pos.isNotSet()) {
                    if (pos.isBefore(current)) {
                        pos = current;
                    } else {
                        o = Optional.of(index);
                        break;
                    }
                }
            } else {
                if (treatNewAsBrokenChain) {
                    o = Optional.of(index);
                    break;
                }
            }
        }
        return o;
    }

    private boolean isContinue(final List<IRobotLineElement> dumps, final IRobotLineElement l) {
        boolean result = false;

        if (l.getTypes().contains(RobotTokenType.PREVIOUS_LINE_CONTINUE)) {
            if (dumps.isEmpty()) {
                result = true;
            } else {
                int dumpsSize = dumps.size();
                boolean sepsOnly = true;
                for (int dumpId = dumpsSize - 1; dumpId >= 0; dumpId--) {
                    final IRobotLineElement rle = dumps.get(dumpId);
                    if (rle instanceof Separator) {
                        continue;
                    } else if (getDumperHelper().isEndOfLine(rle)) {
                        break;
                    } else {
                        sepsOnly = false;
                        break;
                    }
                }

                result = sepsOnly;
            }
        }

        return result;
    }
}
