/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import java.util.List;
import java.util.Optional;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

/**
 * @author wypych
 */
public class NotModelRelatedHashCommentedLineDumper {

    private final DumperHelper generalHelper;

    public NotModelRelatedHashCommentedLineDumper(final DumperHelper generalHelper) {
        this.generalHelper = generalHelper;
    }

    public <T> void dumpHashCommentsIfTheyExists(final AModelElement<T> previousElementDumped,
            final AModelElement<?> nextElementToBeDumped, final RobotFile model, final List<RobotLine> lines) {
        if (nextElementToBeDumped == null || previousElementDumped.getEndPosition().getLine()
                + 1 != nextElementToBeDumped.getBeginPosition().getLine()) {
            if (previousElementDumped.getBeginPosition().isNotSet()) {
                return;
            }
            final Optional<RobotToken> maxPosition = tokenWithMaxLineNumber(previousElementDumped);
            if (maxPosition.isPresent()) {
                final RobotToken lastTokenInLines;
                if (nextElementToBeDumped != null && !nextElementToBeDumped.getElementTokens().isEmpty()) {
                    lastTokenInLines = nextElementToBeDumped.getElementTokens()
                            .get(nextElementToBeDumped.getElementTokens().size() - 1);
                } else {
                    lastTokenInLines = maxPosition.get();
                }
                final int startLine = maxPosition.get().getLineNumber();
                final List<RobotLine> oldContent = model.getFileContent();
                final int lastLineToDump = findLastHashLine(oldContent, startLine, lastTokenInLines);
                if (lastLineToDump > -1) {
                    dumpCommentHashes(model, lines, startLine, oldContent, lastLineToDump, lastTokenInLines);
                }
            }
        }
    }

    private void dumpCommentHashes(final RobotFile model, final List<RobotLine> lines, final int startLine,
            final List<RobotLine> oldContent, final int lastLineToDump, final RobotToken lastTokenInLines) {
        for (int lineIndex = startLine; lineIndex < lastLineToDump; lineIndex++) {
            final RobotLine robotLine = oldContent.get(lineIndex);
            dumpHashLine(model, lines, robotLine);
        }
    }

    private void dumpHashLine(final RobotFile model, final List<RobotLine> lines, final RobotLine robotLine) {
        for (final IRobotLineElement e : robotLine.getLineElements()) {
            generalHelper.getDumpLineUpdater().updateLine(model, lines, e);
        }

        if (!robotLine.getEndOfLine().getFilePosition().isNotSet()) {
            generalHelper.getDumpLineUpdater().updateLine(model, lines, robotLine.getEndOfLine());
        }
    }

    private <T> Optional<RobotToken> tokenWithMaxLineNumber(final AModelElement<T> elem) {
        Optional<RobotToken> max = Optional.empty();
        for (final RobotToken t : elem.getElementTokens()) {
            if (max.isPresent()) {
                if (max.get().getFilePosition().getLine() < t.getLineNumber()) {
                    max = Optional.of(t);
                }
            } else {
                max = Optional.of(t);
            }
        }

        return max;
    }

    private int findLastHashLine(final List<RobotLine> lines, final int startLine, final RobotToken lastTokenInLines) {
        int lastHash = -1;

        final int lineSize = lines.size();
        for (int lineIndex = startLine; lineIndex < lineSize; lineIndex++) {
            final RobotLine line = lines.get(lineIndex);
            for (final IRobotLineElement rle : line.getLineElements()) {
                if (rle instanceof RobotToken) {
                    if ((rle.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                            || rle.getTypes().contains(RobotTokenType.COMMENT_CONTINUE)
                            || (rle.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE)
                                    && line.getLineElements().size() == 1))
                            && rle != lastTokenInLines) {
                        lastHash = line.getLineNumber();
                    } else {
                        return lastHash;
                    }
                }
            }
        }

        return lastHash;
    }
}
