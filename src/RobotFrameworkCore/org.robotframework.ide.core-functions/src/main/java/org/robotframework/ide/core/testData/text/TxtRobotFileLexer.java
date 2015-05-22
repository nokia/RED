package org.robotframework.ide.core.testData.text;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class TxtRobotFileLexer {

    public static final char LINE_FEED = '\n';
    public static final char CARRITAGE_RETURN = '\r';
    public static final char PIPE = '|';
    public static final char SPACE = ' ';
    public static final char TABULATOR = '\t';
    public static final char ASTERISK_CHAR = '*';
    public static final char ESCAPE_CHAR = '\\';
    public static final char DOT_CAN_BE_CONTINOUE = '.';
    public static final char QUOTES = '\"';
    public static final char EQUALS = '=';
    public static final char SCARAL_VARIABLE_BEGIN = '$';
    public static final char LIST_VARIABLE_BEGIN = '@';
    public static final char ENVIRONMENT_VARIABLE_BEGIN = '%';
    public static final char COMMON_VARIABLE_BEGIN = '{';
    public static final char COMMON_VARIABLE_END = '}';
    public static final char COMMENT_BEGIN = '#';
    public static final char COLON_FOR_BEGIN = ':';
    public static final char ELEMENT_INDEX_POSITION_BEGIN_MARKER = '[';
    public static final char ELEMENT_INDEX_POSITION_END_MARKER = ']';
    private static final int END_OF_FILE = -1;

    private static final List<Character> SPECIAL_CHARS = Arrays.asList(PIPE,
            SPACE, TABULATOR, ASTERISK_CHAR, ESCAPE_CHAR, DOT_CAN_BE_CONTINOUE,
            QUOTES, EQUALS, SCARAL_VARIABLE_BEGIN, LIST_VARIABLE_BEGIN,
            ENVIRONMENT_VARIABLE_BEGIN, COMMENT_BEGIN, COMMON_VARIABLE_BEGIN,
            COMMON_VARIABLE_END, COLON_FOR_BEGIN,
            ELEMENT_INDEX_POSITION_BEGIN_MARKER,
            ELEMENT_INDEX_POSITION_END_MARKER);


    public List<StringBuilder> separatePossibleTokensFromText(
            InputStreamReader reader) throws IOException {
        List<StringBuilder> extractedTokens = new LinkedList<>();

        StringBuilder unfinishedText = new StringBuilder();
        int currentChar = END_OF_FILE;
        int previousChar = END_OF_FILE;
        while((currentChar = reader.read()) != END_OF_FILE) {
            unfinishedText = splitTextFromTokens(extractedTokens,
                    unfinishedText, (char) currentChar, (char) previousChar);
            previousChar = currentChar;
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

        if (SPECIAL_CHARS.contains(currentChar)) {
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
            if (SPECIAL_CHARS.contains(previousChar)) {
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
            if (previousToken.capacity() == 1
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
