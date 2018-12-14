/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.resources.IProject;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.junit.ProjectProvider;

public class RobotContainerTest {

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(RobotContainerTest.class);

    @Test
    public void emptyResultIsReturned_whenInitFileDoesNotExist() throws Exception {
        projectProvider.createDir("inner");
        projectProvider.createFile("test.robot");

        final RobotContainer container = createContainer();

        assertThat(container.getInitFileModel()).isNotPresent();
    }

    @Test
    public void correctRobotInitFileModelIsReturned() throws Exception {
        projectProvider.createFile("__init__.robot");
        projectProvider.createFile("__init__.tsv");
        projectProvider.createFile("__init__.txt");

        final RobotContainer container = createContainer();

        assertThat(container.getInitFileModel()).hasValueSatisfying(
                file -> assertThat(file.getFile()).isEqualTo(projectProvider.getFile("__init__.robot")));
    }

    @Test
    public void correctTsvInitFileModelIsReturned() throws Exception {
        projectProvider.createFile("__init__.tsv");
        projectProvider.createFile("__init__.txt");

        final RobotContainer container = createContainer();

        assertThat(container.getInitFileModel()).hasValueSatisfying(
                file -> assertThat(file.getFile()).isEqualTo(projectProvider.getFile("__init__.tsv")));
    }

    @Test
    public void correctTxtInitFileModelIsReturned() throws Exception {
        projectProvider.createFile("__init__.txt");

        final RobotContainer container = createContainer();

        assertThat(container.getInitFileModel()).hasValueSatisfying(
                file -> assertThat(file.getFile()).isEqualTo(projectProvider.getFile("__init__.txt")));
    }

    @Test
    public void correctInitFileModelIsReturned_whenFileNameIsInUpperCase() throws Exception {
        projectProvider.createFile("__INIT__.robot");

        final RobotContainer container = createContainer();

        assertThat(container.getInitFileModel()).hasValueSatisfying(
                file -> assertThat(file.getFile()).isEqualTo(projectProvider.getFile("__INIT__.robot")));
    }

    private RobotContainer createContainer() {
        final IProject project = projectProvider.getProject();
        final RobotProject robotProject = new RobotModel().createRobotProject(project);
        return new RobotContainer(robotProject, project) {
            // nothing to implement
        };
    }
}
