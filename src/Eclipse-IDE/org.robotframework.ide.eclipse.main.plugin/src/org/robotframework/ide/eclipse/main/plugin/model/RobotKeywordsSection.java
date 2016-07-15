/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

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
        
        KeywordTable keywordsTable = (KeywordTable) this.getLinkedElement();
        keywordDefinition.link(keywordsTable.createUserKeyword(name));
        
        elements.add(index, keywordDefinition);
        return keywordDefinition;
    }
    
    public void insertKeywordDefinitionCopy(final int index, final RobotKeywordDefinition definition) {
        final String newKeywordDefinitionName = definition.getName() + " - Copy";
        final RobotKeywordDefinition newKeywordDefinition = new RobotKeywordDefinition(this, newKeywordDefinitionName,
                definition.getComment());

        final KeywordTable keywordsTable = (KeywordTable) this.getLinkedElement();
        final RobotToken nameToken = new RobotToken();
        nameToken.setText(newKeywordDefinitionName);
        final UserKeyword userKeyword = new UserKeyword(nameToken);
        newKeywordDefinition.link(userKeyword);
        if (index >= 0 && index < keywordsTable.getKeywords().size()) {
            keywordsTable.addKeyword(userKeyword, index);
            elements.add(index, newKeywordDefinition);
        } else {
            keywordsTable.addKeyword(userKeyword);
            elements.add(newKeywordDefinition);
        }
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
        super.link(table);

        final KeywordTable keywordsTable = (KeywordTable) sectionTable;
        for (final UserKeyword keyword : keywordsTable.getKeywords()) {
            final RobotKeywordDefinition definition = new RobotKeywordDefinition(this,
                    keyword.getKeywordName().getText().toString(), "");
            definition.link(keyword);
            elements.add(definition);
        }
    }
}
