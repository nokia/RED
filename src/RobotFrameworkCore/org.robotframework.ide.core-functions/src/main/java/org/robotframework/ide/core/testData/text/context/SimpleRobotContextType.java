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
    KEYWORD_TABLE_HEADER;
}
