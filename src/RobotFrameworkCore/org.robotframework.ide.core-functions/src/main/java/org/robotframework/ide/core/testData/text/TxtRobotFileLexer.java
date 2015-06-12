package org.robotframework.ide.core.testData.text;

import static org.robotframework.ide.core.testData.text.NamedElementsStore.CARRITAGE_RETURN;
import static org.robotframework.ide.core.testData.text.NamedElementsStore.LINE_FEED;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class TxtRobotFileLexer {

    private TokenMatcher tokenMatcher;
    private ContextsMatcher contextMatcher;


    public TxtRobotFileLexer() {
        tokenMatcher = new TokenMatcher();
        contextMatcher = new ContextsMatcher();
    }


    public TokenizatorOutput performLexicalAnalysis(
            final InputStreamReader reader) {
        TokenizatorOutput out = new TokenizatorOutput();
        try {
            List<StringBuilder> possibleTokens = separatePossibleTokensFromText(reader);
            out = buildTokens(possibleTokens);
            contextMatcher.matchContexts(out);
        } catch (IOException | InterruptedException | ExecutionException e) {
            out.getProblems().add(e);
        }

        return out;
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
        for (StringBuilder currentText : possibleTokensSplitted) {
            RobotToken recognizedToken = tokenMatcher.match(out, currentText,
                    new LinearPosition(currentLine, currentColumn));
            if (recognizedToken.getType() == RobotTokenType.END_OF_LINE) {
                currentLine++;
                currentColumn = 1;
                RobotToken startLine = buildStartLineToken(currentLine);
                out.tokens.add(recognizedToken);
                out.tokens.add(startLine);
                lastStartLineTokenPosition = out.tokens.size() - 1;
                out.linePositionInTokensList.add(lastStartLineTokenPosition);
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
        private Map<RobotTokenType, List<Integer>> indexesForSpecial = new LinkedHashMap<>();
        private int firstIndexOfSpecialWord = -1;
        private List<Exception> problemCatched = new LinkedList<>();
        private Map<RobotTokenType, List<Integer>> indexesForSeparator = new LinkedHashMap<>();
        private Map<ContextType, List<RobotTokenContext>> contextsPerLine = new LinkedHashMap<>();


        public List<RobotToken> getTokens() {
            return tokens;
        }


        public List<Integer> getStartLineTokensPosition() {
            return linePositionInTokensList;
        }


        public List<Exception> getProblems() {
            return problemCatched;
        }


        public Map<RobotTokenType, List<Integer>> getIndexesOfSpecial() {
            return indexesForSpecial;
        }


        public Map<RobotTokenType, List<Integer>> getIndexesForSeparator() {
            return indexesForSeparator;
        }


        public int getFirstIndexOfSpecialWord() {
            return firstIndexOfSpecialWord;
        }


        public void addNextIndexOfSeparator(RobotToken token) {
            List<Integer> forThisSeparator = indexesForSeparator.get(token
                    .getType());
            if (forThisSeparator == null) {
                forThisSeparator = new LinkedList<>();
                indexesForSeparator.put(token.getType(), forThisSeparator);
            }

            forThisSeparator.add(tokens.size());
        }


        public void addNextIndexOfSpecial(RobotToken token) {
            if (firstIndexOfSpecialWord == -1) {
                firstIndexOfSpecialWord = tokens.size();
            }
            List<Integer> forThisSpecial = indexesForSpecial.get(token
                    .getType());
            if (forThisSpecial == null) {
                forThisSpecial = new LinkedList<>();
                indexesForSpecial.put(token.getType(), forThisSpecial);
            }

            forThisSpecial.add(tokens.size());
        }


        public Map<ContextType, List<RobotTokenContext>> getContexts() {
            return contextsPerLine;
        }
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

        ReadHelpObject readerHelper = new ReadHelpObject();
        readerHelper.unfinishedText = new StringBuilder();
        readerHelper.numberOfCharsToRead = -1;
        CharBuffer charBuffer = CharBuffer.allocate(1024 * 4);
        readerHelper.charBuffer = charBuffer;
        while((readerHelper.numberOfCharsToRead = reader.read(charBuffer)) > 0) {
            readerHelper = read(readerHelper, extractedTokens);
            charBuffer.clear();
        }

        if (readerHelper.unfinishedText.length() > 0) {
            extractedTokens.add(readerHelper.unfinishedText);
        }

        return extractedTokens;
    }


    private ReadHelpObject read(ReadHelpObject readerHelper,
            List<StringBuilder> extractedTokens) {
        int readLength = readerHelper.numberOfCharsToRead;
        CharBuffer charBuffer = readerHelper.charBuffer;
        for (int i = 0; i < readLength; i++) {
            char currentChar = charBuffer.get(i);
            readerHelper.unfinishedText = splitTextFromTokens(extractedTokens,
                    readerHelper.unfinishedText, currentChar,
                    readerHelper.previousChar);
            readerHelper.previousChar = currentChar;
        }

        return readerHelper;
    }

    private class ReadHelpObject {

        private StringBuilder unfinishedText = new StringBuilder();
        private char previousChar = 0;
        private CharBuffer charBuffer = CharBuffer.allocate(0);
        private int numberOfCharsToRead = 0;
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
