/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Iterables.any;

import java.util.List;

import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.keywords.KeywordReturn;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTags;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTeardown;
import org.rf.ide.core.testdata.model.table.keywords.KeywordTimeout;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.NamesGenerator;

import com.google.common.base.Predicate;

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
        if(index >= 0 && index < keywordsTable.getKeywords().size() && index < getChildren().size()) {
            keywordDefinition.link(keywordsTable.createUserKeyword(name, index));
            elements.add(index, keywordDefinition);
        } else {
            keywordDefinition.link(keywordsTable.createUserKeyword(name));
            elements.add(keywordDefinition);
        }
        
        return keywordDefinition;
    }
    
    @SuppressWarnings("unchecked")
    public void insertKeywordDefinitionCopy(final int index, final RobotKeywordDefinition definition) {
        
        final boolean hasKeywordDefinitionNameAlreadyInTable = any(getChildren(), new Predicate<RobotKeywordDefinition>() {

            @Override
            public boolean apply(final RobotKeywordDefinition def) {
                return def.getName().equalsIgnoreCase(definition.getName());
            }
        });
        final String newKeywordDefinitionName = hasKeywordDefinitionNameAlreadyInTable
                ? NamesGenerator.generateUniqueName(this, definition.getName(), true) : definition.getName();
        final RobotKeywordDefinition newKeywordDefinition = new RobotKeywordDefinition(this, newKeywordDefinitionName,
                definition.getComment());

        final RobotToken nameToken = new RobotToken();
        nameToken.setText(newKeywordDefinitionName);
        final UserKeyword userKeyword = new UserKeyword(nameToken);
        
        for (final RobotKeywordCall keywordCall : definition.getExecutableRows()) {
            userKeyword.addKeywordExecutionRow((RobotExecutableRow<UserKeyword>) keywordCall.getLinkedElement());
        }

        final RobotDefinitionSetting argumentsSetting = definition.getArgumentsSetting();
        if (argumentsSetting != null) {
            userKeyword.addArguments((KeywordArguments) argumentsSetting.getLinkedElement());
        }
        final RobotDefinitionSetting documentationSetting = definition.getDocumentationSetting();
        if (documentationSetting != null) {
            userKeyword.addDocumentation((KeywordDocumentation) documentationSetting.getLinkedElement());
        }
        final RobotDefinitionSetting returnValueSetting = definition.getReturnValueSetting();
        if (returnValueSetting != null) {
            userKeyword.addReturn((KeywordReturn) returnValueSetting.getLinkedElement());
        }
        final RobotDefinitionSetting tagsSetting = definition.getTagsSetting();
        if (tagsSetting != null) {
            userKeyword.addTag((KeywordTags) tagsSetting.getLinkedElement());
        }
        final RobotDefinitionSetting teardownSetting = definition.getTeardownSetting();
        if (teardownSetting != null) {
            userKeyword.addTeardown((KeywordTeardown) teardownSetting.getLinkedElement());
        }
        final RobotDefinitionSetting timeoutSetting = definition.getTimeoutSetting();
        if (timeoutSetting != null) {
            userKeyword.addTimeout((KeywordTimeout) timeoutSetting.getLinkedElement());
        }

        newKeywordDefinition.link(userKeyword);
        final KeywordTable keywordsTable = (KeywordTable) this.getLinkedElement();
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
