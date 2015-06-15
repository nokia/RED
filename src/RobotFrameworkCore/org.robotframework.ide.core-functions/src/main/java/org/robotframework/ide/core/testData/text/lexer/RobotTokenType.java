package org.robotframework.ide.core.testData.text.lexer;

/**
 * Declares types of token recognized inside lexer.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotToken
 * @see TxtRobotTestDataLexer
 */
public enum RobotTokenType {
    /**
     * the type of token can't be recognized
     */
    UNKNOWN,
    /**
     * artificial line begin marker
     */
    START_LINE,
    /**
     * line end - it could be: carriage return and optional line feed
     */
    END_OF_LINE,
    /**
     * artificial file end marker
     */
    END_OF_FILE,
    /*
     * in hex: 0x0D
     */
    CARRITAGE_RETURN,
    /**
     * in hex: 0x0A
     */
    LINE_FEED,
    /**
     * in hex: 0x09
     */
    SINGLE_TABULATOR,
    /**
     * in hex: 0x20
     */
    SINGLE_SPACE

}
