/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.robotframework.red.junit.jupiter.ProjectExtension.configure;

import java.util.function.Predicate;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.rf.ide.core.project.RobotProjectConfigWriter;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class RobotProjectNatureTest {

    @Project
    static IProject project;

    private final IProgressMonitor monitor = new NullProgressMonitor();

    @BeforeEach
    public void before() throws Exception {
        final IProjectDescription desc = project.getDescription();
        desc.setNatureIds(new String[0]);
        project.setDescription(desc, monitor);

        project.getFile(RobotProjectConfig.FILENAME).delete(true, null);
    }

    @Test
    public void robotNatureIsSetAndDefaultConfigIsCreated_whenDoesNotExist() throws Exception {
        @SuppressWarnings("unchecked")
        final Predicate<String> shouldReplaceConfig = mock(Predicate.class);
        RobotProjectNature.addRobotNature(project, monitor, shouldReplaceConfig);

        assertThat(RobotProjectNature.hasRobotNature(project)).isTrue();

        final IFile cfgFile = project.getFile(RobotProjectConfig.FILENAME);
        assertThat(cfgFile.exists()).isTrue();
        assertThat(cfgFile.getContents())
                .hasSameContentAs(new RobotProjectConfigWriter().writeConfiguration(RobotProjectConfig.create()));

        verifyNoInteractions(shouldReplaceConfig);
    }

    @Test
    public void robotNatureIsSetAndExistingConfigIsReplaced() throws Exception {
        final RobotProjectConfig config = RobotProjectConfig.create();
        config.addVariableMapping(VariableMapping.create("${xyz}", "abc"));
        configure(project, config);

        RobotProjectNature.addRobotNature(project, monitor, p -> true);

        assertThat(RobotProjectNature.hasRobotNature(project)).isTrue();

        final IFile cfgFile = project.getFile(RobotProjectConfig.FILENAME);
        assertThat(cfgFile.exists()).isTrue();
        assertThat(cfgFile.getContents())
                .hasSameContentAs(new RobotProjectConfigWriter().writeConfiguration(RobotProjectConfig.create()));
    }

    @Test
    public void robotNatureIsSetAndExistingConfigIsNotReplaced() throws Exception {
        final RobotProjectConfig config = RobotProjectConfig.create();
        config.addVariableMapping(VariableMapping.create("${xyz}", "abc"));
        configure(project, config);

        RobotProjectNature.addRobotNature(project, monitor, p -> false);

        assertThat(RobotProjectNature.hasRobotNature(project)).isTrue();

        final IFile cfgFile = project.getFile(RobotProjectConfig.FILENAME);
        assertThat(cfgFile.exists()).isTrue();
        assertThat(cfgFile.getContents()).hasSameContentAs(new RobotProjectConfigWriter().writeConfiguration(config));
    }

    @Test
    public void robotNatureIsRemoved() throws Exception {
        configure(project);
        project.getFile(RobotProjectConfig.FILENAME).delete(true, null);

        @SuppressWarnings("unchecked")
        final Predicate<String> shouldRemoveConfig = mock(Predicate.class);
        RobotProjectNature.removeRobotNature(project, monitor, shouldRemoveConfig);

        assertThat(RobotProjectNature.hasRobotNature(project)).isFalse();

        final IFile cfgFile = project.getFile(RobotProjectConfig.FILENAME);
        assertThat(cfgFile.exists()).isFalse();

        verifyNoInteractions(shouldRemoveConfig);
    }

    @Test
    public void robotNatureIsRemovedAndExistingConfigIsDeleted() throws Exception {
        configure(project);

        RobotProjectNature.removeRobotNature(project, monitor, p -> true);

        assertThat(RobotProjectNature.hasRobotNature(project)).isFalse();

        final IFile cfgFile = project.getFile(RobotProjectConfig.FILENAME);
        assertThat(cfgFile.exists()).isFalse();
    }

    @Test
    public void robotNatureIsRemovedAndExistingConfigIsNotDeleted() throws Exception {
        configure(project);

        RobotProjectNature.removeRobotNature(project, monitor, p -> false);

        assertThat(RobotProjectNature.hasRobotNature(project)).isFalse();

        final IFile cfgFile = project.getFile(RobotProjectConfig.FILENAME);
        assertThat(cfgFile.exists()).isTrue();
    }

}
