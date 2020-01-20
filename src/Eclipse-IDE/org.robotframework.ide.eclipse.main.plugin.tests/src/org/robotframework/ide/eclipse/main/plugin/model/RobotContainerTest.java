/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createDir;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import org.eclipse.core.resources.IProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class RobotContainerTest {

    @Project
    IProject project;

    @Test
    public void emptyResultIsReturned_whenInitFileDoesNotExist() throws Exception {
        createDir(project, "inner");
        createFile(project, "test.robot");

        final RobotContainer container = createContainer();

        assertThat(container.getInitFileModel()).isNotPresent();
    }

    @Test
    public void correctRobotInitFileModelIsReturned() throws Exception {
        createFile(project, "__init__.robot");
        createFile(project, "__init__.tsv");
        createFile(project, "__init__.txt");

        final RobotContainer container = createContainer();

        assertThat(container.getInitFileModel()).hasValueSatisfying(
                file -> assertThat(file.getFile()).isEqualTo(getFile(project, "__init__.robot")));
    }

    @Test
    public void correctTsvInitFileModelIsReturned() throws Exception {
        createFile(project, "__init__.tsv");
        createFile(project, "__init__.txt");

        final RobotContainer container = createContainer();

        assertThat(container.getInitFileModel()).hasValueSatisfying(
                file -> assertThat(file.getFile()).isEqualTo(getFile(project, "__init__.tsv")));
    }

    @Test
    public void correctTxtInitFileModelIsReturned() throws Exception {
        createFile(project, "__init__.txt");

        final RobotContainer container = createContainer();

        assertThat(container.getInitFileModel()).hasValueSatisfying(
                file -> assertThat(file.getFile()).isEqualTo(getFile(project, "__init__.txt")));
    }

    @Test
    public void correctInitFileModelIsReturned_whenFileNameIsInUpperCase() throws Exception {
        createFile(project, "__INIT__.robot");

        final RobotContainer container = createContainer();

        assertThat(container.getInitFileModel()).hasValueSatisfying(
                file -> assertThat(file.getFile()).isEqualTo(getFile(project, "__INIT__.robot")));
    }

    private RobotContainer createContainer() {
        final RobotProject robotProject = new RobotModel().createRobotProject(project);
        return new RobotContainer(robotProject, project) {
            // nothing to implement
        };
    }
}
