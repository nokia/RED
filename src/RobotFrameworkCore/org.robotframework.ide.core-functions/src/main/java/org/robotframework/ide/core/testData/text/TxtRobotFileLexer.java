package org.robotframework.ide.core.testData.text;

import static org.robotframework.ide.core.testData.text.NamedElementsStore.CARRITAGE_RETURN;
import static org.robotframework.ide.core.testData.text.NamedElementsStore.LINE_FEED;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.robotframework.ide.core.testData.text.context.SettingsTableHeaderMatcher;


public class TxtRobotFileLexer {

    public TokenizatorOutput lexemes(final InputStreamReader reader)
            throws IOException {
        List<StringBuilder> possibleTokens = separatePossibleTokensFromText(reader);
        TokenizatorOutput out = buildTokens(possibleTokens);
        try {
            matchContexts(out);
        } catch (InterruptedException | ExecutionException e) {
            out.getProblems().add(e);
        }

        return out;
    }


    public void matchContexts(TokenizatorOutput tokenizatedOutput)
            throws InterruptedException, ExecutionException {
        List<AContextMatcher> matchers = new LinkedList<>();
        matchers.add(new SettingsTableHeaderMatcher(tokenizatedOutput));

        List<Integer> lineNumbersInTokenStream = tokenizatedOutput
                .getStartLineTokensPosition();
        for (int lineNumber : lineNumbersInTokenStream) {
            tokenizatedOutput.getContextsPerLine()
                    .add(matchForSingleLine(matchers, tokenizatedOutput,
                            lineNumber));
        }
    }


    private List<RobotTokenContext> matchForSingleLine(
            List<AContextMatcher> matchers,
            TokenizatorOutput tokenizatedOutput, int lineNumber)
            throws InterruptedException, ExecutionException {
        List<RobotTokenContext> contextsForThisLine = new LinkedList<>();

        ExecutorService execServ = Executors.newFixedThreadPool(16);
        ExecutorCompletionService<List<RobotTokenContext>> completeService = new ExecutorCompletionService<>(
                execServ);

        List<Future<List<RobotTokenContext>>> tasks = new LinkedList<>();

        for (AContextMatcher matcher : matchers) {
            matcher.setLineToMatch(lineNumber);
            Future<List<RobotTokenContext>> task = completeService
                    .submit(matcher);
            tasks.add(task);
        }

        try {
            for (int taskId = 0; taskId < tasks.size(); taskId++) {
                List<RobotTokenContext> ctxForMatcher = completeService.take()
                        .get();
                contextsForThisLine.addAll(ctxForMatcher);
            }
        } catch (InterruptedException | ExecutionException e) {
            throw e;
        } finally {
            execServ.shutdownNow();
        }

        return contextsForThisLine;
    }


    public TokenizatorOutput buildTokens(
            List<StringBuilder> possibleTokensSplitted) {
        TokenizatorOutput out = new TokenizatorOutput();

        if (possibleTokensSplitted.size() > 0) {
            out.tokens.add(buildStartLineToken(1));
            out.linePositionInTokensList.add(0);
        }

        int currentLine = 1;
        int currentColumn = 1;
        int lastStartLineTokenPosition = 0;
        RobotTokenType lineSeparator = RobotTokenType.UNKNOWN;
        for (StringBuilder currentText : possibleTokensSplitted) {
            RobotToken recognizedToken = match(out.tokens, currentText,
                    new LinearPosition(currentLine, currentColumn),
                    lineSeparator);

            if (recognizedToken.getType() == RobotTokenType.END_OF_LINE) {
                currentLine++;
                currentColumn = 1;

                RobotToken startLine = buildStartLineToken(currentLine);
                out.tokens.add(recognizedToken);
                out.tokens.add(startLine);
                lastStartLineTokenPosition = out.tokens.size() - 1;
                out.linePositionInTokensList.add(lastStartLineTokenPosition);

                lineSeparator = RobotTokenType.UNKNOWN;
            } else if (lineSeparator == RobotTokenType.UNKNOWN) {
                out.tokens.add(recognizedToken);
                lineSeparator = matchLineSeparator(out.tokens, currentText,
                        new LinearPosition(currentLine, currentColumn),
                        lastStartLineTokenPosition);
                currentColumn = recognizedToken.getEndPos().getColumn();
            } else {
                out.tokens.add(recognizedToken);
                currentColumn = recognizedToken.getEndPos().getColumn();
            }

        }

        out.tokens.add(buildEndOfFileToken(currentLine, currentColumn));

        return out;
    }

    public class TokenizatorOutput {

        private List<RobotToken> tokens = new LinkedList<>();
        private List<Integer> linePositionInTokensList = new LinkedList<>();
        private List<List<RobotTokenContext>> contextsPerLine = new LinkedList<>();
        private List<Exception> problemCatched = new LinkedList<>();


        public List<RobotToken> getTokens() {
            return tokens;
        }


