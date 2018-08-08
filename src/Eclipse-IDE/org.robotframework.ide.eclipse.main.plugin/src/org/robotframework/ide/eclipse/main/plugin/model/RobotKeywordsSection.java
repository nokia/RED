/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;

public class RobotKeywordsSection extends RobotSuiteFileSection {

    public static final String SECTION_NAME = "Keywords";

    RobotKeywordsSection(final RobotSuiteFile parent, final KeywordTable keywordTable) {
        super(parent, SECTION_NAME, keywordTable);
    }

    @Override
    public void link() {
        final KeywordTable keywordsTable = getLinkedElement();
        for (final UserKeyword keyword : keywordsTable.getKeywords()) {
            final RobotKeywordDefinition definition = new RobotKeywordDefinition(this, keyword);
            definition.link();
            elements.add(definition);
        }
    }

    @Override
    public KeywordTable getLinkedElement() {
        return (KeywordTable) super.getLinkedElement();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<RobotKeywordDefinition> getChildren() {
        return (List<RobotKeywordDefinition>) super.getChildren();
    }

    @Override
    public String getDefaultChildName() {
        return "Keyword";
    }

    @Override
    public RobotKeywordDefinition createChild(final int index, final String name) {
        final RobotKeywordDefinition keywordDefinition;

        final KeywordTable keywordsTable = getLinkedElement();
        if (index >= 0 && index < getChildren().size()) {
            keywordDefinition = new RobotKeywordDefinition(this, keywordsTable.createUserKeyword(name, index));
            elements.add(index, keywordDefinition);
        } else {
            keywordDefinition = new RobotKeywordDefinition(this, keywordsTable.createUserKeyword(name));
            elements.add(keywordDefinition);
        }
        return keywordDefinition;
    }

    @Override
    public void insertChild(final int index, final RobotFileInternalElement element) {
        final RobotKeywordDefinition keyword = (RobotKeywordDefinition) element;
        keyword.setParent(this);

        final KeywordTable keywordsTable = getLinkedElement();
        if (index >= 0 && index < elements.size()) {
            getChildren().add(index, keyword);
            keywordsTable.addKeyword(keyword.getLinkedElement(), index);
        } else {
            getChildren().add(keyword);
            keywordsTable.addKeyword(keyword.getLinkedElement());
        }
    }
    
    @Override
    public void removeChildren(final List<? extends RobotFileInternalElement> elementsToRemove) {
        getChildren().removeAll(elementsToRemove);

        final KeywordTable linkedElement = getLinkedElement();
        for (final RobotFileInternalElement elementToDelete : elementsToRemove) {
            linkedElement.removeKeyword((UserKeyword) elementToDelete.getLinkedElement());
        }
    }

    List<RobotKeywordDefinition> getUserDefinedKeywords() {
        return getChildren().stream().map(RobotKeywordDefinition.class::cast).collect(toList());
    }
}