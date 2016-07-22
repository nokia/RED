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

public class RobotKeywordsSection extends RobotSuiteFileSection {

    public static final String SECTION_NAME = "Keywords";

    RobotKeywordsSection(final RobotSuiteFile parent) {
        super(parent, SECTION_NAME);
    }

    public RobotKeywordDefinition createKeywordDefinition(final String name) {
        return createKeywordDefinition(getChildren().size(), name);
    }

    public RobotKeywordDefinition createKeywordDefinition(final int index, final String name) {
        final RobotKeywordDefinition keywordDefinition;
        
        final KeywordTable keywordsTable = (KeywordTable) this.getLinkedElement();
        if(index >= 0 && index < keywordsTable.getKeywords().size() && index < getChildren().size()) {
            keywordDefinition = new RobotKeywordDefinition(this, keywordsTable.createUserKeyword(name, index));
            elements.add(index, keywordDefinition);
        } else {
            keywordDefinition = new RobotKeywordDefinition(this, keywordsTable.createUserKeyword(name));
            elements.add(keywordDefinition);
        }
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
        super.link(table);

        final KeywordTable keywordsTable = (KeywordTable) sectionTable;
        for (final UserKeyword keyword : keywordsTable.getKeywords()) {
            final RobotKeywordDefinition definition = new RobotKeywordDefinition(this, keyword);
            definition.link();
            elements.add(definition);
        }
    }
}
