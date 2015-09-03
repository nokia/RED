package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.robotframework.ide.core.testData.model.table.ARobotSectionTable;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;

public class RobotKeywordsSection extends RobotSuiteFileSection {

    public static final String SECTION_NAME = "Keywords";

    RobotKeywordsSection(final RobotSuiteFile parent) {
        super(parent, SECTION_NAME);
    }

    public RobotKeywordDefinition createKeywordDefinition(final String name) {
        return createKeywordDefinition(getChildren().size(), name);
    }

    public RobotKeywordDefinition createKeywordDefinition(final int index, final String name) {
        final RobotKeywordDefinition keywordDefinition = new RobotKeywordDefinition(this, name, "");
        elements.add(index, keywordDefinition);
        return keywordDefinition;
    }

    public RobotKeywordDefinition createKeywordDefinition(final String name, final String comment) {
        final RobotKeywordDefinition keywordDefinition = new RobotKeywordDefinition(this, name, comment);
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

    @SuppressWarnings("unchecked")
    @Override
    public List<RobotKeywordDefinition> getChildren() {
        return (List<RobotKeywordDefinition>) super.getChildren();
    }

    @Override
    public void link(final ARobotSectionTable table) {
        final KeywordTable keywordsTable = (KeywordTable) table;
        for (final UserKeyword keyword : keywordsTable.getKeywords()) {
            final RobotKeywordDefinition definition = new RobotKeywordDefinition(this,
                    keyword.getKeywordName().getText().toString(), "");
            elements.add(definition);
        }
    }
}
