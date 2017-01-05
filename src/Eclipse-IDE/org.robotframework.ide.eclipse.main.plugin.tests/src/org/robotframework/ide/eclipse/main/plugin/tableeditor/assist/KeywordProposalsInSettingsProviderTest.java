/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

public class KeywordProposalsInSettingsProviderTest {

    @ClassRule
    public static final ProjectProvider projectProvider = new ProjectProvider(
            KeywordProposalsInSettingsProviderTest.class);

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("all_settings.robot",
                "*** Settings ***",
                "Library",
                "Resource",
                "Variables",
                "Metadata",
                "Suite Setup",
                "Suite Teardown",
                "Test Setup",
                "Test Teardown",
                "Test Template",
                "Test Timeout",
                "Force Tags",
                "Default Tags");
        projectProvider.createFile("kw_based_settings.robot",
                "*** Settings ***",
                "Suite Setup",
                "Suite Teardown",
                "Test Setup",
                "Test Teardown",
                "Test Template");
        projectProvider.createFile("non_kw_based_settings.robot",
                "*** Settings ***",
                "Library",
                "Resource",
                "Variables",
                "Metadata",
                "Test Timeout",
                "Force Tags",
                "Default Tags");
    }

    @Test
    public void thereAreNoProposalsProvided_whenColumnIsDifferentThanSecond() {
        final RobotSuiteFile suiteFile = RedPlugin.getModelManager()
                .createSuiteFile(projectProvider.getFile("all_settings.robot"));
        final List<RobotKeywordCall> settings = suiteFile.findSection(RobotSettingsSection.class).get().getChildren();

        final IRowDataProvider<Object> dataProvider = prepareSettingsProvider(settings);
        final KeywordProposalsInSettingsProvider provider = new KeywordProposalsInSettingsProvider(suiteFile,
                dataProvider);

        for (int column = 0; column < 10; column++) {
            if (column == 1) {
                continue;
            }
            for (int row = 0; row < settings.size(); row++) {
                final AssistantContext context = new NatTableAssistantContext(column, row);
                final RedContentProposal[] proposals = provider.getProposals("foo", 0, context);
                assertThat(proposals).isEmpty();
            }
        }
    }
    
    @Test
    public void thereAreNoProposalsProvided_whenSettingIsNotKeywordBased() throws Exception {
        final RobotSuiteFile suiteFile = RedPlugin.getModelManager()
                .createSuiteFile(projectProvider.getFile("non_kw_based_settings.robot"));
        final List<RobotKeywordCall> settings = suiteFile.findSection(RobotSettingsSection.class).get().getChildren();

        final IRowDataProvider<Object> dataProvider = prepareSettingsProvider(settings);
        final KeywordProposalsInSettingsProvider provider = new KeywordProposalsInSettingsProvider(suiteFile,
                dataProvider);

        for (int row = 0; row < settings.size(); row++) {
            final AssistantContext context = new NatTableAssistantContext(1, row);
            final RedContentProposal[] proposals = provider.getProposals("foo", 0, context);
            assertThat(proposals).isEmpty();
        }
    }
    
    @Test
    public void thereAreNoProposalsProvided_whenThereIsNoKeywordMatchingCurrentPrefix() throws Exception {
        final RobotSuiteFile suiteFile = RedPlugin.getModelManager()
                .createSuiteFile(projectProvider.getFile("kw_based_settings.robot"));
        final List<RobotKeywordCall> settings = suiteFile.findSection(RobotSettingsSection.class).get().getChildren();

        final IRowDataProvider<Object> dataProvider = prepareSettingsProvider(settings);
        final KeywordProposalsInSettingsProvider provider = new KeywordProposalsInSettingsProvider(suiteFile,
                dataProvider);

        for (int row = 0; row < settings.size(); row++) {
            final AssistantContext context = new NatTableAssistantContext(1, row);
            final RedContentProposal[] proposals = provider.getProposals("foo", 1, context);
            assertThat(proposals).isEmpty();
        }
    }

    @Test
    public void thereAreProposalsProvided_whenPrefixIsMatchingAndProperContentIsInserted() throws Exception {
        final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
        text.setText("kw");

        final RobotSuiteFile suiteFile = RedPlugin.getModelManager()
                .createSuiteFile(projectProvider.getFile("kw_based_settings.robot"));
        final RobotKeywordsSection kwSection = (RobotKeywordsSection) suiteFile
                .createRobotSection(RobotKeywordsSection.SECTION_NAME);
        kwSection.createKeywordDefinition("keyword");

        final List<RobotKeywordCall> settings = suiteFile.findSection(RobotSettingsSection.class).get().getChildren();

        final IRowDataProvider<Object> dataProvider = prepareSettingsProvider(settings);
        final KeywordProposalsInSettingsProvider provider = new KeywordProposalsInSettingsProvider(suiteFile,
                dataProvider);

        for (int row = 0; row < settings.size(); row++) {
            final AssistantContext context = new NatTableAssistantContext(1, row);
            final RedContentProposal[] proposals = provider.getProposals(text.getText(), 1, context);
            assertThat(proposals).hasSize(1);

            proposals[0].getModificationStrategy().insert(text, proposals[0]);
            assertThat(text.getText()).isEqualTo("keyword");
        }
    }

    private static IRowDataProvider<Object> prepareSettingsProvider(final List<RobotKeywordCall> settings) {
        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        for (int i = 0; i < settings.size(); i++) {
            final Map<String, Object> map = new HashMap<>();
            map.put(settings.get(0).getName(), new Object());
            final Entry<String, Object> entry = map.entrySet().iterator().next();

            when(dataProvider.getRowObject(i)).thenReturn(entry);
        }
        return dataProvider;
    }
}
