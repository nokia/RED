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

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

import com.google.common.base.Predicate;

public class WithNameElementsProposalsProviderTest {

    @ClassRule
    public static final ProjectProvider projectProvider = new ProjectProvider(
            WithNameElementsProposalsProviderTest.class);

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  DateTime",
                "Resource  res.robot",
                "*** Test Cases ***",
                "case",
                "  [documentation]",
                "  log",
                "  log");
    }

    @Test
    public void thereAreNoProposalsProvided_whenElementIsNotInSettings() {
        final RobotSuiteFile suiteFile = RedPlugin.getModelManager()
                .createSuiteFile(projectProvider.getFile("suite.robot"));

        final Iterable<RobotElement> elements = filter(getAllElements(suiteFile), new Predicate<RobotElement>() {

            @Override
            public boolean apply(final RobotElement element) {
                return !(element instanceof RobotSetting);
            }
        });
        final IRowDataProvider<Object> dataProvider = prepareElementsProvider(elements);
        final WithNameElementsProposalsProvider provider = new WithNameElementsProposalsProvider(dataProvider);

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
    public void thereAreNoProposalsProvided_whenElementIsInSettingsButColumnIsBeforeThird() {
        final RobotSuiteFile suiteFile = RedPlugin.getModelManager()
                .createSuiteFile(projectProvider.getFile("suite.robot"));

        final Iterable<RobotSetting> elements = filter(getAllElements(suiteFile), RobotSetting.class);
        final IRowDataProvider<Object> dataProvider = prepareElementsProvider(elements);
        final WithNameElementsProposalsProvider provider = new WithNameElementsProposalsProvider(dataProvider);

        for (int column = 0; column < 2; column++) {

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
    public void thereAreNoProposalsProvided_whenElementIsInSettingsAndColumnIsThirdOneButPrefixDoesNotMatch() {
        final RobotSuiteFile suiteFile = RedPlugin.getModelManager()
                .createSuiteFile(projectProvider.getFile("suite.robot"));

        final Iterable<RobotSetting> elements = filter(getAllElements(suiteFile), RobotSetting.class);
        final IRowDataProvider<Object> dataProvider = prepareElementsProvider(elements);
        final WithNameElementsProposalsProvider provider = new WithNameElementsProposalsProvider(dataProvider);

        int row = 0;
        for (@SuppressWarnings("unused")
        final RobotElement element : elements) {
            final AssistantContext context = new NatTableAssistantContext(2, row);
            final RedContentProposal[] proposals = provider.getProposals("foo", 2, context);
            assertThat(proposals).isEmpty();

            row++;
        }
    }

    @Test
    public void thereAreNoProposalsProvided_whenElementIsInSettingsAndColumnIsThirdOneButNotLibrarySetting() {
        final RobotSuiteFile suiteFile = RedPlugin.getModelManager()
                .createSuiteFile(projectProvider.getFile("suite.robot"));

        final Iterable<RobotSetting> elements = filter(getAllElements(suiteFile), RobotSetting.class);
        final IRowDataProvider<Object> dataProvider = prepareElementsProvider(elements);
        final WithNameElementsProposalsProvider provider = new WithNameElementsProposalsProvider(dataProvider);

        int row = 0;
        for (@SuppressWarnings("unused")
        final RobotElement element : elements) {
            if (row == 0) { // skip Library setting
                row++;
                continue;
            }
            final AssistantContext context = new NatTableAssistantContext(2, row);
            final RedContentProposal[] proposals = provider.getProposals("foo", 0, context);
            assertThat(proposals).isEmpty();

            row++;
        }
    }

    @Test
    public void thereAreProposalsProvided_whenPrefixIsMatchingAndProperContentIsInserted() throws Exception {
        final RobotSuiteFile suiteFile = RedPlugin.getModelManager()
                .createSuiteFile(projectProvider.getFile("suite.robot"));

        final Iterable<RobotSetting> elements = filter(getAllElements(suiteFile), RobotSetting.class);
        final IRowDataProvider<Object> dataProvider = prepareElementsProvider(elements);
        final WithNameElementsProposalsProvider provider = new WithNameElementsProposalsProvider(dataProvider);

        final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
        text.setText("with");

        final AssistantContext context = new NatTableAssistantContext(2, 0);
        final RedContentProposal[] proposals = provider.getProposals(text.getText(), 4, context);
        assertThat(proposals).hasSize(1);
        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("WITH NAME");
    }

    @Test
    public void thereIsWithNameProposalProvided_whenInAtLeastThirdColumnAndCurrentSettingIsLibrary() {
        final RobotExecutableRow<?> executableRow = new RobotExecutableRow<>();
        executableRow.setAction(RobotToken.create("Library"));
        final RobotSetting libraryImport = new RobotSetting(null, RobotSetting.SettingsGroup.LIBRARIES, executableRow);

        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(libraryImport);
        final WithNameElementsProposalsProvider provider = new WithNameElementsProposalsProvider(dataProvider);

        for (int column = 0; column < 10; column++) {
            if (column == 0 || column == 1) { // we only test for at least third column
                continue;
            }
            final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
            text.setText("with xyz");

            final AssistantContext context = new NatTableAssistantContext(column, 0);
            final RedContentProposal[] proposals = provider.getProposals(text.getText(), 4, context);
            assertThat(proposals).hasSize(1);

            proposals[0].getModificationStrategy().insert(text, proposals[0]);
            assertThat(text.getText()).isEqualTo("WITH NAME");
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
