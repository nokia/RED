/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import java.util.List;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

public class TextEditorPartitionScanner extends RuleBasedPartitionScanner {

    public final static String TEST_CASES_SECTION = "__test_cases_section";

    public final static String KEYWORDS_SECTION = "__keywords_section";

    public final static String VARIABLES_SECTION = "__variables_section";

    public final static String SETTINGS_SECTION = "__settings_section";

    private List<String> sectionHeadersList;

    public TextEditorPartitionScanner(final RobotSuiteFile suiteFile) {
        IToken testCases = new Token(TEST_CASES_SECTION);
        IToken keywords = new Token(KEYWORDS_SECTION);
        IToken settings = new Token(SETTINGS_SECTION);
        IToken variables = new Token(VARIABLES_SECTION);

        IPredicateRule[] rules = new IPredicateRule[4];

        sectionHeadersList = suiteFile.getSectionHeaders();

        rules[0] = new TextEditorSectionRule(sectionHeadersList.get(0), "*", settings, (char) 0, false, true, sectionHeadersList);
        rules[1] = new TextEditorSectionRule(sectionHeadersList.get(1), "*", variables, (char) 0, false, true, sectionHeadersList);
        rules[2] = new TextEditorSectionRule(sectionHeadersList.get(2), "*", testCases, (char) 0, false, true, sectionHeadersList);
        rules[3] = new TextEditorSectionRule(sectionHeadersList.get(3), "*", keywords, (char) 0, false, true, sectionHeadersList);

        setPredicateRules(rules);
    }

    public List<String> getHeadersList() {
        return sectionHeadersList;
    }

}
