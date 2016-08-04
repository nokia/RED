package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;

class RedTokensQueueBuilder {

    Deque<IRobotLineElement> buildQueue(final int offset, final int length, final List<RobotLine> lines,
            final int startingLine) {
        final Deque<IRobotLineElement> tokens = new ArrayDeque<>();
        for (int lineIndex = startingLine; lineIndex < lines.size(); lineIndex++) {
            for (final IRobotLineElement lineElement : lines.get(lineIndex).getLineElements()) {

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
                    return tokens;
                }
            }
        }
        return tokens;
    }
}
