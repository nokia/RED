package org.robotframework.ide.core.testData.text.context;

/**
 * Gives types, which are not multiple lines - just one line.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 */
public enum SimpleRobotContextType implements IContextElementType {
    /**
     * means that this line can't be match to any context
     */
    UNDECLARED_COMMENT,
    /**
     * line contains hash sign or comment word
     */
    DECLARED_COMMENT,
    /**
     * <pre>
     * *** Settings ***
     * </pre>
     * 
     * setting table declaration
     */
    SETTING_TABLE_HEADER,
    /**
     * <pre>
     * *** Variables ***
     * </pre>
     * 
     * variable table declaration
     */
    VARIABLE_TABLE_HEADER,
    /**
     * <pre>
     * *** Test Case ***
     * </pre>
     * 
     * variable table declaration
     */
    TEST_CASE_TABLE_HEADER,
    /**
     * <pre>
     * *** Keywords ***
     * </pre>
     * 
     * keyword table declaration
     */
    KEYWORD_TABLE_HEADER,
    /**
     * is i.e. "water is liquid"
     */
    QUOTES_SENTENCE,
    /**
     * separator without pipe
     */
    DOUBLE_SPACE_OR_TABULATOR_SEPARATED,
    /**
     * separator following by pipe and whitespace inside
     */
    PIPE_SEPARATED,
    /**
     * this contexts belongs to additional spaces or tabulators made by user for
     * readability increase
     */
    PRETTY_ALIGN,
    /**
     * in textual format new line \n
     */
    LINE_FEED_TEXT,
    /**
     * \t
     */
    TABULATOR_TEXT,
    /**
     * \xhh char with hex value
     */
    CHAR_WITH_BYTE_HEX_VALUE,
    /**
     * \\uhhhh
     */
    CHAR_WITH_SHORT_HEX_VALUE,
    /**
     * \\Uhhhhhhhh
     */
    UNICODE_CHAR_WITH_HEX_VALUE,
    /**
     * contains only space or tabulators or just nothing
     */
    EMPTY_LINE,
    /**
     * it could be usage of scalar ('$') or just declaration of them
     */
    SCALAR_VARIABLE,
    /**
     * it could be usage of list ('@') or just declaration of them
     */
    LIST_VARIABLE,
    /**
     * it could be usage of dictionary ('&') or just declaration of them
     */
    DICTIONARY_VARIABLE,
    /**
     * it could be usage of environment ('%') or just declaration of them
     */
    ENVIRONMENT_VARIABLE,
    /**
     * applies currently for list and dictionaries ( '[' ']' )
     */
    COLLECTION_TYPE_VARIABLE_POSITION,
    /**
     * applies currently for possible scalar variable '\$'
     */
    ESCAPED_DOLLAR_SIGN,
    /**
     * applies currently for possible list variable '\@'
     */
    ESCAPED_AT_SIGN,
    /**
     * applies currently for possible environment variable '\%'
     */
    ESCAPED_PROCENT_SIGN,
    /**
     * applies currently for possible comment '\#'
     */
    ESCAPED_HASH_SIGN,
    /**
     * never part of named argument syntax '\='
     */
    ESCAPED_EQUALS_SIGN,
    /**
     * never a separator pipe '\|'
     */
    ESCAPED_PIPE_SIGN,
    /**
     * never escapes anything '\\'
     */
    ESCAPED_BACKSLASH_SIGN,
    /**
     * never a separator between two elements i.e. 'normal \ word', robot will
     * understand it as 'normal word' and two spaces will be not taken as
     * separator
     */
    ESCAPED_SPACE,
    /**
     * Continue of previous line - three dots {@code ...} - they should be at
     * the first position
     */
    CONTINUE_PREVIOUS_LINE_DECLARATION;
}
