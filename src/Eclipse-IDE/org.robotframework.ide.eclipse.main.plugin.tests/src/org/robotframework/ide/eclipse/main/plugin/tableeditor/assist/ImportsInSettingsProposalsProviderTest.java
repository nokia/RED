/*
 * Copyright 2017 Nokia Solutions and Networks
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

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;

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
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

@ExtendWith({ ProjectExtension.class, FreshShellExtension.class })
public class ImportsInSettingsProposalsProviderTest {

    @Project
    static IProject project;

    @FreshShell
    Shell shell;

    private static RobotModel robotModel;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        robotModel = RedPlugin.getModelManager().getModel();

        createFile(project, "kw_based_settings.robot",
                "*** Settings ***",
                "Suite Setup",
                "Suite Teardown",
                "Test Setup",
                "Test Teardown",
                "Test Template",
                "Task Setup",
                "Task Teardown",
                "Task Template",
                "Resource  res.robot");
        createFile(project, "non_kw_based_settings.robot",
                "*** Settings ***",
                "Library",
                "Resource",
                "Variables",
                "Metadata",
                "Test Timeout",
                "Task Timeout",
                "Force Tags",
                "Default Tags",
                "Resource  res.robot");
    }

    @AfterAll
    public static void afterSuite() {
        RedPlugin.getModelManager().dispose();
    }

    @Test
    public void thereAreNoProposalsProvided_whenColumnIsDifferentThanSecond() {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "kw_based_settings.robot"));
        final List<RobotKeywordCall> settings = suiteFile.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .stream()
                .filter(element -> !element.getName().equals("Resource"))
                .collect(toList());

        final IRowDataProvider<Object> dataProvider = prepareSettingsProvider(settings);
        final ImportsInSettingsProposalsProvider provider = new ImportsInSettingsProposalsProvider(suiteFile,
                dataProvider);

        for (int column = 0; column < 10; column++) {
            for (int row = 0; row < settings.size(); row++) {
                final AssistantContext context = new NatTableAssistantContext(column, row);
                final RedContentProposal[] proposals = provider.computeProposals("foo", 0, context);
                if (column == 1) {
                    assertThat(proposals).isNotNull();
                } else {
                    assertThat(proposals).isNull();
                }
            }
        }
    }

    @Test
    public void thereAreNoProposalsProvided_whenSettingIsNotKeywordBased() throws Exception {
        final RobotSuiteFile suiteFile = robotModel
                .createSuiteFile(getFile(project, "non_kw_based_settings.robot"));
        final List<RobotKeywordCall> settings = suiteFile.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .stream()
                .filter(element -> !element.getName().equals("Resource"))
                .collect(toList());

        final IRowDataProvider<Object> dataProvider = prepareSettingsProvider(settings);
        final ImportsInSettingsProposalsProvider provider = new ImportsInSettingsProposalsProvider(suiteFile,
                dataProvider);

        for (int row = 0; row < settings.size(); row++) {
            final AssistantContext context = new NatTableAssistantContext(1, row);
            assertThat(provider.computeProposals("foo", 0, context)).isNull();
        }
    }

    @Test
    public void thereAreNoProposalsProvided_whenThereIsNoKeywordMatchingCurrentInput() throws Exception {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "kw_based_settings.robot"));
        final List<RobotKeywordCall> settings = suiteFile.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .stream()
                .filter(element -> !element.getName().equals("Resource"))
                .collect(toList());

        final IRowDataProvider<Object> dataProvider = prepareSettingsProvider(settings);
        final ImportsInSettingsProposalsProvider provider = new ImportsInSettingsProposalsProvider(suiteFile,
                dataProvider);

        for (int row = 0; row < settings.size(); row++) {
            final AssistantContext context = new NatTableAssistantContext(1, row);
            final RedContentProposal[] proposals = provider.computeProposals("foo", 1, context);
            assertThat(proposals).isEmpty();
        }
    }

    @Test
    public void thereAreProposalsProvided_whenInputIsMatchingAndProperContentIsInserted() throws Exception {
        final Text text = new Text(shell, SWT.SINGLE);
        text.setText("rrr");

        final RobotSuiteFile suiteFile = robotModel
                .createSuiteFile(getFile(project, "kw_based_settings.robot"));

        final List<RobotKeywordCall> settings = suiteFile.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .stream()
                .filter(element -> !element.getName().equals("Resource"))
                .collect(toList());

        final IRowDataProvider<Object> dataProvider = prepareSettingsProvider(settings);
        final ImportsInSettingsProposalsProvider provider = new ImportsInSettingsProposalsProvider(suiteFile,
                dataProvider);

        for (int row = 0; row < settings.size(); row++) {
            final AssistantContext context = new NatTableAssistantContext(1, row);
            final RedContentProposal[] proposals = provider.computeProposals(text.getText(), 1, context);
            assertThat(proposals).hasSize(1);

            proposals[0].getModificationStrategy().insert(text, proposals[0]);
            assertThat(text.getText()).isEqualTo("res.");
        }
    }

    private static IRowDataProvider<Object> prepareSettingsProvider(final List<RobotKeywordCall> settings) {
        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        for (int i = 0; i < settings.size(); i++) {
            final RobotKeywordCall call = settings.get(i);
            final Entry<String, Object> entry = new SimpleEntry<>(call.getName(), call);
            when(dataProvider.getRowObject(i)).thenReturn(entry);
        }
        return dataProvider;
    }
}
