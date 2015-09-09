/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;


public class TextEditorPartitionScanner extends RuleBasedPartitionScanner {

    public final static String TEST_CASES_SECTION = "__test_cases_section";
    public final static String KEYWORDS_SECTION = "__keywords_section";
    public final static String VARIABLES_SECTION = "__variables_section";
    public final static String SETTINGS_SECTION = "__settings_section";

    public TextEditorPartitionScanner()
    {
        IToken testCases = new Token(TEST_CASES_SECTION);
        IToken keywords = new Token(KEYWORDS_SECTION);
        IToken settings = new Token(SETTINGS_SECTION);
        IToken variables = new Token(VARIABLES_SECTION);

        IPredicateRule[] rules = new IPredicateRule[4];

        rules[0] = new MultiLineRule("Test Cases ***", "***", testCases, (char) 0, true);
        rules[1] = new MultiLineRule("Keywords ***", "***", keywords, (char) 0, true);
        rules[2] = new MultiLineRule("Settings ***", "***", settings, (char) 0, true);
        rules[3] = new MultiLineRule("Variables ***", "***", variables, (char) 0, true);

        setPredicateRules(rules);
    }
}
