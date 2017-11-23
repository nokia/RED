/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscoverer;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscoverer.DiscovererFactory;
import org.robotframework.red.junit.ProjectProvider;

public class AddLibraryToRedXmlFixerTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(AddLibraryToRedXmlFixerTest.class);

    private static IFile suite;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.configure();
        suite = projectProvider.createFile("suite.robot");
    }

    @Test
    public void autodiscovererIsStarted_whenLibraryIsNotFound() throws Exception {
        final LibrariesAutoDiscoverer discoverer = mock(LibrariesAutoDiscoverer.class);
        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyList())).thenReturn(discoverer);

        final IMarker marker = suite.createMarker(RedPlugin.PLUGIN_ID);
        final AddLibraryToRedXmlFixer fixer = new AddLibraryToRedXmlFixer("UnknownPythonLibrary", false, factory);
        fixer.asContentProposal(marker).apply(null);

        assertThat(marker.exists()).isTrue();

        verify(factory).create(any(RobotProject.class), eq(newArrayList(suite)));
        verify(discoverer).start();
        verifyNoMoreInteractions(discoverer);
    }

}
