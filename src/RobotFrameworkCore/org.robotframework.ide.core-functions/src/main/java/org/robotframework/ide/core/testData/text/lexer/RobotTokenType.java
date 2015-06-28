package org.robotframework.ide.core.testData.text.lexer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Declares single character types of token recognized inside lexer.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotToken
 * @see TxtRobotTestDataLexer
 */
public enum RobotTokenType implements RobotType {
    /**
     * the type of token can't be recognized
     */
    UNKNOWN(HELPER.UNWRITABLE_CHARS),
    /**
     * line end - it could be: carriage return and optional line feed
     */
    END_OF_LINE(HELPER.UNWRITABLE_CHARS),
    /*
     * in hex: 0x0D
     */
    CARRIAGE_RETURN('\r'),
    /**
     * in hex: 0x0A
     */
    LINE_FEED('\n'),
    /**
     * in hex: 0x09
     */
    SINGLE_TABULATOR('\t'),
    /**
     * in hex: 0x20
     */
    SINGLE_SPACE(' '),
    /**
     * PIPE - '|'
     */
    SINGLE_PIPE('|'),
    /**
     * '*'
     */
    SINGLE_ASTERISK('*'),
    /**
     * '#'
     */
    SINGLE_COMMENT_HASH('#'),
    /**
     * '\'
     */
    SINGLE_ESCAPE_BACKSLASH('\\'),
    /**
     * '$'
     */
    SINGLE_SCALAR_BEGIN_DOLLAR('$'),
    /**
     * '@'
     */
    SINGLE_LIST_BEGIN_AT('@'),
    /**
     * '%'
     */
    SINGLE_ENVIRONMENT_BEGIN_PROCENT('%'),
    /**
     * '&'
     */
    SINGLE_DICTIONARY_BEGIN_AMPERSAND('&'),
    /**
     * '='
     */
    SINGLE_EQUAL('='),
    /*
     * '{'
     */
    SINGLE_VARIABLE_BEGIN_CURLY_BRACKET('{'),
    /**
     * '}'
     */
    SINGLE_VARIABLE_END_CURLY_BRACKET('}'),
    /**
     * '['
     */
    SINGLE_POSSITION_INDEX_BEGIN_SQUARE_BRACKET('['),
    /**
     * ']'
     */
    SINGLE_POSSITION_INDEX_END_SQUARE_BRACKET(']'),
    /**
     * ':'
     */
    SINGLE_COLON(':'),
    /**
     * '.'
     */
    SINGLE_DOT('.'),
    /**
     * '"'
     */
    SINGLE_QUOTE_MARK('"');

    private final char thisTokenChar;
    private final String toWriteText;
    private static final Map<Character, RobotTokenType> writeableTypes;

    static {
        Map<Character, RobotTokenType> temp = new HashMap<>();
        RobotTokenType[] values = RobotTokenType.values();
        for (RobotTokenType type : values) {
            if (type.thisTokenChar != HELPER.UNWRITABLE_CHARS) {
                temp.put(type.thisTokenChar, type);
            }
        }

        writeableTypes = Collections.unmodifiableMap(temp);
    }


    private RobotTokenType(final char thisTokenChar) {
        this.thisTokenChar = thisTokenChar;
        if (this.thisTokenChar == HELPER.UNWRITABLE_CHARS) {
            this.toWriteText = "";
        } else {
            this.toWriteText = "" + this.thisTokenChar;
        }
    }


    public static RobotTokenType getToken(char c) {
        RobotTokenType type = writeableTypes.get(c);

        if (type == null) {
            type = RobotTokenType.UNKNOWN;
        }

        return type;
    }


    public char getThisTokenChar() {
        return this.thisTokenChar;
    }


    @Override
    public String toWrite() {
        return this.toWriteText;
    }

    static interface HELPER {

        public static final char UNWRITABLE_CHARS = '\u0000';
    }


    @Override
    public boolean isWriteable() {
        return (HELPER.UNWRITABLE_CHARS != thisTokenChar);
    }


    @Override
    public RobotType getTokenType(StringBuilder text) {
        RobotType type = RobotTokenType.UNKNOWN;
        if (text != null) {
            type = getTokenType(text.toString());
        }

        return type;
    }


    @Override
    public RobotType getTokenType(String text) {
        RobotType type = RobotTokenType.UNKNOWN;

        if (text != null && text.length() == 1) {
            type = getToken(text.charAt(0));
        }

        return type;
    }
}
