/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class RobotLaunchConfigurationShortcutTest {

    @Project(files = { "Resource1.fake" })
    static IProject project;

    @Test
    public void nullReturned_whenSelectionIsEmpty() {
        final RobotLaunchConfigurationShortcut shortcut = new RobotLaunchConfigurationShortcut();
        final IStructuredSelection selection = new StructuredSelection();
        final ILaunchConfiguration[] configs = shortcut.getLaunchConfigurations(selection);

        assertThat(configs).isNull();
    }

    @Test
    public void configurationReturned_whenResourceSelected() throws CoreException {
        final RobotLaunchConfigurationShortcut shortcut = new RobotLaunchConfigurationShortcut();
        final IStructuredSelection selection = new StructuredSelection(
                Arrays.asList(getFile(project, "Resource1.fake")));
        final ILaunchConfiguration[] configs = shortcut.getLaunchConfigurations(selection);

        assertThat(configs).isNotNull();
        assertThat(configs.length).isEqualTo(1);
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configs[0]);
        assertThat(robotConfig.getProject()).isEqualTo(project);
        assertThat(robotConfig.isGeneralPurposeConfiguration()).isTrue();
    }

    @Test
    public void configurationReturned_whenTestCaseSelected() throws CoreException {
        final RobotLaunchConfigurationShortcut shortcut = new RobotLaunchConfigurationShortcut();
        final RobotCase rtc = mock(RobotCase.class);
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotSuiteFile robotFile = new RobotSuiteFile(robotProject, getFile(project, "Resource1.fake"));
        when(robotProject.getProject()).thenReturn(project);
        when(rtc.getSuiteFile()).thenReturn(robotFile);
        final IStructuredSelection selection = new StructuredSelection(rtc);
        final ILaunchConfiguration[] configs = shortcut.getLaunchConfigurations(selection);

        assertThat(configs).isNotNull();
        assertThat(configs.length).isEqualTo(1);
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configs[0]);
        assertThat(robotConfig.getProject()).isEqualTo(project);
        assertThat(robotConfig.isGeneralPurposeConfiguration()).isFalse();
    }

    @Test
    public void nullReturned_forLaunchableEditorResources() {
        final RobotLaunchConfigurationShortcut shortcut = new RobotLaunchConfigurationShortcut();
        final IEditorPart editorPart = mock(IEditorPart.class);
        final IResource resource = shortcut.getLaunchableResource(editorPart);

        assertThat(resource).isNull();
    }

    @Test
    public void nullReturned_forLaunchableSelectedResources() {
        final RobotLaunchConfigurationShortcut shortcut = new RobotLaunchConfigurationShortcut();
        final IStructuredSelection selection = new StructuredSelection();
        final IResource resource = shortcut.getLaunchableResource(selection);

        assertThat(resource).isNull();
    }

    @Test
    public void nullReturned_whenEditorInputIsNonLaunchable() {
        final RobotLaunchConfigurationShortcut shortcut = new RobotLaunchConfigurationShortcut();
        final IEditorPart editorPart = mock(IEditorPart.class);
        when(editorPart.getEditorInput()).thenReturn(null);
        final ILaunchConfiguration[] configs = shortcut.getLaunchConfigurations(editorPart);

        assertThat(configs).isNull();
    }

    @Test
    public void configurationReturned_whenEditorInputIsLaunchable() throws CoreException {
        final RobotLaunchConfigurationShortcut shortcut = new RobotLaunchConfigurationShortcut();
        final IEditorPart editorPart = mock(IEditorPart.class);
        final FileEditorInput editorInput = mock(FileEditorInput.class);
        when(editorPart.getEditorInput()).thenReturn(editorInput);
        when(editorInput.getFile()).thenReturn(getFile(project, "Resource1.fake"));
        final ILaunchConfiguration[] configs = shortcut.getLaunchConfigurations(editorPart);

        assertThat(configs).isNotNull();
        assertThat(configs.length).isEqualTo(1);
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configs[0]);
        assertThat(robotConfig.getProject()).isEqualTo(project);
        assertThat(robotConfig.isGeneralPurposeConfiguration()).isTrue();
    }

    @Test
    public void sameConfigurationReturned_whenConfigForEditorInputSaved() throws CoreException {
        final RobotLaunchConfigurationShortcut shortcut = new RobotLaunchConfigurationShortcut();
        final IEditorPart editorPart = mock(IEditorPart.class);
        final FileEditorInput editorInput = mock(FileEditorInput.class);
        when(editorPart.getEditorInput()).thenReturn(editorInput);
        when(editorInput.getFile()).thenReturn(getFile(project, "Resource1.fake"));
        final ILaunchConfiguration[] configs1 = shortcut.getLaunchConfigurations(editorPart);
        ((ILaunchConfigurationWorkingCopy) configs1[0]).doSave();
        final ILaunchConfiguration[] configs2 = shortcut.getLaunchConfigurations(editorPart);

        assertThat(configs2).isNotNull();
        assertThat(configs2.length).isEqualTo(1);
        assertThat(configs1[0]).isEqualToIgnoringGivenFields(configs2[0], "fOriginal");
    }
}