        public List<Integer> getStartLineTokensPosition() {
            return linePositionInTokensList;
        }


        public List<List<RobotTokenContext>> getContextsPerLine() {
            return contextsPerLine;
        }


        public List<Exception> getProblems() {
            return problemCatched;
        }
    }


    private RobotTokenType matchLineSeparator(List<RobotToken> tokens,
            StringBuilder currentText, LinearPosition linearPosition,
            int nearestStartLineToken) {
        RobotTokenType type = RobotTokenType.UNKNOWN;
        int pipeOrOtherSign = nearestStartLineToken + 1;
        if (pipeOrOtherSign < tokens.size()) {
            RobotToken theFirstTokenAfterStartLine = tokens
                    .get(pipeOrOtherSign);
            if (theFirstTokenAfterStartLine.getType() == RobotTokenType.PIPE) {
                if (pipeOrOtherSign + 3 == tokens.size()) {
                    RobotTokenType theSecondTokenType = tokens.get(
                            pipeOrOtherSign + 1).getType();
                    if (theSecondTokenType == RobotTokenType.SPACE
                            || theSecondTokenType == RobotTokenType.TABULATOR) {
                        type = RobotTokenType.PIPE_SEPARATOR;
                    } else {
                        type = RobotTokenType.DOUBLE_SPACE_OR_TAB_SEPARATOR;
                    }
                }
            } else {
                type = RobotTokenType.DOUBLE_SPACE_OR_TAB_SEPARATOR;
            }
        }

        if (type != RobotTokenType.UNKNOWN) {
            RobotToken startLineToken = tokens.get(nearestStartLineToken);
            RobotToken separatorForLine = new RobotToken(type);
            separatorForLine.setStartPos(startLineToken.getStartPos());
            separatorForLine.setEndPos(startLineToken.getEndPos());
            startLineToken.addNextSubToken(separatorForLine, false);
        }

        return type;
    }


    private RobotToken match(List<RobotToken> tokens,
            StringBuilder currentText, LinearPosition lp,
            RobotTokenType lineSeparator) {
        RobotToken rt = matchSpecialChars(currentText, lp);
        if (rt.getType() == RobotTokenType.UNKNOWN) {
            rt = matchSpecialWords(currentText, lp);
        }

        if (rt.getType() == RobotTokenType.UNKNOWN) {
            rt = matchLineEnd(currentText, lp);
        }

        if (rt.getType() == RobotTokenType.UNKNOWN) {
            rt = buildToken(currentText, lp, RobotTokenType.VALUE);
        }

        return rt;
    }


    private RobotToken matchSpecialWords(StringBuilder currentText,
            LinearPosition lp) {

        String text = currentText.toString().toLowerCase().intern();
        RobotTokenType type = NamedElementsStore.SPECIAL_WORDS_STORE.get(text);
        if (type == null) {
            type = RobotTokenType.UNKNOWN;
        }

        RobotToken rt = buildToken(currentText, lp, type);

        return rt;
    }


    private RobotToken buildToken(StringBuilder currentText, LinearPosition lp,
            RobotTokenType type) {
        RobotToken rt;
        rt = new RobotToken(type);
        rt.setStartPos(lp);
        LinearPosition endLp = new LinearPosition(lp.getLine(), lp.getColumn()
                + currentText.length());
        rt.setEndPos(endLp);
        rt.setText(currentText);
        return rt;
    }


    private RobotToken matchSpecialChars(StringBuilder currentText,
            LinearPosition lp) {
        RobotTokenType type = RobotTokenType.UNKNOWN;

        if (currentText.length() > 0) {
            char c = currentText.charAt(0);
            type = NamedElementsStore.SPECIAL_ROBOT_TOKENS_STORE.get(c);
            if (type == null) {
                type = RobotTokenType.UNKNOWN;
            }
        }

        RobotToken rt = buildToken(currentText, lp, type);
        return rt;
    }


    private RobotToken matchLineEnd(StringBuilder currentText, LinearPosition lp) {
        RobotToken rt = new RobotToken(RobotTokenType.UNKNOWN);

        if (currentText.length() == 1) {
            char c = currentText.charAt(0);
            if (c == CARRITAGE_RETURN || c == LINE_FEED) {
                rt = buildEndOfLineToken(lp.getLine(), lp.getColumn(),
                        currentText);
            }
        } else if (currentText.length() == 2) {
            char c = currentText.charAt(0);
            if (c == CARRITAGE_RETURN) {
                rt = buildEndOfLineToken(lp.getLine(), lp.getColumn(),
                        currentText);
            }
        }

        return rt;
    }


    private RobotToken buildEndOfLineToken(int currentLine, int currentColumn,
            StringBuilder text) {
        RobotToken rt = new RobotToken(RobotTokenType.END_OF_LINE);
        LinearPosition startLp = new LinearPosition(currentLine, currentColumn);
        rt.setStartPos(startLp);
        LinearPosition endLp = new LinearPosition(currentLine, currentColumn
                + text.length());
        rt.setEndPos(endLp);
        rt.setText(text);

        return rt;
    }


