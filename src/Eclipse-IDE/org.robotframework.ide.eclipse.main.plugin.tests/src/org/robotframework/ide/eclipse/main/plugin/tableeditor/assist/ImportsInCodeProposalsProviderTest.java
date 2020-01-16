/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

@ExtendWith({ ProjectExtension.class, FreshShellExtension.class })
public class ImportsInCodeProposalsProviderTest {

    @Project
    static IProject project;

    @FreshShell
    Shell shell;

    private static RobotModel robotModel;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        robotModel = RedPlugin.getModelManager().getModel();

        createFile(project, "suite.robot",
                "*** Settings ***",
                "Resource  ares.robot",
                "Resource  bres.robot");
        createFile(project, "non_kw_based_settings.robot",
                "*** Test Cases ***",
                "tc",
                "  [Documentation]",
                "  [Tags]",
                "  [Timeout]");
        createFile(project, "kw_based_settings.robot",
                "*** Test Cases ***",
                "tc",
                "  [Setup]",
                "  [Teardown]",
                "  [Template]");
        createFile(project, "with_template.robot",
                "*** Test Cases ***",
                "tc",
                "  [Template]  Some Kw",
                "  Log  message",
                "  Kw Call  ",
                "  ");
        createFile(project, "without_template.robot",
                "*** Test Cases ***",
                "tc",
                "  [Template]  NONE",
                "  Log  message",
                "  Kw Call  ",
                "  ");
    }

    @AfterAll
    public static void afterSuite() {
        RedPlugin.getModelManager().dispose();
    }

    @Test
    public void thereAreNoProposalsProvided_whenSettingIsNotKeywordBased() {
        final RobotSuiteFile suiteFile = robotModel
                .createSuiteFile(getFile(project, "non_kw_based_settings.robot"));
        final List<RobotKeywordCall> settings = suiteFile.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren();

        final IRowDataProvider<Object> dataProvider = prepareDataProvider(settings);
        final ImportsInCodeProposalsProvider provider = new ImportsInCodeProposalsProvider(suiteFile, dataProvider);

        for (int row = 0; row < settings.size(); row++) {
            final AssistantContext context = new NatTableAssistantContext(1, row);
            assertThat(provider.computeProposals("foo", 0, context)).isNull();
        }
    }

    @Test
    public void thereAreProposalsProvided_whenSettingIsKeywordBased() {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "kw_based_settings.robot"));
        final List<RobotKeywordCall> settings = suiteFile.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren();

        final IRowDataProvider<Object> dataProvider = prepareDataProvider(settings);
        final ImportsInCodeProposalsProvider provider = new ImportsInCodeProposalsProvider(suiteFile, dataProvider);

        for (int row = 0; row < settings.size(); row++) {
            final AssistantContext context = new NatTableAssistantContext(1, row);
            assertThat(provider.computeProposals("foo", 0, context)).isNotNull();
        }
    }

    @Test
    public void thereAreNoProposalsProvidedInCode_whenTemplateIsUsed() {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "with_template.robot"));
        final List<RobotKeywordCall> nonSetting = suiteFile.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .stream()
                .filter(element -> !element.isLocalSetting())
                .collect(toList());

        final IRowDataProvider<Object> dataProvider = prepareDataProvider(nonSetting);
        final ImportsInCodeProposalsProvider provider = new ImportsInCodeProposalsProvider(suiteFile, dataProvider);

        for (int row = 0; row < nonSetting.size(); row++) {
            final AssistantContext context = new NatTableAssistantContext(1, row);
            assertThat(provider.computeProposals("foo", 0, context)).isNull();
        }
    }

    @Test
    public void thereAreProposalsProvidedInCode_whenTemplateIsNotUsed() {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "without_template.robot"));
        final List<RobotKeywordCall> nonSetting = suiteFile.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .stream()
                .filter(element -> !element.isLocalSetting())
                .collect(toList());

        final IRowDataProvider<Object> dataProvider = prepareDataProvider(nonSetting);
        final ImportsInCodeProposalsProvider provider = new ImportsInCodeProposalsProvider(suiteFile, dataProvider);

        for (int row = 0; row < nonSetting.size(); row++) {
            final AssistantContext context = new NatTableAssistantContext(1, row);
            assertThat(provider.computeProposals("foo", 0, context)).isNotNull();
        }
    }

    @Test
    public void thereAreNoProposalsProvided_whenThereIsNoImportMatchingCurrentInput() throws Exception {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "suite.robot"));

        final IRowDataProvider<Object> dataProvider = prepareDataProvider(new ArrayList<>());
        final ImportsInCodeProposalsProvider provider = new ImportsInCodeProposalsProvider(suiteFile, dataProvider);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.computeProposals("foo", 1, context);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreProposalsProvided_whenInputIsMatchingAndProperContentIsInserted() throws Exception {
        final Text text = new Text(shell, SWT.SINGLE);
        text.setText("abc");

        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "suite.robot"));

        final IRowDataProvider<Object> dataProvider = prepareDataProvider(new ArrayList<>());
        final ImportsInCodeProposalsProvider provider = new ImportsInCodeProposalsProvider(suiteFile, dataProvider);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.computeProposals(text.getText(), 1, context);
        assertThat(proposals).hasSize(1);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("ares.");
    }

    private static IRowDataProvider<Object> prepareDataProvider(final List<RobotKeywordCall> calls) {
        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        for (int i = 0; i < calls.size(); i++) {
            when(dataProvider.getRowObject(i)).thenReturn(calls.get(i));
        }
        return dataProvider;
    }
}
