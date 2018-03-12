/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.function.BooleanSupplier;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.rf.ide.core.project.RobotProjectConfigWriter;
import org.robotframework.red.junit.ProjectProvider;

public class RobotProjectNatureTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RobotProjectNatureTest.class);

    private final IProgressMonitor monitor = new NullProgressMonitor();

    @Before
    public void before() throws Exception {
        final IProject project = projectProvider.getProject();

        final IProjectDescription desc = project.getDescription();
        desc.setNatureIds(new String[0]);
        project.setDescription(desc, monitor);

        project.getFile(RobotProjectConfig.FILENAME).delete(true, null);
    }

    @Test
    public void robotNatureIsSetAndDefaultConfigIsCreated_whenDoesNotExist() throws Exception {
        final IProject project = projectProvider.getProject();

        final BooleanSupplier shouldReplaceConfig = mock(BooleanSupplier.class);
        RobotProjectNature.addRobotNature(project, monitor, shouldReplaceConfig);

        assertThat(RobotProjectNature.hasRobotNature(project)).isTrue();

        final IFile cfgFile = project.getFile(RobotProjectConfig.FILENAME);
        assertThat(cfgFile.exists()).isTrue();
        assertThat(cfgFile.getContents())
                .hasSameContentAs(new RobotProjectConfigWriter().writeConfiguration(RobotProjectConfig.create()));

        verifyZeroInteractions(shouldReplaceConfig);
    }

    @Test
    public void robotNatureIsSetAndExistingConfigIsReplaced() throws Exception {
        final IProject project = projectProvider.getProject();
        final RobotProjectConfig config = RobotProjectConfig.create();
        config.addVariableMapping(VariableMapping.create("${xyz}", "abc"));
        projectProvider.configure(config);

        RobotProjectNature.addRobotNature(project, monitor, () -> true);

        assertThat(RobotProjectNature.hasRobotNature(project)).isTrue();

        final IFile cfgFile = project.getFile(RobotProjectConfig.FILENAME);
        assertThat(cfgFile.exists()).isTrue();
        assertThat(cfgFile.getContents())
                .hasSameContentAs(new RobotProjectConfigWriter().writeConfiguration(RobotProjectConfig.create()));
    }

    @Test
    public void robotNatureIsSetAndExistingConfigIsNotReplaced() throws Exception {
        final IProject project = projectProvider.getProject();
        final RobotProjectConfig config = RobotProjectConfig.create();
        config.addVariableMapping(VariableMapping.create("${xyz}", "abc"));
        projectProvider.configure(config);

        RobotProjectNature.addRobotNature(project, monitor, () -> false);

        assertThat(RobotProjectNature.hasRobotNature(project)).isTrue();

        final IFile cfgFile = project.getFile(RobotProjectConfig.FILENAME);
        assertThat(cfgFile.exists()).isTrue();
        assertThat(cfgFile.getContents()).hasSameContentAs(new RobotProjectConfigWriter().writeConfiguration(config));
    }

    @Test
    public void robotNatureIsRemoved() throws Exception {
        final IProject project = projectProvider.getProject();
        projectProvider.configure();
        project.getFile(RobotProjectConfig.FILENAME).delete(true, null);

        final BooleanSupplier shouldRemoveConfig = mock(BooleanSupplier.class);
        RobotProjectNature.removeRobotNature(project, monitor, shouldRemoveConfig);

        assertThat(RobotProjectNature.hasRobotNature(project)).isFalse();

        final IFile cfgFile = project.getFile(RobotProjectConfig.FILENAME);
        assertThat(cfgFile.exists()).isFalse();

        verifyZeroInteractions(shouldRemoveConfig);
    }

    @Test
    public void robotNatureIsRemovedAndExistingConfigIsDeleted() throws Exception {
        final IProject project = projectProvider.getProject();
        projectProvider.configure();

        RobotProjectNature.removeRobotNature(project, monitor, () -> true);

        assertThat(RobotProjectNature.hasRobotNature(project)).isFalse();

        final IFile cfgFile = project.getFile(RobotProjectConfig.FILENAME);
        assertThat(cfgFile.exists()).isFalse();
    }

    @Test
    public void robotNatureIsRemovedAndExistingConfigIsNotDeleted() throws Exception {
        final IProject project = projectProvider.getProject();
        projectProvider.configure();

        RobotProjectNature.removeRobotNature(project, monitor, () -> false);

        assertThat(RobotProjectNature.hasRobotNature(project)).isFalse();

        final IFile cfgFile = project.getFile(RobotProjectConfig.FILENAME);
        assertThat(cfgFile.exists()).isTrue();
    }

}
