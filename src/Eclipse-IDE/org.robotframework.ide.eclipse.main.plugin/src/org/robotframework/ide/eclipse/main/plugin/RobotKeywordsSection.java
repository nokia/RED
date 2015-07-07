package org.robotframework.ide.eclipse.main.plugin;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

public class RobotKeywordsSection extends RobotSuiteFileSection {

    public static final String SECTION_NAME = "Keywords";

    RobotKeywordsSection(final RobotSuiteFile parent, final boolean readOnly) {
        super(parent, SECTION_NAME, readOnly);
    }

    RobotKeywordDefinition createKeywordDefinition(final String name, final String[] arguments, final String comment) {
        final RobotKeywordDefinition keywordDefinition = new RobotKeywordDefinition(this, name,
                newArrayList(arguments), comment);
        elements.add(keywordDefinition);
        return keywordDefinition;
    }

    List<RobotKeywordDefinition> getUserDefinedKeywords() {
        final List<RobotKeywordDefinition> userKeywords = newArrayList();
        for (final RobotElement child : getChildren()) {
            userKeywords.add((RobotKeywordDefinition) child);
        }
        return userKeywords;
    }

}
