/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.syntaxHighlighting;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Display;


public class RulesGenerator {

    private final Set<Resource> resources = newHashSet();
    
    private IToken sectionToken;
    private IToken variableToken;
    private IToken commentToken;
    private IToken settingsToken;
    private IToken keywordToken;
    
    private WordRule asteriskWordRule;
    private WordRule testCasesSectionWordsRule;
    private WordRule keywordsSectionWordsRule;
    
    private IRule settingsSectionRule;
    private IRule testCasesSectionRule;
    private IRule keywordsSectionRule;
    private IRule variablesSectionRule;
    
    private IRule commentRule;
    
    private KeywordRule keywordsRule;
    
    private KeywordRule settingsSectionWordsRule;
    
    private List<IRule> commonRules = newArrayList();
    
    public RulesGenerator(final Display display, final List<String> keywordList) {
        createTokens(display);
        
        createSectionsRules();
        createSettingsWordsRule();
        createKeywordRule(keywordList);
        createCommentRule();
        
        fillCommonRules();
    }
    
    public IRule[] getSettingsSectionRules() {
        List<IRule> rules = newArrayList();
        rules.add(settingsSectionWordsRule);
        rules.add(settingsSectionRule);
        rules.addAll(commonRules);
        
        return rules.toArray(new IRule[rules.size()]);
    }
    
    public IRule[] getKeywordsSectionRules() {
        List<IRule> rules = newArrayList();
        rules.add(keywordsRule);
        rules.add(keywordsSectionRule);
        rules.add(keywordsSectionWordsRule);
        rules.addAll(commonRules);

        return rules.toArray(new IRule[rules.size()]);
    }
    
    public IRule[] getTestCasesSectionRules() {
        List<IRule> rules = newArrayList();
        rules.add(keywordsRule);
        rules.add(testCasesSectionRule);
        rules.add(testCasesSectionWordsRule);
        rules.addAll(commonRules);

        return rules.toArray(new IRule[rules.size()]);
    }
    
    public IRule[] getVariablesSectionRules() {
        List<IRule> rules = newArrayList();
        rules.add(variablesSectionRule);
        rules.addAll(commonRules);

        return rules.toArray(new IRule[rules.size()]);
    }
    
    public IRule[] getDefaultRules() {
        List<IRule> rules = newArrayList();
        rules.add(asteriskWordRule);
        rules.add(commentRule);

        return rules.toArray(new IRule[rules.size()]);
    }
    
    private void fillCommonRules() {
        
        final SingleLineRule scalarRule = new SingleLineRule("${", "}", variableToken);
        final SingleLineRule listRule = new SingleLineRule("@{", "}", variableToken);
        final SingleLineRule dictionaryRule = new SingleLineRule("&{", "}", variableToken);
        
        commonRules.add(scalarRule);
        commonRules.add(listRule);
        commonRules.add(dictionaryRule);
        
        commonRules.add(commentRule);
        commonRules.add(asteriskWordRule);
    }
    
    private void createTokens(final Display display) {

        // TODO : colors for syntax highlighting should be handled via some other
        // mechanism (preferences probably)

        final Color redSystemColor = display.getSystemColor(SWT.COLOR_RED);
        final Color greenSystemColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
        final Color graySystemColor = display.getSystemColor(SWT.COLOR_DARK_GRAY);
        final Color userColor = new Color(display, 0, 128, 192);
        resources.add(userColor);
        final Color userSettingsColor = new Color(display, 149, 0, 85);
        resources.add(userSettingsColor);
        
        sectionToken = new Token(new TextAttribute(redSystemColor));
        variableToken = new Token(new TextAttribute(greenSystemColor));
        commentToken = new Token(new TextAttribute(graySystemColor));
        settingsToken = new Token(new TextAttribute(userSettingsColor));
        keywordToken = new Token(new TextAttribute(userColor, null, SWT.BOLD));
    }
    
    private void createSectionsRules() {
        settingsSectionRule = new SingleLineRule("Settings", "***", sectionToken);
        testCasesSectionRule = new SingleLineRule("Test Cases", "***", sectionToken);
        keywordsSectionRule = new SingleLineRule("Keywords", "***", sectionToken);
        variablesSectionRule = new SingleLineRule("Variables", "***", sectionToken);
        
        asteriskWordRule = setupWordRule();
        asteriskWordRule.addWord("***", sectionToken);
        asteriskWordRule.setColumnConstraint(0);
    }
    
    private void createSettingsWordsRule() {
        final List<String> words = newArrayList();
        words.add("Library");
        words.add("Resource");
        words.add("Variables");
        words.add("Documentation");
        words.add("Metadata");
        words.add("Suite Setup");
        words.add("Suite Teardown");
        words.add("Force Tags");
        words.add("Default Tags");
        words.add("Test Setup");
        words.add("Test Teardown");
        words.add("Test Template");
        words.add("Test Timeout");
        settingsSectionWordsRule = new KeywordRule(settingsToken, words);
        settingsSectionWordsRule.setColumnConstraint(0);
        
        
        testCasesSectionWordsRule = setupWordRule();
        testCasesSectionWordsRule.addWord("[Documentation]", settingsToken);
        testCasesSectionWordsRule.addWord("[Tags]", settingsToken);
        testCasesSectionWordsRule.addWord("[Setup]", settingsToken);
        testCasesSectionWordsRule.addWord("[Teardown]", settingsToken);
        testCasesSectionWordsRule.addWord("[Template]", settingsToken);
        testCasesSectionWordsRule.addWord("[Timeout]", settingsToken);
        
        keywordsSectionWordsRule = setupWordRule();
        keywordsSectionWordsRule.addWord("[Documentation]", settingsToken);
        keywordsSectionWordsRule.addWord("[Tags]", settingsToken);
        keywordsSectionWordsRule.addWord("[Arguments]", settingsToken);
        keywordsSectionWordsRule.addWord("[Return]", settingsToken);
        keywordsSectionWordsRule.addWord("[Teardown]", settingsToken);
        keywordsSectionWordsRule.addWord("[Timeout]", settingsToken);
    }
    
    private void createCommentRule() {
        commentRule =  new EndOfLineRule("#", commentToken);
    }
    
    private void createKeywordRule(final List<String> keywordList) {
        keywordsRule = new KeywordRule(keywordToken, keywordList);
    }
    
    private WordRule setupWordRule() {
        return new WordRule(new IWordDetector() {

            @Override
            public boolean isWordStart(char c) {
                return !Character.isWhitespace(c);
            }

            @Override
            public boolean isWordPart(char c) {
                return !Character.isWhitespace(c);
            }
        });
    }
   
    public void disposeResources() {
        for (final Resource resource : resources) {
            resource.dispose();
        }
    }
    
}
