package org.robotframework.ide.core.testData.text.contexts;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.AContextMatcher;
import org.robotframework.ide.core.testData.text.RobotTokenContext;
import org.robotframework.ide.core.testData.text.TxtRobotFileLexer.TokenizatorOutput;


public class PipeLineSeparatorSearcher extends AContextMatcher {

    public PipeLineSeparatorSearcher(TokenizatorOutput tokenProvider) {
        super(tokenProvider);
    }


    @Override
    protected List<RobotTokenContext> findContexts(
            TokenizatorOutput tokenProvider) throws Exception {
        List<RobotTokenContext> contexts = new LinkedList<>();
        LineIterator iter = new LineIterator(tokenProvider);
        while(iter.hasNext()) {
            System.out.println(iter.next());
        }

        return contexts;
    }

    private final class LineIterator {

        private final TokenizatorOutput tokenProvider;
        private final List<Integer> startLineTokensPosition;
        private final int tokenNumbers;
        private Line next;


        public LineIterator(final TokenizatorOutput tokenProvider) {
            this.tokenProvider = tokenProvider;
            this.startLineTokensPosition = tokenProvider
                    .getStartLineTokensPosition();
            this.tokenNumbers = tokenProvider.getTokens().size();
            this.next = check(new Line(-1, -1, 0));
        }


        public boolean hasNext() {
            return (this.next != null);
        }


        public Line next() {
            Line toReturn = this.next;
            this.next = check(this.next);

            return toReturn;
        }

        private final class Line {

            private final int begin;
            private final int end;
            private final int lineNumber;


            public Line(final int begin, final int end, final int lineNumber) {
                this.begin = begin;
                this.end = end;
                this.lineNumber = lineNumber;
            }


            @Override
            public String toString() {
                return String.format("Line [begin=%s, end=%s, lineNumber=%s]",
                        begin, end, lineNumber);
            }
        }


        private Line check(Line line) {
            Line nextLine = null;
            if (line != null) {
                if (line.begin == -1 && line.end == -1 && line.lineNumber == 0) {
                    if (startLineTokensPosition.isEmpty()
                            || startLineTokensPosition.size() == 1) {
                        nextLine = new Line(0, tokenNumbers, 1);
                    } else {
                        nextLine = new Line(startLineTokensPosition.get(0),
                                startLineTokensPosition.get(1), 1);
                    }
                } else {
                    if (line.end >= tokenNumbers) {
                        nextLine = null;
                    } else {
                        int beginToken = line.end;
                        int endToken = -1;
                        if (line.lineNumber + 1 < startLineTokensPosition
                                .size()) {
                            endToken = startLineTokensPosition
                                    .get(line.lineNumber + 1);
                        } else {
                            endToken = tokenNumbers;
                        }

                        nextLine = new Line(beginToken, endToken,
                                line.lineNumber + 1);
                    }
                }
            }

            return nextLine;
        }
    }
}
