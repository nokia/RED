/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ProjectProvider;

public class RobotLaunchConfigurationShortcutTest {

    private static final String PROJECT_NAME = RobotLaunchConfigurationFinderTest.class.getSimpleName();

    private static final List<String> RESOURCE_NAMES = asList("Resource1.fake");

    private static IProject project;

    private static List<IResource> resources;

    @BeforeClass
    public static void createNeededResources() throws CoreException, IOException, ClassNotFoundException {
        resources = new ArrayList<>();
        project = projectProvider.getProject();
        for (final String name : RESOURCE_NAMES) {
            resources.add(projectProvider.createFile(name, ""));
        }
    }

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

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
        final IStructuredSelection selection = new StructuredSelection(resources);
        final ILaunchConfiguration[] configs = shortcut.getLaunchConfigurations(selection);

        assertThat(configs).isNotNull();
        assertThat(configs.length).isEqualTo(1);
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configs[0]);
        assertThat(robotConfig.getProject()).isEqualTo(project);
        assertThat(robotConfig.getResourcesUnderDebug()).containsExactly(resources.get(0));
        assertThat(robotConfig.isGeneralPurposeConfiguration()).isTrue();
    }

    @Test
    public void configurationReturned_whenTestCaseSelected() throws CoreException {
        final RobotLaunchConfigurationShortcut shortcut = new RobotLaunchConfigurationShortcut();
        final RobotCase rtc = mock(RobotCase.class);
        final RobotProject robotProject = mock(RobotProject.class);
        final RobotSuiteFile robotFile = new RobotSuiteFile(robotProject, (IFile) resources.get(0));
        when(robotProject.getProject()).thenReturn(project);
        when(rtc.getSuiteFile()).thenReturn(robotFile);
        final IStructuredSelection selection = new StructuredSelection(rtc);
        final ILaunchConfiguration[] configs = shortcut.getLaunchConfigurations(selection);

        assertThat(configs).isNotNull();
        assertThat(configs.length).isEqualTo(1);
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configs[0]);
        assertThat(robotConfig.getProject()).isEqualTo(project);
        assertThat(robotConfig.getResourcesUnderDebug()).containsExactly(resources.get(0));
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
        when(editorInput.getFile()).thenReturn((IFile) resources.get(0));
        final ILaunchConfiguration[] configs = shortcut.getLaunchConfigurations(editorPart);

        assertThat(configs).isNotNull();
        assertThat(configs.length).isEqualTo(1);
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configs[0]);
        assertThat(robotConfig.getProject()).isEqualTo(project);
        assertThat(robotConfig.getResourcesUnderDebug()).containsExactly(resources.get(0));
        assertThat(robotConfig.isGeneralPurposeConfiguration()).isTrue();
    }

    @Test
    public void sameConfigurationReturned_whenConfigForEditorInputSaved() throws CoreException {
        final RobotLaunchConfigurationShortcut shortcut = new RobotLaunchConfigurationShortcut();
        final IEditorPart editorPart = mock(IEditorPart.class);
        final FileEditorInput editorInput = mock(FileEditorInput.class);
        when(editorPart.getEditorInput()).thenReturn(editorInput);
        when(editorInput.getFile()).thenReturn((IFile) resources.get(0));
        final ILaunchConfiguration[] configs1 = shortcut.getLaunchConfigurations(editorPart);
        ((ILaunchConfigurationWorkingCopy) configs1[0]).doSave();
        final ILaunchConfiguration[] configs2 = shortcut.getLaunchConfigurations(editorPart);

        assertThat(configs2).isNotNull();
        assertThat(configs2.length).isEqualTo(1);
        assertThat(configs1[0]).isEqualToIgnoringGivenFields(configs2[0], "fOriginal");
    }
}
