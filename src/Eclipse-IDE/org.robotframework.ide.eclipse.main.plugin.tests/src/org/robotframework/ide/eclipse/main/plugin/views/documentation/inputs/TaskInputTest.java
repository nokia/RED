/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.libraries.Documentation.DocFormat;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.red.junit.ProjectProvider;


public class TaskInputTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(TaskInputTest.class);

    @Test
    public void properTaskDocUriIsProvidedForInput() throws Exception {
        final RobotTask test = createTask("task");
        final TaskInput input = new TaskInput(test);

        final String fileUri = test.getSuiteFile().getFile().getLocationURI().toString();
        assertThat(input.getInputUri().toString()).isEqualTo(fileUri + "?show_doc=true&task=task");
    }

    @Test
    public void theInputContainsGivenTask() throws Exception {
        final RobotTask task = createTask("task");
        final TaskInput input = new TaskInput(task);

        assertThat(input.contains("task")).isFalse();
        assertThat(input.contains(task.getSuiteFile())).isFalse();
        assertThat(input.contains(task)).isTrue();
    }

    @Test
    public void properHtmlIsReturned_forTaskWithoutTemplate() throws Exception {
        final RobotRuntimeEnvironment env = mock(RobotRuntimeEnvironment.class);
        when(env.createHtmlDoc(any(String.class), eq(DocFormat.ROBOT))).thenReturn("doc");

        final RobotTask task = createTask("task");
        final TaskInput input = new TaskInput(task);

        final String fileUri = task.getSuiteFile().getFile().getLocationURI().toString();
        final String fileLabel = task.getSuiteFile().getFile().getFullPath().toString();

        final String html = input.provideHtml(env);
        assertThat(html).contains(fileUri + "?show_source=true&task=task\">" + fileLabel + "</a>");
        assertThat(html).contains(fileUri + "?show_doc=true&suite=\">Documentation</a>");

        assertThat(html).doesNotContainPattern("Template");
    }

    @Test
    public void properHtmlIsReturned_forTaskWithTemplate() throws Exception {
        final RobotRuntimeEnvironment env = mock(RobotRuntimeEnvironment.class);
        when(env.createHtmlDoc(any(String.class), eq(DocFormat.ROBOT))).thenReturn("doc");

        final RobotTask task = createTemplatedTask("task", "template kw");
        final TaskInput input = new TaskInput(task);

        final String fileUri = task.getSuiteFile().getFile().getLocationURI().toString();
        final String fileLabel = task.getSuiteFile().getFile().getFullPath().toString();

        final String html = input.provideHtml(env);
        assertThat(html).contains(fileUri + "?show_source=true&task=task\">" + fileLabel + "</a>");
        assertThat(html).contains(fileUri + "?show_doc=true&suite=\">Documentation</a>");

        assertThat(html).contains("Template", "template kw");
    }

    @Test
    public void properRawDocumentationIsReturned_forTaskWithoutTemplate() throws Exception {
        final RobotTask task = createTask("task");
        final TaskInput input = new TaskInput(task);

        final String fileLabel = task.getSuiteFile().getFile().getFullPath().toString();

        final String raw = input.provideRawText();
        assertThat(raw).contains("Name: task");
        assertThat(raw).contains("Source: " + fileLabel);

        assertThat(raw).doesNotContainPattern("Template");
    }

    @Test
    public void properRawDocumentationIsReturned_forTaskWithTemplate() throws Exception {
        final RobotTask task = createTemplatedTask("task", "template kw");
        final TaskInput input = new TaskInput(task);

        final String fileLabel = task.getSuiteFile().getFile().getFullPath().toString();

        final String raw = input.provideRawText();
        assertThat(raw).contains("Name: task");
        assertThat(raw).contains("Source: " + fileLabel);
        assertThat(raw).contains("Template: template kw");
    }

    private static RobotTask createTask(final String testName) throws Exception {
        final IFile file = projectProvider.createFile("suite.robot",
                "*** Tasks ***",
                testName,
                "  call  arg");
        final RobotModel model = new RobotModel();
        model.createRobotProject(projectProvider.getProject()).setRobotParserComplianceVersion(new RobotVersion(3, 1));
        return model.createSuiteFile(file).findSection(RobotTasksSection.class).get().getChildren().get(0);
    }

    private static RobotTask createTemplatedTask(final String testName, final String template) throws Exception {
        final IFile file = projectProvider.createFile("suite.robot",
                "*** Tasks ***",
                testName,
                "  [Template]  " + template,
                "  x  y");
        final RobotModel model = new RobotModel();
        model.createRobotProject(projectProvider.getProject()).setRobotParserComplianceVersion(new RobotVersion(3, 1));
        return model.createSuiteFile(file).findSection(RobotTasksSection.class).get().getChildren().get(0);
    }
}
