package org.robotframework.ide.core.testData.text;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class TxtRobotFileLexer {

    private static final char LINE_FEED = '\n';
    private static final char CARRITAGE_RETURN = '\r';
    private static final char PIPE = '|';
    private static final char SPACE = ' ';
    private static final char TABULATOR = '\t';
    private static final char ASTERISK_CHAR = '*';
    private static final char ESCAPE_CHAR = '\\';
    private static final char DOT_CAN_BE_CONTINOUE = '.';
    private static final char QUOTES = '\"';
    private static final char EQUALS = '=';
    private static final char SCALAR_VARIABLE_BEGIN = '$';
    private static final char LIST_VARIABLE_BEGIN = '@';
    private static final char ENVIRONMENT_VARIABLE_BEGIN = '%';
    private static final char COMMON_VARIABLE_BEGIN = '{';
    private static final char COMMON_VARIABLE_END = '}';
    private static final char COMMENT_BEGIN = '#';
    private static final char COLON_FOR_BEGIN = ':';
    private static final char ELEMENT_INDEX_POSITION_BEGIN_MARKER = '[';
    private static final char ELEMENT_INDEX_POSITION_END_MARKER = ']';
    private static final int END_OF_FILE = -1;

    private static final Map<Character, RobotTokenType> SPECIAL_ROBOT_TOKENS = new HashMap<>();
    static {
        SPECIAL_ROBOT_TOKENS.put(PIPE, RobotTokenType.PIPE);
        SPECIAL_ROBOT_TOKENS.put(SPACE, RobotTokenType.SPACE);
        SPECIAL_ROBOT_TOKENS.put(TABULATOR, RobotTokenType.TABULATOR);
        SPECIAL_ROBOT_TOKENS.put(ASTERISK_CHAR, RobotTokenType.TABLE_ASTERISK);
        SPECIAL_ROBOT_TOKENS.put(ESCAPE_CHAR, RobotTokenType.ESCAPE_ANY_CHAR);
        SPECIAL_ROBOT_TOKENS.put(DOT_CAN_BE_CONTINOUE, RobotTokenType.DOT);
        SPECIAL_ROBOT_TOKENS.put(QUOTES, RobotTokenType.QUOTES);
        SPECIAL_ROBOT_TOKENS.put(EQUALS, RobotTokenType.EQUALS);
        SPECIAL_ROBOT_TOKENS.put(SCALAR_VARIABLE_BEGIN,
                RobotTokenType.SCALAR_VARIABLE_BEGIN);
        SPECIAL_ROBOT_TOKENS.put(LIST_VARIABLE_BEGIN,
                RobotTokenType.LIST_VARIABLE_BEGIN);
        SPECIAL_ROBOT_TOKENS.put(ENVIRONMENT_VARIABLE_BEGIN,
                RobotTokenType.ENVIRONMENT_VARIABLE_BEGIN);
        SPECIAL_ROBOT_TOKENS.put(COMMON_VARIABLE_BEGIN,
                RobotTokenType.VARIABLE_BEGIN);
        SPECIAL_ROBOT_TOKENS.put(COMMON_VARIABLE_END,
                RobotTokenType.VARIABLE_END);
        SPECIAL_ROBOT_TOKENS.put(COMMENT_BEGIN, RobotTokenType.COMMENT_BEGIN);
        SPECIAL_ROBOT_TOKENS.put(COLON_FOR_BEGIN, RobotTokenType.COLON);
        SPECIAL_ROBOT_TOKENS.put(ELEMENT_INDEX_POSITION_BEGIN_MARKER,
                RobotTokenType.INDEX_BEGIN);
        SPECIAL_ROBOT_TOKENS.put(ELEMENT_INDEX_POSITION_END_MARKER,
                RobotTokenType.INDEX_END);
    }

    private static final Map<String, RobotTokenType> SPECIAL_WORDS = new HashMap<>();
    static {
        SPECIAL_WORDS.put("setting", RobotTokenType.WORD_SETTING);
        SPECIAL_WORDS.put("settings", RobotTokenType.WORD_SETTING);
        SPECIAL_WORDS.put("variable", RobotTokenType.WORD_VARIABLE);
        SPECIAL_WORDS.put("variables", RobotTokenType.WORD_VARIABLE);
        SPECIAL_WORDS.put("test", RobotTokenType.WORD_TEST);
        SPECIAL_WORDS.put("case", RobotTokenType.WORD_CASE);
        SPECIAL_WORDS.put("cases", RobotTokenType.WORD_CASE);
        SPECIAL_WORDS.put("metadata", RobotTokenType.WORD_METADATA);
        SPECIAL_WORDS.put("keyword", RobotTokenType.WORD_KEYWORD);
        SPECIAL_WORDS.put("keywords", RobotTokenType.WORD_KEYWORD);
        SPECIAL_WORDS.put("user", RobotTokenType.WORD_USER);
        SPECIAL_WORDS.put("setup", RobotTokenType.WORD_SETUP);
        SPECIAL_WORDS.put("precondition", RobotTokenType.WORD_PRECONDITION);
        SPECIAL_WORDS.put("teardown", RobotTokenType.WORD_TEARDOWN);
        SPECIAL_WORDS.put("postcondition", RobotTokenType.WORD_POSTCONDITION);
        SPECIAL_WORDS.put("library", RobotTokenType.WORD_LIBRARY);
        SPECIAL_WORDS.put("resource", RobotTokenType.WORD_RESOURCE);
        SPECIAL_WORDS.put("documentation", RobotTokenType.WORD_DOCUMENTATION);
        SPECIAL_WORDS.put("suite", RobotTokenType.WORD_SUITE);
        SPECIAL_WORDS.put("force", RobotTokenType.WORD_FORCE);
        SPECIAL_WORDS.put("default", RobotTokenType.WORD_DEFAULT);
        SPECIAL_WORDS.put("tags", RobotTokenType.WORD_TAGS);
        SPECIAL_WORDS.put("template", RobotTokenType.WORD_TEMPLATE);
        SPECIAL_WORDS.put("timeout", RobotTokenType.WORD_TIMEOUT);
        SPECIAL_WORDS.put("arguments", RobotTokenType.WORD_ARGUMENTS);
        SPECIAL_WORDS.put("return", RobotTokenType.WORD_RETURN);
        SPECIAL_WORDS.put("for", RobotTokenType.WORD_FOR);
        SPECIAL_WORDS.put("in", RobotTokenType.WORD_IN);
        SPECIAL_WORDS.put("range", RobotTokenType.WORD_RANGE);
        SPECIAL_WORDS.put("with", RobotTokenType.WORD_WITH);
        SPECIAL_WORDS.put("name", RobotTokenType.WORD_NAME);
    }

    private static final List<Character> SPECIAL_CHARS = Arrays.asList(PIPE,
            SPACE, TABULATOR, ASTERISK_CHAR, ESCAPE_CHAR, DOT_CAN_BE_CONTINOUE,
            QUOTES, EQUALS, SCALAR_VARIABLE_BEGIN, LIST_VARIABLE_BEGIN,
            ENVIRONMENT_VARIABLE_BEGIN, COMMENT_BEGIN, COMMON_VARIABLE_BEGIN,
            COMMON_VARIABLE_END, COLON_FOR_BEGIN,
            ELEMENT_INDEX_POSITION_BEGIN_MARKER,
            ELEMENT_INDEX_POSITION_END_MARKER);


    public List<RobotToken> buildTokens(
            List<StringBuilder> possibleTokensSplitted) {
        List<RobotToken> tokens = new LinkedList<>();
        List<RobotToken> tempStore = new LinkedList<>();

        if (possibleTokensSplitted.size() > 0) {
            tokens.add(buildStartLineToken(0));
        }

        int currentLine = 1;
        int currentColumn = 1;
        RobotTokenType lineSeparator = RobotTokenType.UNKNOWN;
        for (StringBuilder currentText : possibleTokensSplitted) {
            RobotToken recognizedToken = match(tokens, tempStore, currentText,
                    new LinearPosition(currentLine, currentColumn),
                    lineSeparator);

            if (recognizedToken.getType() == RobotTokenType.END_OF_LINE) {
                currentLine++;
                currentColumn = 1;

                if (!tempStore.isEmpty()) {
                    // clean-up in the end
                    // merge to word possible

                    // throw new IllegalStateException();
                }

                tokens.add(recognizedToken);
                tokens.add(buildStartLineToken(currentLine));
                lineSeparator = RobotTokenType.UNKNOWN;
            } else if (lineSeparator == RobotTokenType.UNKNOWN) {
                tempStore.add(recognizedToken);
                lineSeparator = matchLineSeparator(tokens, tempStore,
                        currentText, new LinearPosition(currentLine,
                                currentColumn));
                currentColumn = recognizedToken.getEndPos().getColumn();
            } else {
                tempStore.add(recognizedToken);
                currentColumn = recognizedToken.getEndPos().getColumn();
            }

            merge(tokens, tempStore, lineSeparator);

        }

        System.out.println(tempStore);

        tokens.add(buildEndOfFileToken(currentLine, currentColumn));

        return tokens;
    }


    private void merge(List<RobotToken> tokens, List<RobotToken> tempStore,
            RobotTokenType lineSeparator) {

    }


    private RobotToken match(List<RobotToken> tokens,
            List<RobotToken> tempStore, StringBuilder currentText,
            LinearPosition lp, RobotTokenType lineSeparator) {
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
        RobotTokenType type = SPECIAL_WORDS.get(text);
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


    private RobotTokenType matchLineSeparator(List<RobotToken> tokens,
            List<RobotToken> tempStore, StringBuilder currentText,
            LinearPosition linearPosition) {
        RobotTokenType type = RobotTokenType.UNKNOWN;
        if (!tempStore.isEmpty()) {
            RobotToken theFirstToken = tempStore.get(0);
            if (theFirstToken.getType() == RobotTokenType.PIPE) {
                if (tempStore.size() == 3) {
                    RobotTokenType theSecondTokenType = tempStore.get(1)
                            .getType();
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

        return type;
    }


    private RobotToken matchSpecialChars(StringBuilder currentText,
            LinearPosition lp) {
        RobotTokenType type = RobotTokenType.UNKNOWN;

        if (currentText.length() > 0) {
            char c = currentText.charAt(0);
            type = SPECIAL_ROBOT_TOKENS.get(c);
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
