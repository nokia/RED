/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.remote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ProjectProvider;

public class RemoteRobotLaunchConfigurationShortcutTest {

    @ClassRule
    public static ProjectProvider projectProvider1 = new ProjectProvider("Project1");

    @ClassRule
    public static ProjectProvider projectProvider2 = new ProjectProvider("Project2");

    @Test
    public void noConfigurationReturned_forSelection() {
        final RemoteRobotLaunchConfigurationShortcut shortcut = new RemoteRobotLaunchConfigurationShortcut();
        final IStructuredSelection selection = new StructuredSelection();
        final ILaunchConfiguration[] configs = shortcut.getLaunchConfigurations(selection);

        assertThat(configs).isNull();
    }

    @Test
    public void noConfigurationReturned_forEditor() {
        final RemoteRobotLaunchConfigurationShortcut shortcut = new RemoteRobotLaunchConfigurationShortcut();
        final IEditorPart editorPart = mock(IEditorPart.class);
        final ILaunchConfiguration[] configs = shortcut.getLaunchConfigurations(editorPart);

        assertThat(configs).isNull();
    }

    @Test
    public void nullReturned_forLaunchableEditorResources() {
        final RemoteRobotLaunchConfigurationShortcut shortcut = new RemoteRobotLaunchConfigurationShortcut();
        final IEditorPart editorPart = mock(IEditorPart.class);
        final IResource resource = shortcut.getLaunchableResource(editorPart);

        assertThat(resource).isNull();
    }

    @Test
    public void nullReturned_forLaunchableSelectedResources() {
        final RemoteRobotLaunchConfigurationShortcut shortcut = new RemoteRobotLaunchConfigurationShortcut();
        final IStructuredSelection selection = new StructuredSelection();
        final IResource resource = shortcut.getLaunchableResource(selection);

        assertThat(resource).isNull();
    }

    @Test
    public void noProjectReturned_forEmptySelection() {
        final IStructuredSelection selection = new StructuredSelection();
        final Optional<IProject> project = RemoteRobotLaunchConfigurationShortcut.getProjectFromSelection(selection);

        assertThat(project.isPresent()).isFalse();
    }

    @Test
    public void noProjectReturned_forResourcesFromDifferentProjects() throws IOException, CoreException {
        final List<IResource> resources = new ArrayList<>();
        resources.add(projectProvider1.createFile("res1.fake", ""));
        resources.add(projectProvider1.createFile("res2.fake", ""));
        resources.add(projectProvider2.createFile("res3.fake", ""));
        final IStructuredSelection selection = new StructuredSelection(resources);
        final Optional<IProject> project = RemoteRobotLaunchConfigurationShortcut.getProjectFromSelection(selection);

        assertThat(project.isPresent()).isFalse();
    }

    @Test
    public void noProjectReturned_forTestCasesFromDifferentProjects() throws IOException, CoreException {
        final RobotCase rtc1 = mock(RobotCase.class);
        final RobotProject robotProject1 = mock(RobotProject.class);
        final RobotSuiteFile robotFile1 = new RobotSuiteFile(robotProject1,
                projectProvider1.createFile("res1.fake", ""));
        when(robotProject1.getProject()).thenReturn(projectProvider1.getProject());
        when(rtc1.getSuiteFile()).thenReturn(robotFile1);
        final RobotCase rtc2 = mock(RobotCase.class);
        final RobotProject robotProject2 = mock(RobotProject.class);
        final RobotSuiteFile robotFile2 = new RobotSuiteFile(robotProject2,
                projectProvider2.createFile("res2.fake", ""));
        when(robotProject2.getProject()).thenReturn(projectProvider2.getProject());
        when(rtc2.getSuiteFile()).thenReturn(robotFile2);

        final List<RobotCase> testCases = new ArrayList<>();
        testCases.add(rtc1);
        testCases.add(rtc2);
        final IStructuredSelection selection = new StructuredSelection(testCases);
        final Optional<IProject> project = RemoteRobotLaunchConfigurationShortcut.getProjectFromSelection(selection);

        assertThat(project.isPresent()).isFalse();
    }

