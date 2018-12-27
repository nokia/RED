/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import java.util.List;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SectionPartitionRule.Section;

import com.google.common.collect.ImmutableList;

public class SuiteSourcePartitionScanner extends RuleBasedPartitionScanner {

    public static final String TEST_CASES_SECTION = "__test_cases_section";

    public static final String TASKS_SECTION = "__tasks_section";

    public static final String KEYWORDS_SECTION = "__keywords_section";

    public static final String SETTINGS_SECTION = "__settings_section";

    public static final String VARIABLES_SECTION = "__variables_section";

    public static final List<String> LEGAL_CONTENT_TYPES = ImmutableList.of(TEST_CASES_SECTION, TASKS_SECTION,
            KEYWORDS_SECTION, SETTINGS_SECTION, VARIABLES_SECTION);

    public SuiteSourcePartitionScanner(final boolean isTsv) {
        final List<SectionPartitionRule> rules = ImmutableList.of(
                new SectionPartitionRule(Section.TEST_CASES, new Token(TEST_CASES_SECTION), isTsv),
                new SectionPartitionRule(Section.TASKS, new Token(TASKS_SECTION), isTsv),
                new SectionPartitionRule(Section.KEYWORDS, new Token(KEYWORDS_SECTION), isTsv),
                new SectionPartitionRule(Section.SETTINGS, new Token(SETTINGS_SECTION), isTsv),
                new SectionPartitionRule(Section.VARIABLES, new Token(VARIABLES_SECTION), isTsv));

        setPredicateRules(rules.toArray(new IPredicateRule[0]));
    }
}
