/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.rf.ide.core.testdata.text.read.EndOfLineBuilder.EndOfLineTypes;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

class RedTokensQueueBuilder {

    Deque<IRobotLineElement> buildQueue(final int offset, final int length, final List<RobotLine> lines,
            final int startingLine) {
        final Deque<IRobotLineElement> tokens = new ArrayDeque<>();
        for (int lineIndex = startingLine; lineIndex < lines.size(); lineIndex++) {
            final RobotLine line = lines.get(lineIndex);

            for (final IRobotLineElement lineElement : line.getLineElements()) {
                final boolean shouldStop = processElement(lineElement, tokens, offset, length);
                if (shouldStop) {
                    return tokens;
                }
            }
            if (line.getEndOfLine().getTypes().contains(EndOfLineTypes.EOF)) {
                return tokens;
            }
            final boolean shouldStop = processElement(line.getEndOfLine(), tokens, offset, length);
            if (shouldStop) {
                return tokens;
            }
        }
        return tokens;
    }

    private boolean processElement(final IRobotLineElement lineElement, final Deque<IRobotLineElement> tokens,
            final int offset, final int length) {
        if (shouldOmitElement(tokens, lineElement)) {
            return false;
        } else if (lineElement.getStartOffset() >= offset
                && lineElement.getStartOffset() + lineElement.getText().length() <= offset + length) {
            // when token is within given region
            tokens.addLast(lineElement);

        } else if (lineElement.getStartOffset() < offset
                && offset < lineElement.getStartOffset() + lineElement.getText().length()) {
            // when region begin is inside the token
            tokens.addLast(lineElement);

        } else if (lineElement.getStartOffset() < offset + length
                && lineElement.getStartOffset() + lineElement.getText().length() > offset + length) {

            // when region end is within given region
            tokens.addLast(lineElement);
        } else if (!tokens.isEmpty()) {
            // we've hit the token which is outside the region, but we haven't found the
            // first token yet
            return true;
        }
        return false;
    }

    private boolean shouldOmitElement(final Deque<IRobotLineElement> tokens, final IRobotLineElement lineElement) {
        return isEmpty(lineElement) || isArtificialForLoopElement(lineElement)
                || overlapsWithPreviousToken(tokens, lineElement);
    }

    private boolean isEmpty(final IRobotLineElement lineElement) {
        return lineElement.getText().isEmpty();
    }

    private boolean isArtificialForLoopElement(final IRobotLineElement lineElement) {
        return lineElement.getTypes().contains(RobotTokenType.FOR_CONTINUE_ARTIFICIAL_TOKEN);
    }

    private boolean overlapsWithPreviousToken(final Deque<IRobotLineElement> tokens,
            final IRobotLineElement lineElement) {
        return !tokens.isEmpty() && tokens.getLast().getStartOffset()
                + tokens.getLast().getText().length() != lineElement.getStartOffset();
    }
}