    @Test
    public void noProjectReturned_forTestCasesSectionsFromDifferentProjects() throws IOException, CoreException {
        final RobotProject robotProject1 = mock(RobotProject.class);
        final RobotSuiteFile robotFile1 = new RobotSuiteFile(robotProject1,
                projectProvider1.createFile("res1.fake", ""));
        when(robotProject1.getProject()).thenReturn(projectProvider1.getProject());
        final RobotCasesSection section1 = mock(RobotCasesSection.class);
        when(section1.getSuiteFile()).thenReturn(robotFile1);
        final RobotProject robotProject2 = mock(RobotProject.class);
        final RobotSuiteFile robotFile2 = new RobotSuiteFile(robotProject2,
                projectProvider2.createFile("res2.fake", ""));
        when(robotProject2.getProject()).thenReturn(projectProvider2.getProject());
        final RobotCasesSection section2 = mock(RobotCasesSection.class);
        when(section2.getSuiteFile()).thenReturn(robotFile2);

        final List<RobotCasesSection> casesSections = new ArrayList<>();
        casesSections.add(section1);
        casesSections.add(section2);
        final IStructuredSelection selection = new StructuredSelection(casesSections);
        final Optional<IProject> project = RemoteRobotLaunchConfigurationShortcut.getProjectFromSelection(selection);

        assertThat(project.isPresent()).isFalse();
    }

    @Test
    public void projectReturned_forResourcesFromSameProject() throws IOException, CoreException {
        final List<IResource> resources = new ArrayList<>();
        resources.add(projectProvider1.createFile("res1.fake", ""));
        resources.add(projectProvider1.createFile("res2.fake", ""));
        final IStructuredSelection selection = new StructuredSelection(resources);
        final Optional<IProject> project = RemoteRobotLaunchConfigurationShortcut.getProjectFromSelection(selection);

        assertThat(project.isPresent()).isTrue();
        assertThat(project.get()).isEqualTo(projectProvider1.getProject());
    }

    @Test
    public void projectReturned_forTestCasesFromSameProject() throws IOException, CoreException {
        final RobotCase rtc1 = mock(RobotCase.class);
        final RobotProject robotProject1 = mock(RobotProject.class);
        final RobotSuiteFile robotFile1 = new RobotSuiteFile(robotProject1,
                projectProvider1.createFile("res1.fake", ""));
        when(robotProject1.getProject()).thenReturn(projectProvider1.getProject());
        when(rtc1.getSuiteFile()).thenReturn(robotFile1);
        final RobotCase rtc2 = mock(RobotCase.class);
        final RobotSuiteFile robotFile2 = new RobotSuiteFile(robotProject1,
                projectProvider1.createFile("res2.fake", ""));
        when(rtc2.getSuiteFile()).thenReturn(robotFile2);

        final List<RobotCase> testCases = new ArrayList<>();
        testCases.add(rtc1);
        testCases.add(rtc2);
        final IStructuredSelection selection = new StructuredSelection(testCases);
        final Optional<IProject> project = RemoteRobotLaunchConfigurationShortcut.getProjectFromSelection(selection);

        assertThat(project.isPresent()).isTrue();
        assertThat(project.get()).isEqualTo(projectProvider1.getProject());
    }

    @Test
    public void projectReturned_forTestCasesSectionsFromSameProject() throws IOException, CoreException {
        final RobotProject robotProject1 = mock(RobotProject.class);
        final RobotSuiteFile robotFile1 = new RobotSuiteFile(robotProject1,
                projectProvider1.createFile("res1.fake", ""));
        when(robotProject1.getProject()).thenReturn(projectProvider1.getProject());
        final RobotCasesSection section1 = mock(RobotCasesSection.class);
        when(section1.getSuiteFile()).thenReturn(robotFile1);
        final RobotSuiteFile robotFile2 = new RobotSuiteFile(robotProject1,
                projectProvider1.createFile("res2.fake", ""));
        final RobotCasesSection section2 = mock(RobotCasesSection.class);
        when(section2.getSuiteFile()).thenReturn(robotFile2);

        final List<RobotCasesSection> casesSections = new ArrayList<>();
        casesSections.add(section1);
        casesSections.add(section2);
        final IStructuredSelection selection = new StructuredSelection(casesSections);
        final Optional<IProject> project = RemoteRobotLaunchConfigurationShortcut.getProjectFromSelection(selection);

        assertThat(project.isPresent()).isTrue();
        assertThat(project.get()).isEqualTo(projectProvider1.getProject());
    }
}
