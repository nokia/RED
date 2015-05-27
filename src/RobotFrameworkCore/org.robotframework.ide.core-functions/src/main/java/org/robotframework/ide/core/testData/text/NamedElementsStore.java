package org.robotframework.ide.core.testData.text;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NamedElementsStore {

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
    public static final char SCALAR_VARIABLE_BEGIN = '$';
    public static final char LIST_VARIABLE_BEGIN = '@';
    public static final char ENVIRONMENT_VARIABLE_BEGIN = '%';
    public static final char COMMON_VARIABLE_BEGIN = '{';
    public static final char COMMON_VARIABLE_END = '}';
    public static final char COMMENT_BEGIN = '#';
    public static final char COLON_FOR_BEGIN = ':';
    public static final char ELEMENT_INDEX_POSITION_BEGIN_MARKER = '[';
    public static final char ELEMENT_INDEX_POSITION_END_MARKER = ']';
    public static final int END_OF_FILE = -1;

    private static volatile Map<Character, RobotTokenType> SPECIAL_ROBOT_TOKENS = new HashMap<>();
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
    public volatile static Map<Character, RobotTokenType> SPECIAL_ROBOT_TOKENS_STORE = Collections
            .unmodifiableMap(SPECIAL_ROBOT_TOKENS);

    private volatile static Map<String, RobotTokenType> SPECIAL_WORDS = new HashMap<>();
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
    public volatile static Map<String, RobotTokenType> SPECIAL_WORDS_STORE = Collections
            .unmodifiableMap(SPECIAL_WORDS);

    private static volatile List<Character> SPECIAL_CHARS = Arrays.asList(PIPE,
            SPACE, TABULATOR, ASTERISK_CHAR, ESCAPE_CHAR, DOT_CAN_BE_CONTINOUE,
            QUOTES, EQUALS, SCALAR_VARIABLE_BEGIN, LIST_VARIABLE_BEGIN,
            ENVIRONMENT_VARIABLE_BEGIN, COMMENT_BEGIN, COMMON_VARIABLE_BEGIN,
            COMMON_VARIABLE_END, COLON_FOR_BEGIN,
            ELEMENT_INDEX_POSITION_BEGIN_MARKER,
            ELEMENT_INDEX_POSITION_END_MARKER);
    public volatile static List<Character> SPECIAL_CHARS_STORE = Collections
            .unmodifiableList(SPECIAL_CHARS);
}
