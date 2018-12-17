/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.CaseNameRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.CommentRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ExecutableCallInSettingsRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ExecutableCallRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.KeywordNameRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.KeywordSettingsRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.MatchEverythingRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.SectionHeaderRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.SettingRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.SettingsTemplateRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.SpecialTokensInNestedExecsRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.SpecialTokensRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TaskNameRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TaskSettingsRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TestCaseSettingsRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.VariableDefinitionRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.VariableUsageRule;

public class SuiteSourceEditorConfigurationTest {

    @Test
    public void coloringRulesAreCreated() throws Exception {
        final SuiteSourceEditorConfiguration config = new SuiteSourceEditorConfiguration(new SuiteSourceEditor(),
                KeySequence.getInstance());

        final Map<String, ISyntaxColouringRule[]> coloringRules = config.createColoringRules();

        assertThat(coloringRules).hasSize(6)
                .hasEntrySatisfying(IDocument.DEFAULT_CONTENT_TYPE,
                        rules -> haveExactTypes(rules, SectionHeaderRule.class, CommentRule.class,
                                MatchEverythingRule.class))
                .hasEntrySatisfying(SuiteSourcePartitionScanner.TEST_CASES_SECTION,
                        rules -> haveExactTypes(rules, SectionHeaderRule.class, CaseNameRule.class,
                                TestCaseSettingsRule.class, SettingsTemplateRule.class,
                                ExecutableCallInSettingsRule.class, ExecutableCallRule.class,
                                SpecialTokensInNestedExecsRule.class, CommentRule.class, VariableUsageRule.class,
                                SpecialTokensRule.class))
                .hasEntrySatisfying(SuiteSourcePartitionScanner.TASKS_SECTION,
                        rules -> haveExactTypes(rules, SectionHeaderRule.class, TaskNameRule.class,
                                TaskSettingsRule.class, SettingsTemplateRule.class,
                                ExecutableCallInSettingsRule.class, ExecutableCallRule.class,
                                SpecialTokensInNestedExecsRule.class, CommentRule.class, VariableUsageRule.class,
                                SpecialTokensRule.class))
                .hasEntrySatisfying(SuiteSourcePartitionScanner.KEYWORDS_SECTION,
                        rules -> haveExactTypes(rules, SectionHeaderRule.class, KeywordNameRule.class,
                                KeywordSettingsRule.class, ExecutableCallInSettingsRule.class, ExecutableCallRule.class,
                                SpecialTokensInNestedExecsRule.class, CommentRule.class, VariableUsageRule.class,
                                SpecialTokensRule.class))
                .hasEntrySatisfying(SuiteSourcePartitionScanner.SETTINGS_SECTION,
                        rules -> haveExactTypes(rules, SectionHeaderRule.class, SettingRule.class,
                                SettingsTemplateRule.class, ExecutableCallInSettingsRule.class,
                                SpecialTokensInNestedExecsRule.class, CommentRule.class, VariableUsageRule.class,
                                SpecialTokensRule.class))
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

    @Test
    public void redSourceDoubleClickStrategyIsProvided_independentlyOfContentType() {
        final RobotSuiteFile fileModel = new RobotSuiteFileCreator().build();
        final SuiteSourceEditor editor = ContextInjector.prepareContext()
                .inWhich(fileModel)
                .isInjectedInto(new SuiteSourceEditor());

        final SuiteSourceEditorConfiguration config = new SuiteSourceEditorConfiguration(editor,
                KeySequence.getInstance());
        final ISourceViewer sourceViewer = mock(ISourceViewer.class);

        assertThat(config.getDoubleClickStrategy(sourceViewer, null)).isInstanceOf(RedSourceDoubleClickStrategy.class);
        assertThat(config.getDoubleClickStrategy(sourceViewer, "")).isInstanceOf(RedSourceDoubleClickStrategy.class);
        assertThat(config.getDoubleClickStrategy(sourceViewer, "type"))
                .isInstanceOf(RedSourceDoubleClickStrategy.class);
        for (final String type : SuiteSourcePartitionScanner.LEGAL_CONTENT_TYPES) {
            assertThat(config.getDoubleClickStrategy(sourceViewer, type))
                    .isInstanceOf(RedSourceDoubleClickStrategy.class);
        }
    }
}
