/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.text.IDocument;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.CaseNameRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.CommentRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ExecutableCallInSettingsRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ExecutableCallRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.InTokenRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.KeywordNameRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.KeywordSettingsRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.MatchEverythingRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.NestedExecsSpecialTokensRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.SectionHeaderRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.SettingRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.SettingsTemplateRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TestCaseSettingsRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.VariableDefinitionRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.VariableUsageRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.WithNameRule;

public class SuiteSourceEditorConfigurationTest {

    @Test
    public void coloringRulesAreCreated() throws Exception {
        final SuiteSourceEditorConfiguration config = new SuiteSourceEditorConfiguration(new SuiteSourceEditor(),
                KeySequence.getInstance());

        final Map<String, ISyntaxColouringRule[]> coloringRules = config.createColoringRules();

        assertThat(coloringRules).hasSize(5)
                .hasEntrySatisfying(IDocument.DEFAULT_CONTENT_TYPE,
                        rules -> haveExactTypes(rules, SectionHeaderRule.class, CommentRule.class,
                                MatchEverythingRule.class))
                .hasEntrySatisfying(SuiteSourcePartitionScanner.TEST_CASES_SECTION,
                        rules -> haveExactTypes(rules, SectionHeaderRule.class, CaseNameRule.class,
                                TestCaseSettingsRule.class, SettingsTemplateRule.class,
                                ExecutableCallInSettingsRule.class, ExecutableCallRule.class,
                                NestedExecsSpecialTokensRule.class, CommentRule.class, VariableUsageRule.class,
                                InTokenRule.class))
                .hasEntrySatisfying(SuiteSourcePartitionScanner.KEYWORDS_SECTION,
                        rules -> haveExactTypes(rules, SectionHeaderRule.class, KeywordNameRule.class,
                                KeywordSettingsRule.class, ExecutableCallInSettingsRule.class, ExecutableCallRule.class,
                                NestedExecsSpecialTokensRule.class, CommentRule.class, VariableUsageRule.class,
                                InTokenRule.class))
                .hasEntrySatisfying(SuiteSourcePartitionScanner.SETTINGS_SECTION,
                        rules -> haveExactTypes(rules, SectionHeaderRule.class, SettingRule.class,
                                SettingsTemplateRule.class, ExecutableCallInSettingsRule.class,
                                NestedExecsSpecialTokensRule.class, CommentRule.class, VariableUsageRule.class,
                                WithNameRule.class))
                .hasEntrySatisfying(SuiteSourcePartitionScanner.VARIABLES_SECTION,
                        rules -> haveExactTypes(rules, SectionHeaderRule.class, VariableDefinitionRule.class,
                                CommentRule.class, VariableUsageRule.class));
    }

    private void haveExactTypes(final ISyntaxColouringRule[] rules, final Class<?>... types) {
        assertThat(rules).hasSameSizeAs(types);
        for (int i = 0; i < rules.length; i++) {
            assertThat(rules[i]).isExactlyInstanceOf(types[i]);
        }
    }
}
