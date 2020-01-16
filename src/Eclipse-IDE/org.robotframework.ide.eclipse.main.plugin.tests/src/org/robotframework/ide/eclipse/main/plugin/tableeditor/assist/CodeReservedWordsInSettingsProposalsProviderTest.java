/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

@ExtendWith({ ProjectExtension.class, FreshShellExtension.class })
public class CodeReservedWordsInSettingsProposalsProviderTest {

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
                "Library  DateTime",
                "Resource  res.robot",
                "*** Test Cases ***",
                "case",
                "  [documentation]",
                "  log",
                "  log");
    }

    @AfterAll
    public static void afterSuite() {
        RedPlugin.getModelManager().dispose();
    }

    @Test
    public void thereAreNoProposalsProvided_whenElementIsNotInSettings() {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "suite.robot"));

        final List<RobotElement> elements = getAllElements(suiteFile).stream()
                .filter(element -> !(element instanceof RobotSetting))
                .collect(Collectors.toList());
        final IRowDataProvider<Object> dataProvider = prepareElementsProvider(elements);
        final CodeReservedWordsInSettingsProposalsProvider provider = new CodeReservedWordsInSettingsProposalsProvider(
                null, dataProvider);

        for (int column = 0; column < 10; column++) {
            for (int row = 0; row < elements.size(); row++) {
                final AssistantContext context = new NatTableAssistantContext(column, row);
                final RedContentProposal[] proposals = provider.computeProposals("foo", 0, context);
                assertThat(proposals).isEmpty();
            }
        }
    }

    @Test
    public void thereAreNoProposalsProvided_whenElementIsInSettingsButColumnIsBeforeThird() {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "suite.robot"));

        final List<RobotElement> elements = getAllElements(suiteFile).stream()
                .filter(element -> element instanceof RobotSetting)
                .collect(Collectors.toList());
        final IRowDataProvider<Object> dataProvider = prepareElementsProvider(elements);
        final CodeReservedWordsInSettingsProposalsProvider provider = new CodeReservedWordsInSettingsProposalsProvider(
                null, dataProvider);

        for (int column = 0; column < 2; column++) {
            for (int row = 0; row < elements.size(); row++) {
                final AssistantContext context = new NatTableAssistantContext(column, row);
                final RedContentProposal[] proposals = provider.computeProposals("foo", 0, context);
                assertThat(proposals).isEmpty();
            }
        }
    }

    @Test
    public void thereAreNoProposalsProvided_whenElementIsInSettingsAndColumnIsThirdOneButInputDoesNotMatch() {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "suite.robot"));

        final List<RobotElement> elements = getAllElements(suiteFile).stream()
                .filter(element -> element instanceof RobotSetting)
                .collect(Collectors.toList());
        final IRowDataProvider<Object> dataProvider = prepareElementsProvider(elements);
        final CodeReservedWordsInSettingsProposalsProvider provider = new CodeReservedWordsInSettingsProposalsProvider(
                null, dataProvider);

        for (int row = 0; row < elements.size(); row++) {
            final AssistantContext context = new NatTableAssistantContext(2, row);
            final RedContentProposal[] proposals = provider.computeProposals("foo", 2, context);
            assertThat(proposals).isEmpty();
        }
    }

    @Test
    public void thereAreNoProposalsProvided_whenElementIsInSettingsAndColumnIsThirdOneButNotLibrarySetting() {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "suite.robot"));

        final List<RobotElement> elements = getAllElements(suiteFile).stream()
                .filter(element -> element instanceof RobotSetting)
                .collect(Collectors.toList());
        final IRowDataProvider<Object> dataProvider = prepareElementsProvider(elements);
        final CodeReservedWordsInSettingsProposalsProvider provider = new CodeReservedWordsInSettingsProposalsProvider(
                null, dataProvider);

        for (int row = 0; row < elements.size(); row++) {
            if (row == 0) { // skip Library setting
                continue;
            }
            final AssistantContext context = new NatTableAssistantContext(2, row);
            final RedContentProposal[] proposals = provider.computeProposals("foo", 0, context);
            assertThat(proposals).isEmpty();
        }
    }

    @Test
    public void thereAreProposalsProvided_whenInputIsMatchingAndProperContentIsInserted() throws Exception {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "suite.robot"));

        final List<RobotElement> elements = getAllElements(suiteFile).stream()
                .filter(element -> element instanceof RobotSetting)
                .collect(Collectors.toList());
        final IRowDataProvider<Object> dataProvider = prepareElementsProvider(elements);
        final CodeReservedWordsInSettingsProposalsProvider provider = new CodeReservedWordsInSettingsProposalsProvider(
                null, dataProvider);

        final Text text = new Text(shell, SWT.SINGLE);
        text.setText("with");

        final AssistantContext context = new NatTableAssistantContext(2, 0);
        final RedContentProposal[] proposals = provider.computeProposals(text.getText(), 4, context);
        assertThat(proposals).hasSize(1);
        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("WITH NAME");

        assertThat(proposals[0].getOperationsToPerformAfterAccepting()).hasSize(1);
    }

    @Test
    public void thereIsWithNameProposalProvided_whenInAtLeastThirdColumnAndCurrentSettingIsLibrary() {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "suite.robot"));

        final List<RobotElement> elements = getAllElements(suiteFile).stream()
                .filter(element -> element instanceof RobotSetting)
                .collect(Collectors.toList());
        final IRowDataProvider<Object> dataProvider = prepareElementsProvider(elements);
        final CodeReservedWordsInSettingsProposalsProvider provider = new CodeReservedWordsInSettingsProposalsProvider(
                null, dataProvider);

        for (int column = 0; column < 10; column++) {
            if (column < 2) { // we only test for at least third column
                continue;
            }
            final Text text = new Text(shell, SWT.SINGLE);
            text.setText("with xyz");

            final AssistantContext context = new NatTableAssistantContext(column, 0);
            final RedContentProposal[] proposals = provider.computeProposals(text.getText(), 4, context);
            assertThat(proposals).hasSize(1);

            proposals[0].getModificationStrategy().insert(text, proposals[0]);
            assertThat(text.getText()).isEqualTo("WITH NAME");

            assertThat(proposals[0].getOperationsToPerformAfterAccepting()).hasSize(1);
        }
    }

    @Test
    public void thereIsDisabledSettingProposalProvided_whenInExactlySecondColumnAndCurrentGeneralSettingIsKeywordBased() {
        final RobotExecutableRow<?> executableRow = new RobotExecutableRow<>();
        executableRow.setAction(RobotToken.create("Suite Setup", RobotTokenType.SETTING_SUITE_SETUP_DECLARATION));

        final IRowDataProvider<Object> dataProvider = prepareSettingProvider(new RobotSetting(null, executableRow));
        final CodeReservedWordsInSettingsProposalsProvider provider = new CodeReservedWordsInSettingsProposalsProvider(
                null, dataProvider);

        for (int column = 0; column < 10; column++) {
            if (column != 1) { // we only test for exactly second column
                continue;
            }
            final Text text = new Text(shell, SWT.SINGLE);
            text.setText("note");

            final AssistantContext context = new NatTableAssistantContext(column, 0);
            final RedContentProposal[] proposals = provider.computeProposals(text.getText(), 2, context);
            assertThat(proposals).hasSize(1);

            proposals[0].getModificationStrategy().insert(text, proposals[0]);
            assertThat(text.getText()).isEqualTo("NONE");

            assertThat(proposals[0].getOperationsToPerformAfterAccepting()).isEmpty();
        }
    }

    private static List<RobotElement> getAllElements(final RobotSuiteFile model) {
        final List<RobotElement> elements = new ArrayList<>();
        for (final RobotSuiteFileSection section : model.getSections()) {
            collectAllElements(elements, section);
        }
        assertThat(elements).isNotEmpty();
        return elements;
    }

    private static void collectAllElements(final List<RobotElement> elements, final RobotElement element) {
        elements.add(element);
        for (final RobotElement child : element.getChildren()) {
            collectAllElements(elements, child);
        }
    }

    private static IRowDataProvider<Object> prepareElementsProvider(final List<? extends RobotElement> elements) {
        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getColumnCount()).thenReturn(20);
        when(dataProvider.getDataValue(anyInt(), anyInt())).thenReturn("");
        int i = 0;
        for (final RobotElement element : elements) {
            when(dataProvider.getRowObject(i)).thenReturn(element);
            i++;
        }
        return dataProvider;
    }

    private static IRowDataProvider<Object> prepareSettingProvider(final RobotSetting setting) {
        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getColumnCount()).thenReturn(20);
        when(dataProvider.getDataValue(anyInt(), anyInt())).thenReturn("");
        final Entry<String, Object> entry = new SimpleEntry<>(setting.getName(), setting);
        when(dataProvider.getRowObject(anyInt())).thenReturn(entry);
        return dataProvider;
    }
}
