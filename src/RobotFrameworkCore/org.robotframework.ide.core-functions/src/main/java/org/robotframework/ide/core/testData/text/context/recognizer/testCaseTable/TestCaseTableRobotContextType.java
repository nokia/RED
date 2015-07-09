package org.robotframework.ide.core.testData.text.context.recognizer.testCaseTable;

import org.robotframework.ide.core.testData.text.context.IContextElementType;


/**
 * Gives types, which are not multiple lines - just one line and for Test Case
 * table.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 */
public enum TestCaseTableRobotContextType implements IContextElementType {
    /**
     * Used for specifying a test case documentation, appears as
     * {@code [Documentation]}
     */
    TABLE_TEST_CASE_SETTINGS_DOCUMENTATION,
    /**
     * Used for tagging test cases, appears as {@code [Tags]}
     */
    TABLE_TEST_CASE_SETTINGS_TAGS,
    /**
     * Used for specifying a test setup, appears as {@code [Setup]}
     */
    TABLE_TEST_CASE_SETTINGS_SETUP,
    /**
     * Used for specifying a test teardown, appears as {@code [Teardown]}
     */
    TABLE_TEST_CASE_SETTINGS_TEARDOWN,
    /**
     * A synonym for {@code [Setup]} {@link #TABLE_TEST_CASE_SETTINGS_SETUP}
     */
    TABLE_TEST_CASE_SETTINGS_PRECONDITION,
    /**
     * A synonym for {@code [Teardown]}
     * {@link #TABLE_TEST_CASE_SETTINGS_TEARDOWN}
     */
    TABLE_TEST_CASE_SETTINGS_POSTCONDITION,
    /**
     * Used for specifying a template keyword, appears as {@code [Template]}
     */
    TABLE_TEST_CASE_SETTINGS_TEMPLATE,
    /**
     * Used for specifying a test case timeout, appears as {@code [Timeout]}
     */
    TABLE_TEST_CASE_SETTINGS_TIMEOUT;
}