    private RobotToken buildEndOfFileToken(int currentLine, int currentColumn) {
        RobotToken rt = new RobotToken(RobotTokenType.END_OF_FILE);
        LinearPosition lp = new LinearPosition(currentLine, currentColumn);
        rt.setStartPos(lp);
        rt.setEndPos(lp);

        return rt;
    }


    private RobotToken buildStartLineToken(int currentLine) {
        RobotToken rt = new RobotToken(RobotTokenType.START_LINE);
        LinearPosition lp = new LinearPosition(currentLine, 0);
        rt.setStartPos(lp);
        rt.setEndPos(lp);

        return rt;
    }


    public List<StringBuilder> separatePossibleTokensFromText(
            InputStreamReader reader) throws IOException {
        List<StringBuilder> extractedTokens = new LinkedList<>();

        StringBuilder unfinishedText = new StringBuilder();
        int readLength = -1;
        char previousChar = 0;
        CharBuffer charBuffer = CharBuffer.allocate(4096);
        while((readLength = reader.read(charBuffer)) > 0) {
            for (int i = 0; i < readLength; i++) {
                char currentChar = charBuffer.get(i);
                unfinishedText = splitTextFromTokens(extractedTokens,
                        unfinishedText, currentChar, previousChar);
                previousChar = currentChar;
            }
        }

        if (unfinishedText.length() > 0) {
            extractedTokens.add(unfinishedText);
        }

        return extractedTokens;
    }


    private StringBuilder splitTextFromTokens(
            List<StringBuilder> extractedTokens, StringBuilder unfinishedText,
            char currentChar, char previousChar) {
        StringBuilder currentValidStream = unfinishedText;

        if (NamedElementsStore.SPECIAL_CHARS_STORE.contains(currentChar)) {
            currentValidStream = ifPreviousIsTheSameContinoue(extractedTokens,
                    unfinishedText, currentChar, previousChar);
        } else if (currentChar == CARRITAGE_RETURN) {
            currentValidStream = closePreviousCharStreamAndStoreItValue(
                    extractedTokens, unfinishedText,
                    new StringBuilder().append(CARRITAGE_RETURN));
        } else if (currentChar == LINE_FEED) {
            currentValidStream = closePreviousCharStreamAndStoreItValue(
                    extractedTokens, unfinishedText, null);
            handleCarritageReturnFollowByLineFeedCharacters(extractedTokens);
        } else {
            if (NamedElementsStore.SPECIAL_CHARS_STORE.contains(previousChar)) {
                if (unfinishedText.length() > 0) {
                    extractedTokens.add(unfinishedText);
                    currentValidStream = new StringBuilder()
                            .append(currentChar);
                } else {
                    unfinishedText.append(currentChar);
                }
            } else {
                unfinishedText.append(currentChar);
            }
        }

        return currentValidStream;
    }


    private StringBuilder ifPreviousIsTheSameContinoue(
            List<StringBuilder> extractedTokens, StringBuilder unfinishedText,
            char currentChar, char previousChar) {
        StringBuilder currentValidStream = unfinishedText;
        if (previousChar == currentChar) {
            currentValidStream.append(currentChar);
        } else {
            if (unfinishedText.length() > 0) {
                extractedTokens.add(unfinishedText);
                currentValidStream = new StringBuilder().append(currentChar);
            } else {
                currentValidStream.append(currentChar);
            }
        }

        return currentValidStream;
    }


    private StringBuilder closePreviousCharStreamAndStoreItValue(
            List<StringBuilder> extractedTokens, StringBuilder otherToken,
            StringBuilder streamClosingToken) {
        StringBuilder newStreamToken;

        if (otherToken.length() > 0) {
            extractedTokens.add(otherToken);
            newStreamToken = new StringBuilder();
        } else {
            newStreamToken = otherToken;
        }

        if (streamClosingToken != null) {
            extractedTokens.add(streamClosingToken);
        }

        return newStreamToken;
    }


    private void handleCarritageReturnFollowByLineFeedCharacters(
            List<StringBuilder> extractedTokens) {
        int numberOfTokens = extractedTokens.size();

        StringBuilder tokenToAdd = null;
        if (numberOfTokens > 0) {
            StringBuilder previousToken = extractedTokens
                    .get(numberOfTokens - 1);
            if (previousToken.length() == 1
                    && previousToken.charAt(0) == CARRITAGE_RETURN) {
                tokenToAdd = previousToken;
                extractedTokens.remove(numberOfTokens - 1);
            }
        }

        if (tokenToAdd == null) {
            tokenToAdd = new StringBuilder();
        }

        extractedTokens.add(tokenToAdd);
        tokenToAdd.append(LINE_FEED);
    }
}
