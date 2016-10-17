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

public class LibspecDirectoryFilterTest {

    private final LibspecDirectoryFilter filter = new LibspecDirectoryFilter();

    @Test
    public void whenFileIsGiven_itPassesThroughFilter() {
        final boolean result = filter.select(null, null, mock(IFile.class));
        assertThat(result).isTrue();
    }

    @Test
    public void whenProjectIsGiven_itPassesThroughFilter() {
        final boolean result = filter.select(null, null, mock(IProject.class));
        assertThat(result).isTrue();
    }

    @Test
    public void whenOrdinaryFolderIsGiven_itPassesThroughFilter() {
        final IFolder mock = mock(IFolder.class);
        when(mock.getName()).thenReturn("some_folder");

        final boolean result = filter.select(null, null, mock);
        assertThat(result).isTrue();
    }

    @Test
    public void whenLibspecNestedFolderIsGiven_itPassesThroughFilter() {
        final IFolder mock = mock(IFolder.class);
        when(mock.getName()).thenReturn("libspecs");
        when(mock.getParent()).thenReturn(mock(IFolder.class));

        final boolean result = filter.select(null, null, mock);
        assertThat(result).isTrue();
    }

    @Test
    public void whenLibspecFolderOnTopLevelIsGiven_itDoesNotPassThroughFilter() {
        final IFolder mock = mock(IFolder.class);
        when(mock.getName()).thenReturn("libspecs");
        when(mock.getParent()).thenReturn(mock(IProject.class));

        final boolean result = filter.select(null, null, mock);
        assertThat(result).isFalse();
    }

    @Test
    public void whenWrappedAdaptableLibspecFolderOnTopLevelIsGiven_itDoesNotPassThroughFilter() {
        final IFolder mock = mock(IFolder.class);
        when(mock.getName()).thenReturn("libspecs");
        when(mock.getParent()).thenReturn(mock(IProject.class));

        final boolean result = filter.select(null, null, new WrappedResource(mock));
        assertThat(result).isFalse();
    }
}
