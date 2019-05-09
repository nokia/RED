/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationViewInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.KeywordDefinitionInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.KeywordDefinitionInput.KeywordDefinitionOnSettingInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.LibraryImportSettingInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.SuiteFileInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.SuiteFileInput.SuiteFileOnSettingInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.TaskInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.TaskInput.TaskOnSettingInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.TestCaseInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.TestCaseInput.TestCaseOnSettingInput;
import org.robotframework.red.junit.ProjectProvider;


public class DocumentationsTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(DocumentationsTest.class);

    @Test
    public void suiteFileInputIsFoundForRobotSuiteFile() throws Exception {
        final RobotSuiteFile suiteFile = new RobotSuiteFileCreator().build();

        final Optional<DocumentationViewInput> input = Documentations.findInput(suiteFile);
        assertThat(input).isPresent().containsInstanceOf(SuiteFileInput.class);
    }

    @Test
    public void suiteFileOnDocSettingInputIsFoundForSuiteDocumentationSetting() {
        final RobotSetting setting = new RobotSetting(null, new SuiteDocumentation(RobotToken.create("Documentation")));

        final Optional<DocumentationViewInput> input = Documentations.findInput(setting);
        assertThat(input).isPresent().containsInstanceOf(SuiteFileOnSettingInput.class);
    }

    @Test
    public void libraryImportOnSettingInputIsFoundForLibraryImportSetting() {
        final LibraryImport importedLib = new LibraryImport(RobotToken.create("Library"));
        final RobotSetting setting = new RobotSetting(null, SettingsGroup.LIBRARIES, importedLib);

        final Optional<DocumentationViewInput> input = Documentations.findInput(setting);
        assertThat(input).isPresent().containsInstanceOf(LibraryImportSettingInput.class);
    }

    @Test
    public void suiteFileInputIsFoundForResourceSettingImport() throws Exception {
        projectProvider.createFile("res.robot");
        final IFile suiteFile = projectProvider.createFile("suite.robot", "*** Settings ***", "Resource  res.robot");
        final RobotSuiteFile suite = RedPlugin.getModelManager().createSuiteFile(suiteFile);

        final RobotSetting setting = (RobotSetting) suite.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(0);

        final Optional<DocumentationViewInput> input = Documentations.findInput(setting);
        assertThat(input).isPresent().containsInstanceOf(SuiteFileInput.class);

        RedPlugin.getModelManager().dispose();
    }

    @Test
    public void noInputIsFoundForResourceSettingImport_whenImportedFileDoesNotExist() throws Exception {
        final IFile suiteFile = projectProvider.createFile("suite.robot", "*** Settings ***",
                "Resource  non_existing.robot");
        final RobotSuiteFile suite = RedPlugin.getModelManager().createSuiteFile(suiteFile);

        final RobotSetting setting = (RobotSetting) suite.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(0);

        assertThat(Documentations.findInput(setting)).isEmpty();

        RedPlugin.getModelManager().dispose();

    }

    @Test
    public void keywordDefinitionInputIsFoundForRobotKeywordDefinition() {
        final RobotKeywordDefinition keyword = new RobotKeywordDefinition(null,
                new UserKeyword(RobotToken.create("keyword")));

        final Optional<DocumentationViewInput> input = Documentations.findInput(keyword);
        assertThat(input).isPresent().containsInstanceOf(KeywordDefinitionInput.class);
    }

    @Test
    public void keywordOnDocSettingInputIsFoundForDocumentationSettingInRobotCase() {
        final RobotKeywordDefinition keyword = new RobotKeywordDefinition(null,
                new UserKeyword(RobotToken.create("keyword")));
        final RobotKeywordCall setting = new RobotKeywordCall(keyword,
                new LocalSetting<>(ModelType.USER_KEYWORD_DOCUMENTATION, RobotToken.create("[Documentation]")));

        final Optional<DocumentationViewInput> input = Documentations.findInput(setting);
        assertThat(input).isPresent().containsInstanceOf(KeywordDefinitionOnSettingInput.class);
    }

    @Test
    public void noInputIsFoundOnSomeKeywordSettingOtherThanDocumentation() {
        final RobotKeywordDefinition keyword = new RobotKeywordDefinition(null,
                new UserKeyword(RobotToken.create("keyword")));
        final RobotKeywordCall setting = new RobotKeywordCall(keyword,
                new LocalSetting<>(ModelType.USER_KEYWORD_ARGUMENTS, RobotToken.create("[Arguments]")));

        assertThat(Documentations.findInput(setting)).isEmpty();
    }

    @Test
    public void testCaseInputIsFoundForRobotCase() {
        final RobotCase testCase = new RobotCase(null, new TestCase(RobotToken.create("test")));

        final Optional<DocumentationViewInput> input = Documentations.findInput(testCase);
        assertThat(input).isPresent().containsInstanceOf(TestCaseInput.class);
    }

    @Test
    public void testCaseOnDocSettingInputIsFoundForDocumentationSettingInRobotCase() {
        final RobotCase testCase = new RobotCase(null, new TestCase(RobotToken.create("test")));
        final RobotKeywordCall setting = new RobotKeywordCall(testCase,
                new LocalSetting<>(ModelType.TEST_CASE_DOCUMENTATION, RobotToken.create("[Documentation]")));

        final Optional<DocumentationViewInput> input = Documentations.findInput(setting);
        assertThat(input).isPresent().containsInstanceOf(TestCaseOnSettingInput.class);
    }

    @Test
    public void noInputIsFoundOnSomeTestSettingOtherThanDocumentation() {
        final RobotCase testCase = new RobotCase(null, new TestCase(RobotToken.create("test")));
        final RobotKeywordCall setting = new RobotKeywordCall(testCase,
                new LocalSetting<>(ModelType.TEST_CASE_TEMPLATE, RobotToken.create("[Template]")));

        assertThat(Documentations.findInput(setting)).isEmpty();
    }

    @Test
    public void taskInputIsFoundForRobotTask() {
        final RobotTask task = new RobotTask(null, new Task(RobotToken.create("task")));

        final Optional<DocumentationViewInput> input = Documentations.findInput(task);
        assertThat(input).isPresent().containsInstanceOf(TaskInput.class);
    }

    @Test
    public void taskOnDocSettingInputIsFoundForDocumentationSettingInRobotTask() {
        final RobotTask task = new RobotTask(null, new Task(RobotToken.create("task")));
        final RobotKeywordCall setting = new RobotKeywordCall(task,
                new LocalSetting<>(ModelType.TASK_DOCUMENTATION, RobotToken.create("[Documentation]")));

        final Optional<DocumentationViewInput> input = Documentations.findInput(setting);
        assertThat(input).isPresent().containsInstanceOf(TaskOnSettingInput.class);
    }

    @Test
    public void noInputIsFoundOnSomeTaskSettingOtherThanDocumentation() {
        final RobotTask task = new RobotTask(null, new Task(RobotToken.create("task")));
        final RobotKeywordCall setting = new RobotKeywordCall(task,
                new LocalSetting<>(ModelType.TASK_TEMPLATE, RobotToken.create("[Template]")));

        assertThat(Documentations.findInput(setting)).isEmpty();
    }
}
