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
        if (!lineElement.getTypes().contains(RobotTokenType.FOR_CONTINUE_ARTIFACTAL_TOKEN)) {
            if (lineElement.getStartOffset() >= offset
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
        }
        return false;
    }
}
