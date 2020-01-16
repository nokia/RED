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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedTextContentAdapter.SubstituteTextModificationStrategy;
import org.robotframework.red.junit.jupiter.BooleanPreference;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;
import org.robotframework.red.junit.jupiter.PreferencesExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

@ExtendWith({ ProjectExtension.class, PreferencesExtension.class, FreshShellExtension.class })
public class KeywordProposalsProviderTest {

    @Project
    static IProject project;

    @FreshShell
    Shell shell;

    private static RobotModel robotModel;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        robotModel = RedPlugin.getModelManager().getModel();

        createFile(project, "local_keywords_suite.robot",
                "*** Keywords ***",
                "kw1",
                "kw2");
        createFile(project, "imported_keywords_suite.robot",
                "*** Settings ***",
                "Library  LibImported");
        createFile(project, "keywords_with_args_suite.robot",
                "*** Keywords ***",
                "kw_no_args",
                "kw_with_args",
                "  [Arguments]  ${arg1}  ${arg2}");
        createFile(project, "keywords_with_args_in_setting_suite.robot",
                "*** Test Cases ***",
                "tc",
                "  [Setup]",
                "  [Teardown]",
                "  [Template]",
                "*** Keywords ***",
                "kw_no_args",
                "kw_with_args",
                "  [Arguments]  ${arg1}  ${arg2}");
        createFile(project, "keywords_with_embedded_args_suite.robot",
                "*** Keywords ***",
                "kw_no_arg",
                "kw_with_${arg}");
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
        final KeywordProposalsProvider provider = new KeywordProposalsProvider(suiteFile, dataProvider);

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
        final KeywordProposalsProvider provider = new KeywordProposalsProvider(suiteFile, dataProvider);

