/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static com.google.common.collect.Iterables.filter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

public class CodeReservedElementsProposalsProviderTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(
            CodeReservedElementsProposalsProviderTest.class);

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    private static RobotModel robotModel;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        robotModel = RedPlugin.getModelManager().getModel();

        projectProvider.createFile("suite.robot",
                "*** Test Cases ***",
                "case",
                "  [documentation]",
                "  log",
                "  log",
                "*** Keywords ***",
                "keyword",
                "  [arguments]",
                "  log",
                "  log");
    }

    @AfterClass
    public static void afterSuite() {
        RedPlugin.getModelManager().dispose();
    }

    @Test
    public void thereAreNoProposalsProvided_whenElementIsNotKeywordCall() {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));

        final Iterable<RobotElement> elements = getAllElements(suiteFile).stream()
                .filter(element -> !(element instanceof RobotKeywordCall))
                .collect(Collectors.toList());
        final IRowDataProvider<Object> dataProvider = prepareElementsProvider(elements);
        final CodeReservedElementsProposalsProvider provider = new CodeReservedElementsProposalsProvider(null,
                dataProvider);

        for (int column = 0; column < 10; column++) {
            int row = 0;
            for (@SuppressWarnings("unused")
            final RobotElement element : elements) {
                final AssistantContext context = new NatTableAssistantContext(column, row);
                final RedContentProposal[] proposals = provider.getProposals("foo", 0, context);
                assertThat(proposals).isEmpty();

                row++;
            }
        }
    }

    @Test
    public void thereAreNoProposalsProvided_whenElementIsKeywordCallButColumnIsDifferentThanFirst() {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));

        final Iterable<RobotKeywordCall> elements = filter(getAllElements(suiteFile), RobotKeywordCall.class);
        final IRowDataProvider<Object> dataProvider = prepareElementsProvider(elements);
        final CodeReservedElementsProposalsProvider provider = new CodeReservedElementsProposalsProvider(null,
                dataProvider);

        for (int column = 0; column < 10; column++) {
            if (column == 0) { // we're ommiting first column
                continue;
            }

            int row = 0;
            for (@SuppressWarnings("unused")
            final RobotElement element : elements) {
                final AssistantContext context = new NatTableAssistantContext(column, row);
                final RedContentProposal[] proposals = provider.getProposals("foo", 0, context);
                assertThat(proposals).isEmpty();

                row++;
            }
        }
    }

    @Test
    public void thereAreNoProposalsProvided_whenElementIsKeywordCallAndColumnIsFirstOneButInputDoesNotMatch() {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));

        final Iterable<RobotKeywordCall> elements = filter(getAllElements(suiteFile), RobotKeywordCall.class);
        final IRowDataProvider<Object> dataProvider = prepareElementsProvider(elements);
        final CodeReservedElementsProposalsProvider provider = new CodeReservedElementsProposalsProvider(null,
                dataProvider);

        int row = 0;
        for (@SuppressWarnings("unused")
        final RobotElement element : elements) {
            final AssistantContext context = new NatTableAssistantContext(0, row);
            final RedContentProposal[] proposals = provider.getProposals("other", 2, context);
            assertThat(proposals).isEmpty();

            row++;
        }
    }

    @Test
    public void thereIsForProposalProvided_whenInFirstColumnAndCurrentInputMatchesToForWord() {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));

        final Iterable<RobotKeywordCall> elements = filter(getAllElements(suiteFile), RobotKeywordCall.class);
        final IRowDataProvider<Object> dataProvider = prepareElementsProvider(elements);
        final CodeReservedElementsProposalsProvider provider = new CodeReservedElementsProposalsProvider(null,
                dataProvider);

        int row = 0;
        for (@SuppressWarnings("unused")
        final RobotElement element : elements) {
            final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
            text.setText(":fx");

            final AssistantContext context = new NatTableAssistantContext(0, row);
            final RedContentProposal[] proposals = provider.getProposals(text.getText(), 2, context);
            assertThat(proposals).hasSize(1);

            proposals[0].getModificationStrategy().insert(text, proposals[0]);
            assertThat(text.getText()).isEqualTo(":FOR");

            row++;
        }
    }

    @Test
    public void thereIsBddKeywordProposalProvided_whenInFirstColumnAndCurrentInputMatchesToGivenWord() {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));

        final Iterable<RobotKeywordCall> elements = filter(getAllElements(suiteFile), RobotKeywordCall.class);
        final IRowDataProvider<Object> dataProvider = prepareElementsProvider(elements);
        final CodeReservedElementsProposalsProvider provider = new CodeReservedElementsProposalsProvider(null,
                dataProvider);

        int row = 0;
        for (@SuppressWarnings("unused")
        final RobotElement element : elements) {
            final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
            text.setText("Gib");

            final AssistantContext context = new NatTableAssistantContext(0, row);
            final RedContentProposal[] proposals = provider.getProposals(text.getText(), 2, context);
            assertThat(proposals).hasSize(1);

            proposals[0].getModificationStrategy().insert(text, proposals[0]);
            assertThat(text.getText()).isEqualTo("Given ");

            row++;
        }
    }

    @Test
    public void thereAreInProposalProvided_whenInAtLeastThirdColumnAndCurrentKeywordIsForLoop() {
        final RobotExecutableRow<?> executableRow = new RobotExecutableRow<>();
        executableRow.setAction(RobotToken.create(":FOR"));
        final RobotKeywordCall forExecutable = new RobotKeywordCall(null, executableRow);

        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(forExecutable);
        final CodeReservedElementsProposalsProvider provider = new CodeReservedElementsProposalsProvider(null,
                dataProvider);

        for (int column = 0; column < 10; column++) {
            if (column == 0 || column == 1) { // we only test for at least third column
                continue;
            }
            final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
            text.setText("in rxyz");

            final AssistantContext context = new NatTableAssistantContext(column, 0);
            final RedContentProposal[] proposals = provider.getProposals(text.getText(), 4, context);
            assertThat(proposals).hasSize(1);

            proposals[0].getModificationStrategy().insert(text, proposals[0]);
            assertThat(text.getText()).isEqualTo("IN RANGE");
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

    private static IRowDataProvider<Object> prepareElementsProvider(final Iterable<? extends RobotElement> elements) {
        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        int i = 0;
        for (final RobotElement element : elements) {
            when(dataProvider.getRowObject(i)).thenReturn(element);
            i++;
        }
        return dataProvider;
    }
}
