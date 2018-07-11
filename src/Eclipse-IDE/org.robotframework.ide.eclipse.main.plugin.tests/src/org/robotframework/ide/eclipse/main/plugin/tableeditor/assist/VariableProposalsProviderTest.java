/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ExecutionEnvironment;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

public class VariableProposalsProviderTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(
            VariableProposalsProviderTest.class);

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("suite.robot",
                "*** Variables ***",
                "${a_var}",
                "${b_var}",
                "@{c_list}",
                "*** Settings ***", "Suite Setup",
                "*** Test Cases ***",
                "case",
                "  call");

        // skipping global variables
        final RobotProjectConfig config = RobotProjectConfig.create();
        config.setExecutionEnvironment(ExecutionEnvironment.create("", null));
        projectProvider.configure(config);
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrownWhenDataProviderReturnsOrdinaryObject() {
        final RobotSuiteFile suite = new RobotModel().createSuiteFile(projectProvider.getFile("suite.robot"));

        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(new Object());

        final VariableProposalsProvider provider = new VariableProposalsProvider(suite, dataProvider);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        provider.getProposals("${xyz}", 0, context);
    }

    @Test
    public void thereAreNoVariablesProposalsProvided_whenThereIsNoVariableMatchingCurrentInput() {
        RobotModel model = new RobotModel();
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.getFile("suite.robot"));
        final RobotKeywordCall callElement = suite.findSection(RobotCasesSection.class)
                .get().getChildren().get(0).getChildren().get(0);

        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(callElement);

        final VariableProposalsProvider provider = new VariableProposalsProvider(suite, dataProvider);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.getProposals("${xyz}", 3, context);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreVariablesProposalsProvided_alsoWhenSettingIsWrappedAsEntry() {
        final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
        text.setText("${blah}");
        text.setSelection(3);

        final RobotSuiteFile suite = new RobotModel().createSuiteFile(projectProvider.getFile("suite.robot"));
        final RobotKeywordCall settingElement = suite.findSection(RobotSettingsSection.class).get().getChildren().get(0);
        final Entry<String, RobotKeywordCall> entry = Iterables.getFirst(ImmutableMap.of("x", settingElement).entrySet(), null);

        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(entry);

        final VariableProposalsProvider provider = new VariableProposalsProvider(suite, dataProvider);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.getProposals(text.getText(), text.getSelection().x, context);
        assertThat(proposals).hasSize(1);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("${b_var}");
    }

    @Test
    public void thereAreVariablesProposalsProvided_whenThereIsAProposalMatchingCurrentContent_1() {
        final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
        text.setText("${blah}");
        text.setSelection(3);

        final RobotSuiteFile suite = new RobotModel().createSuiteFile(projectProvider.getFile("suite.robot"));
        final RobotKeywordCall callElement = suite.findSection(RobotCasesSection.class)
                .get().getChildren().get(0).getChildren().get(0);

        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(callElement);

        final VariableProposalsProvider provider = new VariableProposalsProvider(suite, dataProvider);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.getProposals(text.getText(), text.getSelection().x, context);
        assertThat(proposals).hasSize(1);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("${b_var}");
    }

    @Test
    public void thereAreVariablesProposalsProvided_whenThereIsAProposalMatchingCurrentContent_2() {
        final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
        text.setText("abc");
        text.setSelection(1);

        final RobotSuiteFile suite = new RobotModel().createSuiteFile(projectProvider.getFile("suite.robot"));
        final RobotKeywordCall callElement = suite.findSection(RobotCasesSection.class)
                .get().getChildren().get(0).getChildren().get(0);

        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(callElement);

        final VariableProposalsProvider provider = new VariableProposalsProvider(suite, dataProvider);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.getProposals(text.getText(), text.getSelection().x, context);
        assertThat(proposals).hasSize(2);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("${a_var}");
    }

    @Test
    public void thereAreVariablesProposalsProvided_whenThereIsAProposalMatchingCurrentContent_3() {
        final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
        text.setText("a${abc}b");
        text.setSelection(4);

        final RobotSuiteFile suite = new RobotModel().createSuiteFile(projectProvider.getFile("suite.robot"));
        final RobotKeywordCall callElement = suite.findSection(RobotCasesSection.class)
                .get().getChildren().get(0).getChildren().get(0);

        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(callElement);

        final VariableProposalsProvider provider = new VariableProposalsProvider(suite, dataProvider);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.getProposals(text.getText(), text.getSelection().x, context);
        assertThat(proposals).hasSize(1);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("a${a_var}b");
    }
}