        for (int row = 0; row < settings.size(); row++) {
            final AssistantContext context = new NatTableAssistantContext(1, row);
            assertThat(provider.computeProposals("foo", 0, context)).isNotNull();
        }
    }

    @Test
    public void thereAreNoProposalsProvidedInCode_whenTemplateIsUsed() {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "with_template.robot"));
        final List<RobotKeywordCall> nonSettings = suiteFile.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .stream()
                .filter(element -> !element.isLocalSetting())
                .collect(toList());

        final IRowDataProvider<Object> dataProvider = prepareDataProvider(nonSettings);
        final KeywordProposalsProvider provider = new KeywordProposalsProvider(suiteFile, dataProvider);

        for (int row = 0; row < nonSettings.size(); row++) {
            final AssistantContext context = new NatTableAssistantContext(1, row);
            assertThat(provider.computeProposals("foo", 0, context)).isNull();
        }
    }

    @Test
    public void thereAreProposalsProvidedInCode_whenTemplateIsNotUsed() {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "without_template.robot"));
        final List<RobotKeywordCall> nonSettings = suiteFile.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .stream()
                .filter(element -> !element.isLocalSetting())
                .collect(toList());

        final IRowDataProvider<Object> dataProvider = prepareDataProvider(nonSettings);
        final KeywordProposalsProvider provider = new KeywordProposalsProvider(suiteFile, dataProvider);

        for (int row = 0; row < nonSettings.size(); row++) {
            final AssistantContext context = new NatTableAssistantContext(1, row);
            assertThat(provider.computeProposals("foo", 0, context)).isNotNull();
        }
    }

    @Test
    public void thereAreNoProposalsProvided_whenThereIsNoKeywordMatchingCurrentInput() throws Exception {
        final RobotSuiteFile suiteFile = robotModel
                .createSuiteFile(getFile(project, "local_keywords_suite.robot"));
        final IRowDataProvider<Object> dataProvider = prepareDataProvider(new ArrayList<>());
        final KeywordProposalsProvider provider = new KeywordProposalsProvider(suiteFile, dataProvider);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.computeProposals("foo", 1, context);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreProposalsProvided_whenInputIsMatchingAndProperContentIsInserted() throws Exception {
        final Text text = new Text(shell, SWT.SINGLE);
        text.setText("foo");

        final RobotSuiteFile suiteFile = robotModel
                .createSuiteFile(getFile(project, "local_keywords_suite.robot"));
        final IRowDataProvider<Object> dataProvider = prepareDataProvider(new ArrayList<>());
        final KeywordProposalsProvider provider = new KeywordProposalsProvider(suiteFile, dataProvider);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.computeProposals(text.getText(), 0, context);
        assertThat(proposals).hasSize(2);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("kw1");
    }

    @BooleanPreference(key = RedPreferences.ASSISTANT_KEYWORD_FROM_NOT_IMPORTED_LIBRARY_ENABLED, value = true)
    @Test
    public void thereAreOperationsToPerformAfterAccepting_onlyForNotAccessibleKeywordProposals() throws Exception {
        final Map<LibraryDescriptor, LibrarySpecification> refLibs = new LinkedHashMap<>();
        refLibs.putAll(Libraries.createRefLib("LibImported", "kw1", "kw2"));
        refLibs.putAll(Libraries.createRefLib("LibNotImported", "kw3", "kw4"));

        final RobotProject robotProject = robotModel.createRobotProject(project);
        robotProject.setReferencedLibraries(refLibs);

        final RobotSuiteFile suiteFile = robotModel
                .createSuiteFile(getFile(project, "imported_keywords_suite.robot"));
        final IRowDataProvider<Object> dataProvider = prepareDataProvider(new ArrayList<>());
        final KeywordProposalsProvider provider = new KeywordProposalsProvider(suiteFile, dataProvider);

        final AssistantContext context = new NatTableAssistantContext(1, 3);
        final RedContentProposal[] proposals = provider.computeProposals("kw", 2, context);
        assertThat(proposals).hasSize(4);

        assertThat(proposals[0].getLabel()).isEqualTo("kw1 - LibImported");
        assertThat(proposals[0].getOperationsToPerformAfterAccepting()).isEmpty();
        assertThat(proposals[1].getLabel()).isEqualTo("kw2 - LibImported");
        assertThat(proposals[1].getOperationsToPerformAfterAccepting()).isEmpty();
        assertThat(proposals[2].getLabel()).isEqualTo("kw3 - LibNotImported");
        assertThat(proposals[2].getOperationsToPerformAfterAccepting()).hasSize(1);
        assertThat(proposals[3].getLabel()).isEqualTo("kw4 - LibNotImported");
        assertThat(proposals[3].getOperationsToPerformAfterAccepting()).hasSize(1);
    }

    @Test
    public void thereAreOperationsToPerformAfterAccepting_onlyForKeywordsWithArguments() throws Exception {
        final RobotSuiteFile suiteFile = robotModel
                .createSuiteFile(getFile(project, "keywords_with_args_suite.robot"));
        final IRowDataProvider<Object> dataProvider = prepareDataProvider(new ArrayList<>());
        final KeywordProposalsProvider provider = new KeywordProposalsProvider(suiteFile, dataProvider);

        final AssistantContext context = new NatTableAssistantContext(1, 3);
        final RedContentProposal[] proposals = provider.computeProposals("kw", 2, context);
        assertThat(proposals).hasSize(2);

        assertThat(proposals[0].getLabel()).isEqualTo("kw_no_args - keywords_with_args_suite.robot");
        assertThat(proposals[0].getOperationsToPerformAfterAccepting()).isEmpty();
        assertThat(proposals[1].getLabel()).isEqualTo("kw_with_args - keywords_with_args_suite.robot");
        assertThat(proposals[1].getOperationsToPerformAfterAccepting()).hasSize(1);
    }

    @Test
    public void thereAreOperationsToPerformAfterAccepting_onlyForKeywordsWithArgumentsAndKeywordBasedSettingIsNotTemplate()
            throws Exception {
        final RobotSuiteFile suiteFile = robotModel
                .createSuiteFile(getFile(project, "keywords_with_args_in_setting_suite.robot"));
        final List<RobotKeywordCall> settings = suiteFile.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren();

        final IRowDataProvider<Object> dataProvider = prepareDataProvider(settings);
        final KeywordProposalsProvider provider = new KeywordProposalsProvider(suiteFile, dataProvider);

        for (int row = 0; row < settings.size(); row++) {
            final AssistantContext context = new NatTableAssistantContext(1, row);
            final RedContentProposal[] proposals = provider.computeProposals("kw", 2, context);
            assertThat(proposals).hasSize(2);

            assertThat(proposals[0].getLabel())
                    .isEqualTo("kw_no_args - keywords_with_args_in_setting_suite.robot");
            assertThat(proposals[0].getOperationsToPerformAfterAccepting()).isEmpty();
            assertThat(proposals[1].getLabel())
                    .isEqualTo("kw_with_args - keywords_with_args_in_setting_suite.robot");
            if (settings.get(row).getLinkedElement().getDeclaration().getText().equals("[Template]")) {
                assertThat(proposals[1].getOperationsToPerformAfterAccepting()).isEmpty();
            } else {
                assertThat(proposals[1].getOperationsToPerformAfterAccepting()).hasSize(1);
            }
        }
    }

    @Test
    public void keywordsWithEmbeddedArgumentsShouldBeInsertedWithoutCommitting() throws Exception {
        final RobotSuiteFile suiteFile = robotModel
                .createSuiteFile(getFile(project, "keywords_with_embedded_args_suite.robot"));
        final IRowDataProvider<Object> dataProvider = prepareDataProvider(new ArrayList<>());
        final KeywordProposalsProvider provider = new KeywordProposalsProvider(suiteFile, dataProvider);

        final AssistantContext context = new NatTableAssistantContext(1, 3);
        final RedContentProposal[] proposals = provider.computeProposals("kw", 2, context);
        assertThat(proposals).hasSize(2);

        assertThat(proposals[0].getLabel()).isEqualTo("kw_no_arg - keywords_with_embedded_args_suite.robot");
        assertThat(proposals[0].getModificationStrategy()).isInstanceOfSatisfying(
                SubstituteTextModificationStrategy.class,
                strategy -> assertThat(strategy.shouldCommitAfterInsert()).isTrue());
        assertThat(proposals[1].getLabel()).isEqualTo("kw_with_${arg} - keywords_with_embedded_args_suite.robot");
        assertThat(proposals[1].getModificationStrategy()).isInstanceOfSatisfying(
                SubstituteTextModificationStrategy.class,
                strategy -> assertThat(strategy.shouldCommitAfterInsert()).isFalse());
    }

    @Test
    public void emptyArgumentsAreFilteredOut() throws Exception {
        final RedKeywordProposal proposedKeyword = mock(RedKeywordProposal.class);
        when(proposedKeyword.getContent()).thenReturn("name");
        when(proposedKeyword.getArguments()).thenReturn(Arrays.asList("a1", "a2", ""));

        assertThat(KeywordProposalsProvider.getValuesToInsert(proposedKeyword)).containsExactly("name", "a1", "a2");
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
