package org.robotframework.ide.core.testData.text.context.recognizer.settingTable;

import org.robotframework.ide.core.testData.text.context.IContextElementType;


/**
 * Gives types, which are not multiple lines - just one line and for Setting
 * table.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 */
public enum SettingTableRobotContextType implements IContextElementType {
    /**
     * Library import begin declaration
     */
    TABLE_SETTINGS_LIBRARY,
    /**
     * Resource variables and keywords import begin declaration
     */
    TABLE_SETTINGS_RESOURCE,
    /**
     * Importing variables file declaration
     */
    TABLE_SETTINGS_VARIABLES,
    /**
     * Documentation for suite declaration
     */
    TABLE_SETTINGS_DOCUMENTATION,
    /**
     * User key {separator} value additional information about suite
     */
    TABLE_SETTINGS_METADATA,
    /**
     * Suite Setup keyword
     */
    TABLE_SETTINGS_SUITE_SETUP,
    /**
     * Suite Teardown keyword
     */
    TABLE_SETTINGS_SUITE_TEARDOWN,
    /**
     * the same as {@link #TABLE_SETTINGS_SUITE_SETUP}
     */
    TABLE_SETTINGS_SUITE_PRECONDITION,
    /**
     * the same as {@link #TABLE_SETTINGS_SUITE_TEARDOWN}
     */
    TABLE_SETTINGS_SUITE_POSTCONDTION,
    /**
     * Used for specifying forced values for tags when tagging test cases.
     */
    TABLE_SETTINGS_FORCE_TAGS,
    /**
     * Used for specifying default values for tags when tagging test cases.
     */
    TABLE_SETTINGS_DEFAULT_TAGS,
    /**
     * default setup for all test cases in suite
     */
    TABLE_SETTINGS_TEST_SETUP,
    /**
     * default teardown for all test cases in suite
     */
    TABLE_SETTINGS_TEST_TEARDOWN,
    /**
     * the same as {@link #TABLE_SETTINGS_TEST_SETUP}
     */
    TABLE_SETTINGS_TEST_PRECONDITION,
    /**
     * the same as {@link #TABLE_SETTINGS_SUITE_TEARDOWN}
     */
    TABLE_SETTINGS_TEST_POSTCONDITION,
    /**
     * default template keyword for test cases in data-driven tests
     */
    TABLE_SETTINGS_TEST_TEMPLATE,
    /**
     * default test case timeout
     */
    TABLE_SETTINGS_TEST_TIMEOUT;
}
