/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.environment.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeElementsColumnsPropertyAccessor;
import org.robotframework.red.junit.jupiter.IntegerPreference;
import org.robotframework.red.junit.jupiter.PreferencesExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith({ ProjectExtension.class, PreferencesExtension.class })
public class TasksDataProviderTest {

    @Project
    static IProject project;

    private final TasksDataProvider dataProvider = new TasksDataProvider(
            new CodeElementsColumnsPropertyAccessor(null, null), null);

    @Test
    public void columnsAreCountedCorrectly_whenTasksSectionIsEmpty() throws Exception {
        dataProvider.setInput(createTasksSection("*** Tasks ***"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(6);
    }

    @Test
    public void columnsAreCountedCorrectly_whenTaskIsEmpty() throws Exception {
        dataProvider.setInput(createTasksSection("*** Tasks ***",
                "task"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(6);
    }

    @Test
    public void columnsAreCountedCorrectly_whenTaskSettingArgumentsDoNotExceedLimit() throws Exception {
        dataProvider.setInput(createTasksSection("*** Tasks ***",
                "task",
                "  [Tags]    a    b    c    d"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(6);
    }

    @Test
    public void columnsAreCountedCorrectly_whenTaskSettingArgumentsExceedLimit() throws Exception {
        dataProvider.setInput(createTasksSection("*** Tasks ***",
                "task",
                "  [Tags]    a    b    c    d    e    f    g"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(9);
    }

    @Test
    public void columnsAreCountedCorrectly_whenTaskKeywordCallArgumentsDoNotExceedLimit() throws Exception {
        dataProvider.setInput(createTasksSection("*** Tasks ***",
                "task",
                "  Log Many    a    b    c    d"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(6);
    }

    @Test
    public void columnsAreCountedCorrectly_whenTaskKeywordCallArgumentsExceedLimit() throws Exception {
        dataProvider.setInput(createTasksSection("*** Tasks ***",
                "task",
                "  Log Many    a    b    c    d    e    f"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(8);
    }

    @Test
    public void columnsAreCountedCorrectly_whenTaskDocumentationExceedsLimit() throws Exception {
        dataProvider.setInput(createTasksSection("*** Tasks ***",
                "task",
                "  [Documentation]    a    b    c    d    e    f    a    b    c    d    e    f"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(6);
    }

    @Test
    public void columnsAreCountedCorrectly_whenTaskSectionContainsManyTasks() throws Exception {
        dataProvider.setInput(createTasksSection("*** Tasks ***",
                "task 1",
                "  Log Many    ${a}    ${b}    ${c}    ${d}    ${e}",
                "task 2",
                "  [Setup]    Log Many    ${a}    ${b}    ${c}    ${d}    ${e}",
                "  Log Many    message",
                "task 3",
                "  Log Many    ${a}    ${b}    ${c}    ${d}    ${e}    ${f}    ${g}    ${h}",
                "task 4",
                "  Log    message",
                "task 5",
                "  [Documentation]    a    b    c    d    e    f    a    b    c    d    e    f",
                "  Log    message"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(10);
    }

    @IntegerPreference(key = RedPreferences.MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS, value = 15)
    @Test
    public void columnsAreCountedCorrectly_whenMinimalArgumentsColumnsFieldIsChangedInPreferences() throws Exception {
        dataProvider.setInput(createTasksSection("*** Tasks ***",
                "task",
                "  Log Many    ${a}    ${b}    ${c}"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(16);
    }

    private RobotTasksSection createTasksSection(final String... lines) throws Exception {
        final IFile file = createFile(project, "file.robot", lines);
        final RobotModel model = new RobotModel();
        model.createRobotProject(project).setRobotParserComplianceVersion(new RobotVersion(3, 1));
        return model.createSuiteFile(file).findSection(RobotTasksSection.class).get();
    }
}
