/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries.createRefLib;
import static org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries.createStdLib;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.KeywordUsagesFinder;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableConfigurationLabels;
import org.robotframework.red.junit.PreferenceUpdater;
import org.robotframework.red.junit.ProjectProvider;

public class KeywordArgumentsLabelAccumulatorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(KeywordArgumentsLabelAccumulatorTest.class);

    @Rule
    public PreferenceUpdater preferenceUpdater = new PreferenceUpdater();

    private static RobotModel robotModel;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("file.robot",
                "*** Test Cases ***",
                "case",
                "  ",
                "  # some  comment  only",
                "  Empty Args",
                "  Limited Args",
                "  Unlimited Args",
                "  Limited Args  1  2  3  4  5",
                "  Unlimited Args  1  2  3  #a  b  c",
                "  Unknown  1  2",
                "*** Settings ***",
                "Library  UserLib");
        projectProvider.createFile("for_loop.robot",
                "*** Test Cases ***",
                "case",
                "  FOR  ${element}  IN ENUMERATE  1  2  3",
                "    Limited Args  1  2",
                "  END",
                "*** Settings ***",
                "Library  UserLib");
        projectProvider.createFile("vars.robot",
                "*** Test Cases ***",
                "case",
                "  ${x}=",
                "  ${y}=  Limited Args  1",
                "*** Settings ***",
                "Library  UserLib");
        projectProvider.createFile("template.robot",
                "*** Test Cases ***",
                "case",
                "  [Template]  Limited Args",
                "  1  2  3  4",
                "*** Settings ***",
                "Library  UserLib");
        projectProvider.createFile("settings.robot",
                "*** Test Cases ***",
                "case",
                "  [Setup]  NONE",
                "  [Teardown]  Limited Args  1  2",
                "*** Settings ***",
                "Library  UserLib");
        projectProvider.createFile("nested.robot",
                "*** Test Cases ***",
                "case",
                "  Run Keyword",
                "  Run Keyword  Limited Args  1  2",
                "  Wait Until Keyword Succeeds  1  2  Limited Args  1",
                "  Run Keywords  Limited Args  1  AND  Limited Args  2  AND  Limited Args  3",
                "  Run Keyword If  c1  Limited Args  1  ELSE IF  c2  Limited Args  2  ELSE  Limited Args  3",
                "  Run Keyword If  c1  Limited Args  1  ELSE",
                "  Run Keyword  Run Keyword  Run Keyword  Limited Args  1  2",
                "  ${x}  Run Keyword  Limited Args  1",
                "  FOR  ${element}  IN ENUMERATE  1  2  3",
                "    Run Keyword  Limited Args  1  2",
                "  END",
                "  [Setup]  Run Keyword",
                "  [Teardown]  Repeat Keyword  5  Limited Args  1",
                "*** Settings ***",
                "Library  UserLib");
        projectProvider.createFile("embedded.robot",
                "*** Test Cases ***",
                "case",
                "  My ${x} Kw ${y} Xyz",
                "  [Teardown]  My 123 Kw 456 Xyz",
                "*** Keywords ***",
                "My ${a} Kw ${b} Xyz",
                "  Log Many  ${a}  ${b} ");

        robotModel = RedPlugin.getModelManager().getModel();
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());

        robotProject.setReferencedLibraries(createRefLib("UserLib", KeywordSpecification.create("Empty Args"),
                KeywordSpecification.create("Limited Args", "a", "b", "c", "d=10", "e=20"),
                KeywordSpecification.create("Unlimited Args", "a", "b", "c=123", "*list")));

        robotProject.setStandardLibraries(createStdLib("BuiltIn",
                KeywordSpecification.create("Run Keyword", "name", "*args"),
                KeywordSpecification.create("Run Keyword If", "condition", "name", "*args"),
                KeywordSpecification.create("Run Keywords", "*kws"),
                KeywordSpecification.create("Repeat Keyword", "repeat", "name", "*args"),
                KeywordSpecification.create("Wait Until Keyword Succeeds", "retry", "interval", "name", "*args")));

        projectProvider.configure();
    }

    @AfterClass
    public static void afterSuite() {
        RedPlugin.getModelManager().dispose();
        robotModel = null;
    }

    @Before
    public void before() {
        preferenceUpdater.setValue(RedPreferences.KEYWORD_ARGUMENTS_CELL_COLORING, true);
    }

    @Test
    public void labelsAreNotAdded_whenColoringIsDisabledInPreferences() {
        preferenceUpdater.setValue(RedPreferences.KEYWORD_ARGUMENTS_CELL_COLORING, false);

        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("file.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(2);

        for (int column = 1; column <= 10; column++) {
            assertThat(labelsAt(file, call, column)).isEmpty();
        }
    }

    @Test
    public void labelsAreNotAdded_forFirstColumn() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("file.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);

        assertThat(labelsAt(file, call, 0)).isEmpty();
    }

    @Test
    public void labelsAreNotAdded_forCaseDefinitionLine() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("file.robot"));
        final RobotCase robotCase = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0);

        for (int column = 1; column <= 10; column++) {
            assertThat(labelsAt(file, robotCase, column)).isEmpty();
        }
    }

    @Test
    public void labelsAreNotAdded_forEmptyLine() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("file.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);

        for (int column = 1; column <= 10; column++) {
            assertThat(labelsAt(file, call, column)).isEmpty();
        }
    }

    @Test
    public void labelsAreNotAdded_forEmptyLineWithComment() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("file.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(1);

        for (int column = 1; column <= 10; column++) {
            assertThat(labelsAt(file, call, column)).isEmpty();
        }
    }

    @Test
    public void labelsAreNotAdded_forUnknownKeyword() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("file.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(7);

        for (int column = 1; column <= 10; column++) {
            assertThat(labelsAt(file, call, column)).isEmpty();
        }
    }

    @Test
    public void labelsAreAdded_forKeywordWithoutArguments() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("file.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(2);

        for (int column = 1; column <= 10; column++) {
            assertThat(labelsAt(file, call, column))
                    .containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        }
    }

    @Test
    public void labelsAreAdded_forKeywordWithLimitedArgumentList() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("file.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(3);

        assertThat(labelsAt(file, call, 1)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 2)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 3)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 4)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 5)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 6)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 7)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 8)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
    }

    @Test
    public void labelsAreAdded_forKeywordWithUnlimitedArgumentList() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("file.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(4);

        assertThat(labelsAt(file, call, 1)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 2)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 3)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 4)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 5)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 6)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 7)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 8)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
    }

    @Test
    public void labelsAreAdded_forKeywordWithLimitedArgument_eventForNotEmptyOptionalArguments() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("file.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(5);

        assertThat(labelsAt(file, call, 1)).isEmpty();
        assertThat(labelsAt(file, call, 2)).isEmpty();
        assertThat(labelsAt(file, call, 3)).isEmpty();
        assertThat(labelsAt(file, call, 4)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 5)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 6)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 7)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 8)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
    }

    @Test
    public void labelsAreNotAdded_forCellsWithComments() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("file.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(6);

        assertThat(labelsAt(file, call, 1)).isEmpty();
        assertThat(labelsAt(file, call, 2)).isEmpty();
        assertThat(labelsAt(file, call, 3)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 4)).isEmpty();
        assertThat(labelsAt(file, call, 5)).isEmpty();
        assertThat(labelsAt(file, call, 6)).isEmpty();
        assertThat(labelsAt(file, call, 7)).isEmpty();
        assertThat(labelsAt(file, call, 8)).isEmpty();
    }

    @Test
    public void labelsAreNotAdded_forLoopDeclaration() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("for_loop.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);

        for (int column = 1; column <= 10; column++) {
            assertThat(labelsAt(file, call, column)).isEmpty();
        }
    }

    @Test
    public void labelsAreNotAdded_forLoopEnd() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("for_loop.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(2);

        for (int column = 1; column <= 10; column++) {
            assertThat(labelsAt(file, call, column)).isEmpty();
        }
    }

    @Test
    public void labelsAreAdded_forLoopBody() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("for_loop.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(1);

        assertThat(labelsAt(file, call, 1)).isEmpty();
        assertThat(labelsAt(file, call, 2)).isEmpty();
        assertThat(labelsAt(file, call, 3)).isEmpty();
        assertThat(labelsAt(file, call, 4)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 5)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 6)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 7)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 8)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
    }

    @Test
    public void labelsAreNotAdded_forEmptyVariableDeclaration() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("vars.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);

        for (int column = 1; column <= 10; column++) {
            assertThat(labelsAt(file, call, column)).isEmpty();
        }
    }

    @Test
    public void labelsAreAdded_forNotEmptyVariableDeclaration() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("vars.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(1);

        assertThat(labelsAt(file, call, 1)).isEmpty();
        assertThat(labelsAt(file, call, 2)).isEmpty();
        assertThat(labelsAt(file, call, 3)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 4)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 5)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 6)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 7)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 8)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
    }

    @Test
    public void labelsAreNotAdded_forTemplateSettingRow() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("template.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);

        for (int column = 1; column <= 10; column++) {
            assertThat(labelsAt(file, call, column)).isEmpty();
        }
    }

    @Test
    public void labelsAreNotAdded_forTemplateBodyRow() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("template.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(1);

        for (int column = 1; column <= 10; column++) {
            assertThat(labelsAt(file, call, column)).isEmpty();
        }
    }

    @Test
    public void labelsAreNotAdded_forDisabledKeywordBasedSetting() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("settings.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);

        for (int column = 1; column <= 10; column++) {
            assertThat(labelsAt(file, call, column)).isEmpty();
        }
    }

    @Test
    public void labelsAreAdded_forEnabledKeywordBasedSettings() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("settings.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(1);

        assertThat(labelsAt(file, call, 1)).isEmpty();
        assertThat(labelsAt(file, call, 2)).isEmpty();
        assertThat(labelsAt(file, call, 3)).isEmpty();
        assertThat(labelsAt(file, call, 4)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 5)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 6)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 7)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 8)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
    }

    @Test
    public void labelsAreAdded_forEmptyNestedKeywordCall() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("nested.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);

        assertThat(labelsAt(file, call, 1)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 2)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 3)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 4)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 5)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 6)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 7)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 8)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 9)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 10)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
    }

    @Test
    public void labelsAreAdded_forNotEmptyNestedKeywordCall() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("nested.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(1);

        assertThat(labelsAt(file, call, 1)).isEmpty();
        assertThat(labelsAt(file, call, 2)).isEmpty();
        assertThat(labelsAt(file, call, 3)).isEmpty();
        assertThat(labelsAt(file, call, 4)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 5)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 6)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 7)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 8)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 9)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 10)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
    }

    @Test
    public void labelsAreAdded_forNotEmptyNestedKeywordWithOmittedTokensCall() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("nested.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(2);

        assertThat(labelsAt(file, call, 1)).isEmpty();
        assertThat(labelsAt(file, call, 2)).isEmpty();
        assertThat(labelsAt(file, call, 3)).isEmpty();
        assertThat(labelsAt(file, call, 4)).isEmpty();
        assertThat(labelsAt(file, call, 5)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 6)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 7)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 8)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 9)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 10)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
    }

    @Test
    public void labelsAreAdded_forRunKeywordsCall() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("nested.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(3);

        assertThat(labelsAt(file, call, 1)).isEmpty();
        assertThat(labelsAt(file, call, 2)).isEmpty();
        assertThat(labelsAt(file, call, 3)).isEmpty();
        assertThat(labelsAt(file, call, 4)).isEmpty();
        assertThat(labelsAt(file, call, 5)).isEmpty();
        assertThat(labelsAt(file, call, 6)).isEmpty();
        assertThat(labelsAt(file, call, 7)).isEmpty();
        assertThat(labelsAt(file, call, 8)).isEmpty();
        assertThat(labelsAt(file, call, 9)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 10)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 11)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 12)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 13)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 14)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
    }

    @Test
    public void labelsAreAdded_forRunKeywordIfElseCall() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("nested.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(4);

        assertThat(labelsAt(file, call, 1)).isEmpty();
        assertThat(labelsAt(file, call, 2)).isEmpty();
        assertThat(labelsAt(file, call, 3)).isEmpty();
        assertThat(labelsAt(file, call, 4)).isEmpty();
        assertThat(labelsAt(file, call, 5)).isEmpty();
        assertThat(labelsAt(file, call, 6)).isEmpty();
        assertThat(labelsAt(file, call, 7)).isEmpty();
        assertThat(labelsAt(file, call, 8)).isEmpty();
        assertThat(labelsAt(file, call, 9)).isEmpty();
        assertThat(labelsAt(file, call, 10)).isEmpty();
        assertThat(labelsAt(file, call, 11)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 12)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 13)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 14)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 15)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 16)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
    }

    @Test
    public void labelsAreAdded_forIncorrectRunKeywordIfElseCall() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("nested.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(5);

        assertThat(labelsAt(file, call, 1)).isEmpty();
        assertThat(labelsAt(file, call, 2)).isEmpty();
        assertThat(labelsAt(file, call, 3)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 4)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 5)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 6)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 7)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 8)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 9)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 10)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
    }

    @Test
    public void labelsAreAdded_forMultipleNestedKeywordCall() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("nested.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(6);

        assertThat(labelsAt(file, call, 1)).isEmpty();
        assertThat(labelsAt(file, call, 2)).isEmpty();
        assertThat(labelsAt(file, call, 3)).isEmpty();
        assertThat(labelsAt(file, call, 4)).isEmpty();
        assertThat(labelsAt(file, call, 5)).isEmpty();
        assertThat(labelsAt(file, call, 6)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 7)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 8)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 9)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 10)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
    }

    @Test
    public void labelsAreAdded_forVarAssignmentWithNestedKeywordCall() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("nested.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(7);

        assertThat(labelsAt(file, call, 1)).isEmpty();
        assertThat(labelsAt(file, call, 2)).isEmpty();
        assertThat(labelsAt(file, call, 3)).isEmpty();
        assertThat(labelsAt(file, call, 4)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 5)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 6)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 7)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 8)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 9)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 10)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
    }

    @Test
    public void labelsAreAdded_forNestedKeywordCallInLoopBody() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("nested.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(9);

        assertThat(labelsAt(file, call, 1)).isEmpty();
        assertThat(labelsAt(file, call, 2)).isEmpty();
        assertThat(labelsAt(file, call, 3)).isEmpty();
        assertThat(labelsAt(file, call, 4)).isEmpty();
        assertThat(labelsAt(file, call, 5)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 6)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 7)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 8)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 9)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 10)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
    }

    @Test
    public void labelsAreAdded_forEmptyNestedKeywordCallInKeywordBasedSetting() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("nested.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(11);

        assertThat(labelsAt(file, call, 1)).isEmpty();
        assertThat(labelsAt(file, call, 2)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 3)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 4)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 5)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 6)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 7)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 8)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 9)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 10)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
    }

    @Test
    public void labelsAreAdded_forNotEmptyNestedKeywordCallInKeywordBasedSetting() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("nested.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(12);

        assertThat(labelsAt(file, call, 1)).isEmpty();
        assertThat(labelsAt(file, call, 2)).isEmpty();
        assertThat(labelsAt(file, call, 3)).isEmpty();
        assertThat(labelsAt(file, call, 4)).isEmpty();
        assertThat(labelsAt(file, call, 5)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 6)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 7)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 8)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 9)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 10)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
    }

    @Test
    public void labelsAreAdded_forEmbeddedKeywordCall() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("embedded.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);

        for (int column = 1; column <= 10; column++) {
            assertThat(labelsAt(file, call, column))
                    .containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        }
    }

    @Test
    public void labelsAreAdded_forEmbeddedKeywordCallInKeywordBasedSetting() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("embedded.robot"));
        final RobotKeywordCall call = file.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(1);

        assertThat(labelsAt(file, call, 1)).isEmpty();
        assertThat(labelsAt(file, call, 2)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 3)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 4)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 5)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 6)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 7)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 8)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 9)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, call, 10)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
    }

    private static List<String> labelsAt(final RobotSuiteFile file, final RobotFileInternalElement row,
            final int column) {
        final IRowDataProvider<?> dataProvider = createDataProvider(row);
        final KeywordUsagesFinder keywordFinder = createKeywordFinder(file);
        final KeywordArgumentsLabelAccumulator labelAccumulator = new KeywordArgumentsLabelAccumulator(dataProvider,
                keywordFinder);
        final LabelStack labels = new LabelStack();
        labelAccumulator.accumulateConfigLabels(labels, column, 0);
        return labels.getLabels();
    }

    private static IRowDataProvider<Object> createDataProvider(final RobotFileInternalElement row) {
        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(anyInt())).thenReturn(row);
        final List<RobotToken> tokens = ((AModelElement<?>) row.getLinkedElement()).getElementTokens();
        for (int column = 0; column < 20; column++) {
            final String value = column < tokens.size() ? tokens.get(column).getText() : "";
            when(dataProvider.getDataValue(eq(column), anyInt())).thenReturn(value);
        }
        return dataProvider;
    }

    private static KeywordUsagesFinder createKeywordFinder(final RobotSuiteFile file) {
        final KeywordUsagesFinder keywordFinder = new KeywordUsagesFinder(() -> file);
        keywordFinder.refresh().join();
        return keywordFinder;
    }
}
