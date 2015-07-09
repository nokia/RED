package org.robotframework.ide.core.testData.text.context.recognizer.keywordTable;

import org.robotframework.ide.core.testData.text.context.IContextElementType;


/**
 * Gives types, which are not multiple lines - just one line and for Test Case
 * table.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 */
public enum KeywordTableRobotContextType implements IContextElementType {
    /**
     * Used for specifying a user keyword documentation, appears as
     * {@code [Documentation]}
     */
    TABLE_KEYWORD_SETTINGS_DOCUMENTATION,
    /**
     * Used for specifying user keyword arguments, appears as
     * {@code [Arguments]}
     */
    TABLE_KEYWORD_SETTINGS_ARGUMENTS,
    /**
     * Used for specifying user keyword return values, appears as
     * {@code [Return]}
     */
    TABLE_KEYWORD_SETTINGS_RETURN,
    /**
     * Used for specifying user keyword teardown, appears as {@code [Teardown]}
     */
    TABLE_KEYWORD_SETTINGS_TEARDOWN,
    /**
     * Used for specifying a user keyword timeout, appears as {@code [Timeout]}
     */
    TABLE_KEYWORD_SETTINGS_TIMEOUT;
}
