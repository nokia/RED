/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.WrappedResource;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.ResourcesMocks;

public class NonRobotProjectsFilterTest {

    private final NonRobotProjectsFilter filter = new NonRobotProjectsFilter();

    @Test
    public void whenFileIsGiven_itPassesThroughFilter() {
        final boolean result = filter.select(null, null, mock(IFile.class));
        assertThat(result).isTrue();
    }

    @Test
    public void whenFolderIsGiven_itPassesThroughFilter() {
        final boolean result = filter.select(null, null, mock(IFolder.class));
        assertThat(result).isTrue();
    }

    @Test
    public void whenClosedProjectIsGiven_itPassesThroughFilter() {
        final IProject project = mock(IProject.class);
        when(project.isOpen()).thenReturn(false);

        final boolean result = filter.select(null, null, project);
        assertThat(result).isTrue();
    }

    @Test
    public void whenClosedRobotProjectIsGiven_itPassesThroughFilter() {
        final IProject project = ResourcesMocks.prepareRobotMockProject();
        when(project.isOpen()).thenReturn(false);

        final boolean result = filter.select(null, null, project);
        assertThat(result).isTrue();
    }

    @Test
    public void whenOpenProjectIsGiven_itDoesNotPassThroughFilter() {
        final IProject project = mock(IProject.class);
        when(project.isOpen()).thenReturn(true);

        final boolean result = filter.select(null, null, project);
        assertThat(result).isFalse();
    }

    @Test
    public void whenOpenWrappedProjectIsGiven_itDoesNotPassThroughFilter() {
        final IProject project = mock(IProject.class);
        when(project.isOpen()).thenReturn(true);

        final boolean result = filter.select(null, null, new WrappedResource(project));
        assertThat(result).isFalse();
    }

    @Test
    public void whenOpenRobotProjectIsGiven_itPassesThroughFilter() {
        final IProject project = ResourcesMocks.prepareRobotMockProject();
        when(project.isOpen()).thenReturn(true);

        final boolean result = filter.select(null, null, project);
        assertThat(result).isTrue();
    }

    @Test
    public void whenOpenWrappedRobotProjectIsGiven_itPassesThroughFilter() {
        final IProject project = ResourcesMocks.prepareRobotMockProject();
        when(project.isOpen()).thenReturn(true);

        final boolean result = filter.select(null, null, new WrappedResource(project));
        assertThat(result).isTrue();
    }

}
