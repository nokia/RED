/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.WrappedResource;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.ResourcesMocks;

public class SuiteInitFilesFilterTest {

    private final SuiteInitFilesFilter filter = new SuiteInitFilesFilter();

    @Test
    public void whenProjectIsGiven_itPassesThroughFilter() {
        final boolean result = filter.select(null, null, mock(IProject.class));
        assertThat(result).isTrue();
    }

    @Test
    public void whenFolderIsGiven_itPassesThroughFilter() {
        final boolean result = filter.select(null, null, mock(IFolder.class));
        assertThat(result).isTrue();
    }

    @Test
    public void whenOrdinaryFileIs_itPassesThroughFilter() {
        final boolean result = filter.select(null, null, mock(IFile.class));
        assertThat(result).isTrue();
    }

    @Test
    public void whenSuiteInitFileIsGiven_itDoesNotPassThroughFilter() {
        final IFile file = ResourcesMocks.prepareSuiteInitMockFile();
        final boolean result = filter.select(null, null, file);
        assertThat(result).isFalse();
    }

    @Test
    public void whenWrapepdSuiteInitFileIsGiven_itDoesNotPassThroughFilter() {
        final IFile file = ResourcesMocks.prepareSuiteInitMockFile();
        final boolean result = filter.select(null, null, new WrappedResource(file));
        assertThat(result).isFalse();
    }

}
