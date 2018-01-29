/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.RobotLine.PositionCheck;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.rf.ide.core.testdata.text.write.DumperHelper;

public class TableElementDumperHelper {

    public boolean isDirtyAnyDirtyInside(final List<RobotToken> elems) {
        return elems.stream().anyMatch(RobotToken::isDirty);
    }

    public int getLastIndexNotEmptyIndex(final List<RobotToken> elems) {
        int index = -1;

        if (elems != null) {
            final int size = elems.size();
            for (int elementIndex = 0; elementIndex < size; elementIndex++) {
                final RobotToken currentElement = elems.get(elementIndex);
                if (currentElement.isNotEmpty()) {
                    index = elementIndex;
                }
            }
        }

        return index;
    }

    public Set<Integer> getLineEndPos(final RobotFile model, final List<? extends IRobotLineElement> elems) {
        final Set<Integer> lof = new TreeSet<>();

        final int size = elems.size();
        lof.addAll(getLineEndPosByComment(elems));
        lof.addAll(getLineEndPosFromModel(model, elems));
        lof.add(size - 1);

        return lof;
    }

    private Set<Integer> getLineEndPosByComment(final List<? extends IRobotLineElement> elems) {
        final Set<Integer> lof = new HashSet<>();
        final int size = elems.size();
        IRobotTokenType type = null;
        for (int index = 0; index < size; index++) {
            final IRobotLineElement el = elems.get(index);
            final boolean isComment = el.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                    || el.getTypes().contains(RobotTokenType.COMMENT_CONTINUE);
            final RobotTokenType newType = isComment ? RobotTokenType.START_HASH_COMMENT : RobotTokenType.UNKNOWN;

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

        final int size = elems.size();
        if (size > 1) {
            final IRobotLineElement prevElement = elems.get(0);
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

        final int prevLineNumber = prevElement.getLineNumber();
        final int curLineNumber = currentElement.getLineNumber();
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

    public Optional<Integer> getFirstBrokenChainPosition(final List<? extends IRobotLineElement> elems,
            final boolean treatNewAsBrokenChain) {
        Optional<Integer> o = Optional.empty();
        final int size = elems.size();
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

    public boolean dumpAsItIs(final DumperHelper dumpHelper, final RobotFile model, final IRobotLineElement startToken,
            final List<RobotToken> tokens, final List<RobotLine> lines) {
        final List<IRobotLineElement> dumps = new ArrayList<>(0);
        final int tokSize = tokens.size();
        final int startOffset = startToken.getFilePosition().getOffset();

        RobotLine lastLine = null;
        IRobotLineElement lastToken = startToken;
        int meatTokens = 0;
        int offset = startOffset;

        int currentSize = dumps.size();
        boolean removeUpdated = false;

        if (offset == -1) {
            offset = tokens.get(0).getFilePosition().getOffset();
        }

        if (isLastElementTheSameAsFirstInTokensToDump(lastToken, tokens)
                || isAssignmentAfterCurrentToken(tokens.get(0), lastToken)) {
            // dump token before this method
            meatTokens++;
        }

        while (meatTokens < tokSize) {
            final Optional<Integer> line = model.getRobotLineIndexBy(offset);
            if (!line.isPresent()) {
                removeUpdated = true;
                break;
            }
            lastLine = model.getFileContent().get(line.get());
            final List<IRobotLineElement> lastToks = lastLine.getLineElements();
            final int lastToksSize = lastToks.size();

            int elementPositionInLine;
            if (offset != startOffset) {
                final Optional<Integer> elemPosInLine = lastLine.getElementPositionInLine(offset, PositionCheck.STARTS);
                if (elemPosInLine.isPresent()) {
                    elementPositionInLine = elemPosInLine.get();
                } else {
                    elementPositionInLine = -1;
                }
            } else {
                final Optional<Integer> elemPosInLine = lastLine.getElementPositionInLine(lastToken);
                if (elemPosInLine.isPresent()) {
                    elementPositionInLine = elemPosInLine.get() + 1;
                } else {
                    elementPositionInLine = -1;
                }
            }
            currentSize = dumps.size();
            removeUpdated = false;
            for (int i = elementPositionInLine; elementPositionInLine > -1 && i < lastToksSize; i++) {
                final IRobotLineElement e = lastToks.get(i);
                lastToken = e;
                if (e instanceof Separator) {
                    dumps.add(e);
                } else {
                    final RobotToken rt = (RobotToken) e;
                    if (tokSize > meatTokens && rt == tokens.get(meatTokens)) {
                        dumps.add(rt);
                        meatTokens++;
                    } else {
                        if (rt.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE)
                                || rt.getTypes().contains(RobotTokenType.ASSIGNMENT)) {
                            dumps.add(rt);
                        } else if (isContinue(dumpHelper, dumps, rt)) {
                            dumps.add(rt);
                        } else if (startToken == rt) {
                            continue;
                        } else {
                            removeUpdated = true;
                            return false;
                            // break;
                        }
                    }
                }
            }

            if (removeUpdated) {
                final int dumpSize = dumps.size();
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
                final IRobotLineElement end = dumps.get(dumps.size() - 1);
                offset = end.getStartOffset() + (end.getEndColumn() - end.getStartColumn());
            }
        }

        if (lastLine != null && lastToken != null && !dumpHelper.isEndOfLine(lastToken)) {
            final List<IRobotLineElement> lineElements = lastLine.getLineElements();
            final int size = lineElements.size();
            final int tokPosInLine = lastLine.getElementPositionInLine(lastToken).get();
            currentSize = dumps.size();
            removeUpdated = false;

            for (int index = tokPosInLine + 1; index < size; index++) {
                final IRobotLineElement elem = lineElements.get(index);
                if (elem instanceof Separator) {
                    dumps.add(elem);
                } else {
                    removeUpdated = true;
                }
            }

            if (removeUpdated) {
                final int dumpSize = dumps.size();
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
            dumpHelper.getDumpLineUpdater().updateLine(model, lines, rle);
        }

        return (dumps.size() > 0);
    }

    public boolean dumpEOLAsItIs(final DumperHelper dumpHelper, final RobotFile model,
            final IRobotLineElement startToken, final List<RobotLine> lines) {
        final int offset = startToken.getFilePosition().getOffset();

        final Optional<Integer> line = model.getRobotLineIndexBy(offset);
        if (!line.isPresent()) {
            return false;
        }
        final RobotLine lastLine = model.getFileContent().get(line.get());

        final Optional<Integer> elemPosInLine = lastLine.getElementPositionInLine(startToken);
        if (!elemPosInLine.isPresent()) {
            return false;
        }

        dumpHelper.getDumpLineUpdater().updateLine(model, lines, lastLine.getEndOfLine());
        return true;
    }

    private boolean isAssignmentAfterCurrentToken(final IRobotLineElement prevToken,
            final IRobotLineElement currentToken) {
        return (!prevToken.getFilePosition().isNotSet()
                && prevToken.getFilePosition().getOffset()
                        + prevToken.getText().length() == currentToken.getFilePosition().getOffset()
                && currentToken.getTypes().contains(RobotTokenType.ASSIGNMENT));
    }

    private boolean isLastElementTheSameAsFirstInTokensToDump(final IRobotLineElement startToken,
            final List<RobotToken> tokens) {
        return (!tokens.isEmpty() && tokens.get(0) == startToken);
    }

    private boolean isContinue(final DumperHelper dumpHelper, final List<IRobotLineElement> dumps,
            final IRobotLineElement l) {
        boolean result = false;

        if (l.getTypes().contains(RobotTokenType.PREVIOUS_LINE_CONTINUE)) {
            if (dumps.isEmpty()) {
                result = true;
            } else {
                final int dumpsSize = dumps.size();
                boolean sepsOnly = true;
                for (int dumpId = dumpsSize - 1; dumpId >= 0; dumpId--) {
                    final IRobotLineElement rle = dumps.get(dumpId);
                    if (rle instanceof Separator) {
                        continue;
                    } else if (dumpHelper.isEndOfLine(rle)) {
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
