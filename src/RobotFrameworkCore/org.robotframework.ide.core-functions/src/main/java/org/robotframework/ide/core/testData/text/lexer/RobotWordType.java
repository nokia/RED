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
     * 
     */
    UNKNOWN_WORD(null, null),
    /**
     * 
     */
    SETTING_WORD("setting", "Setting"),
    /**
     * 
     */
    SETTINGS_WORD("settings", "Settings"),
    /**
     * 
     */
    METADATA_WORD("metadata", "Metadata"),
    /**
     * 
     */
    VARIABLE_WORD("variable", "Variable"),
    /**
     * 
     */
    VARIABLES_WORD("variables", "Variables"),
    /**
     * 
     */
    TEST_WORD("test", "Test"),
    /**
     * 
     */
    CASE_WORD("case", "Case"),
    /**
     * 
     */
    CASES_WORD("cases", "Cases"),
    /**
     * 
     */
    KEYWORD_WORD("keyword", "Keyword"),
    /**
     * 
     */
    KEYWORDS_WORD("keywords", "Keywords"),
    /**
     * 
     */
    USER_WORD("user", "User"),
    /**
     * 
     */
    LIBRARY_WORD("library", "Library"),
    /**
     * 
     */
    WITH_WORD("with", "WITH"),
    /**
     * 
     */
    NAME_WORD("name", "NAME"),
    /**
     * 
     */
    RESOURCE_WORD("resource", "Resource"),
    /**
     * 
     */
    DOCUMENTATION_WORD("documentation", "Documentation"),
    /**
     * 
     */
    SUITE_WORD("suite", "Suite"),
    /**
     * 
     */
    SETUP_WORD("setup", "Setup"),
    /**
     * 
     */
    TEARDOWN_WORD("teardown", "Teardown"),
    /**
     * 
     */
    PRECONDITION_WORD("precondition", "Precondition"),
    /**
     * 
     */
    POSTCONDITION_WORD("postcondition", "Postcondition"),
    /**
     * 
     */
    FORCE_WORD("force", "Force"),
    /**
     * 
     */
    DEFAULT_WORD("default", "Default"),
    /**
     * 
     */
    TAGS_WORD("tags", "Tags"),
    /**
     * 
     */
    TEMPLATE_WORD("template", "Template"),
    /**
     * 
     */
    TIMEOUT_WORD("timeout", "Timeout"),
    /**
     * 
     */
    ARGUMENTS_WORD("arguments", "Arguments"),
    /**
     * 
     */
    RETURN_WORD("return", "Return"),
    /**
     * 
     */
    FOR_WORD("for", "FOR"),
    /**
     * 
     */
    IN_WORD("in", "IN"),
    /**
     * 
     */
    RANGE_WORD("range", "RANGE"),
    /**
     * 
     */
    DOUBLE_SPACE("  ", "  "),
    /**
     * 
     */
    EMPTY_CELL_DOTS("..", ".."),
    /**
     * 
     */
    CONTINOUE_PREVIOUS_LINE_DOTS("...", "..."),
    /**
     * 
     */
    DOUBLE_ESCAPE_BACKSLASH("\\\\", "\\\\");

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
}
