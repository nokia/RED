package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

public class RobotKeywordsSection extends RobotSuiteFileSection {

    public static final String SECTION_NAME = "Keywords";

    RobotKeywordsSection(final RobotSuiteFile parent, final boolean readOnly) {
        super(parent, SECTION_NAME, readOnly);
    }

    public RobotKeywordDefinition createKeywordDefinition(final String name, final List<String> arguments,
            final String comment) {
        return createKeywordDefinition(getChildren().size(), name, arguments, comment);
    }

    public RobotKeywordDefinition createKeywordDefinition(final int index, final String name,
            final List<String> arguments, final String comment) {
        final RobotKeywordDefinition keywordDefinition = new RobotKeywordDefinition(this, name,
                newArrayList(arguments), comment);
        elements.add(index, keywordDefinition);
        return keywordDefinition;
    }

    List<RobotKeywordDefinition> getUserDefinedKeywords() {
        final List<RobotKeywordDefinition> userKeywords = newArrayList();
        for (final RobotElement child : getChildren()) {
            userKeywords.add((RobotKeywordDefinition) child);
        }
        return userKeywords;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<RobotKeywordDefinition> getChildren() {
        return (List<RobotKeywordDefinition>) super.getChildren();
    }
}
