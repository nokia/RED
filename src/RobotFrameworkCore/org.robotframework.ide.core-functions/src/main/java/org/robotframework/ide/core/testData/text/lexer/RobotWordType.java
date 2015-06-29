package org.robotframework.ide.core.testData.text.lexer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Represents words, which has special meaning in Robot Framework test data.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public enum RobotWordType implements RobotType {
    /**
     * It value, which do not have any special meaning, text to put should be
     * taken from token not from this type
     */
    UNKNOWN_WORD(null, null),
    /**
     * this is one of Setting table name
     */
    SETTING_WORD("setting", "Setting"),
    /**
     * this is one of Setting table name
     */
    SETTINGS_WORD("settings", "Settings"),
    /**
     * this is one of Setting table name or it is one of it element
     */
    METADATA_WORD("metadata", "Metadata"),
    /**
     * this is one of Variable table name
     */
    VARIABLE_WORD("variable", "Variable"),
    /**
     * this is one of Variable table name or as part of Settings table to <i>
     * aking variable files into use </i>
     */
    VARIABLES_WORD("variables", "Variables"),
    /**
     * word appears as part of Test Case table name or element of setting table
     * name element related to pre-configuration of test case table like Test
     * Setup or Test Teardown
     */
    TEST_WORD("test", "Test"),
    /**
     * word appears as part of Test Case table name
     */
    CASE_WORD("case", "Case"),
    /**
     * word appears as part of Test Case table name
     */
    CASES_WORD("cases", "Cases"),
    /**
     * word is Keyword table name or part of them in case of User Keyword names
     */
    KEYWORD_WORD("keyword", "Keyword"),
    /**
     * word is Keyword table name or part of them in case of User Keywords names
     */
    KEYWORDS_WORD("keywords", "Keywords"),
    /**
     * word is part of Keyword table name
     */
    USER_WORD("user", "User"),
    /**
     * part of Settings table section, <i> Used for taking test libraries into
     * use.</i>
     */
    LIBRARY_WORD("library", "Library"),
    /**
     * part of aliases used in library import
     */
    WITH_WORD("with", "WITH"),
    /**
     * part of aliases used in library import
     */
    NAME_WORD("name", "NAME"),
    /**
     * part of Settings table section, <i> Used for taking resource files into
     * use. </i>
     */
    RESOURCE_WORD("resource", "Resource"),
    /**
     * part of Settings table section ( <i> Used for specifying a test suite or
     * resource file documentation. </i> ) or for the same propose in Test Case
     * table and Keyword table.
     */
    DOCUMENTATION_WORD("documentation", "Documentation"),
    /**
     * part of Settings table section related to declaration of Suite
     * pre-configuration before and after test is executed.
     */
    SUITE_WORD("suite", "Suite"),
    /**
     * part of Settings table or Test Case table section related to the first
     * phase of handling test execution.
     */
    SETUP_WORD("setup", "Setup"),
    /**
     * part of Settings table or Test Case table section related to the last
     * phase of handling test execution.
     */
    TEARDOWN_WORD("teardown", "Teardown"),
    /**
     * part of Settings table or Test Case table section related to the first
     * phase of handling test execution.
     */
    PRECONDITION_WORD("precondition", "Precondition"),
    /**
     * part of Settings table or Test Case table section related to the last
     * phase of handling test execution.
     */
    POSTCONDITION_WORD("postcondition", "Postcondition"),
    /**
     * part of Settings table related to <i> specifying forced values for tags
     * when tagging test cases </i>
     */
    FORCE_WORD("force", "Force"),
    /**
     * part of Settings table related to <i> specifying default values for tags
     * when tagging test cases </i>
     */
    DEFAULT_WORD("default", "Default"),
    /**
     * part of Settings table or Test Case table related to test tags
     */
    TAGS_WORD("tags", "Tags"),
    /**
     * part of Settings table or Test Case table related to <i> specifying a
     * template keyword for test cases </i> in case of Data-Driven acceptance
     * tests.
     */
    TEMPLATE_WORD("template", "Template"),
    /**
     * part of Settings table, Test Case table and Keyword table used to
     * specifying for Robot Framework time slot acceptable for current element
     * execution.
     */
    TIMEOUT_WORD("timeout", "Timeout"),
    /**
     * part of Keyword table used for <i> specifying user keyword arguments </i>
     */
    ARGUMENTS_WORD("arguments", "Arguments"),
    /**
     * part of Keyword table used for <i> specifying user keyword return values
     * </i>
     */
    RETURN_WORD("return", "Return"),
    /**
     * it can be loop begin declaration in Test Case table or Keyword table
     */
    FOR_WORD("for", "FOR"),
    /**
     * it specifying for loop over what it should iterate
     */
    IN_WORD("in", "IN"),
    /**
     * this word add restriction to loop iteration or could give number, char,
     * word range to use
     */
    RANGE_WORD("range", "RANGE"),
    /**
     * this is one of arguments separator in robot file line
     */
    DOUBLE_SPACE("  ", "  "),
    /**
     * this could be used as substitution of backslash for empty cells
     */
    EMPTY_CELL_DOTS("..", ".."),
    /**
     * 
     */
    CONTINOUE_PREVIOUS_LINE_DOTS("...", "..."),
    /**
     * 
     */
    DOUBLE_ESCAPE_BACKSLASH("\\\\", "\\\\"),
    /**
     * 
     */
    COMMENT_FROM_BUILTIN("comment", "Comment");

    private final String aliases;
    private final String toWriteText;

    private static final Map<String, RobotWordType> reservedWordTypes;

    static {
        Map<String, RobotWordType> temp = new HashMap<>();
        RobotWordType[] values = RobotWordType.values();
        for (RobotWordType type : values) {
            temp.put(type.aliases, type);
        }

        reservedWordTypes = Collections.unmodifiableMap(temp);
    }


    @Override
    public String toWrite() {
        return toWriteText;
    }


    public static RobotWordType getToken(String text) {
        RobotWordType type = RobotWordType.UNKNOWN_WORD;
        if (text != null) {
            RobotWordType foundType = reservedWordTypes.get(text.toLowerCase());
            if (foundType != null) {
                type = foundType;
            }
        }
        return type;
    }


    private RobotWordType(final String aliases, final String toWriteText) {
        this.aliases = aliases;
        this.toWriteText = toWriteText;
    }


    @Override
    public boolean isWriteable() {
        return (this.toWriteText != null);
    }


    public static RobotType getToken(StringBuilder text) {
        RobotWordType type = RobotWordType.UNKNOWN_WORD;
        if (text != null) {
            type = getToken(text.toString());
        }

        return type;
    }


    @Override
    public RobotType getTokenType(StringBuilder text) {
        return getToken(text);
    }


    @Override
    public RobotType getTokenType(String text) {
        return getToken(text);
    }
}
