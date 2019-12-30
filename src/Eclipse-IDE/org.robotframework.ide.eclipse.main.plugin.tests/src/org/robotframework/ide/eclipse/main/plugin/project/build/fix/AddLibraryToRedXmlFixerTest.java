/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscoverer.DiscovererFactory;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.SimpleLibrariesAutoDiscoverer;
import org.robotframework.red.junit.ProjectProvider;

public class AddLibraryToRedXmlFixerTest {

    private static final String PROJECT_NAME = AddLibraryToRedXmlFixerTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @ClassRule
    public static ProjectProvider notConfiguredProjectProvider = new ProjectProvider(PROJECT_NAME + "NoConfig");

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.configure();
    }

    @Test
    public void autodiscovererIsStarted_whenLibraryIsNotFound() throws Exception {
        final SimpleLibrariesAutoDiscoverer discoverer = mock(SimpleLibrariesAutoDiscoverer.class);
        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final IFile file = projectProvider.createFile("suite.robot");
        final IMarker marker = file.createMarker(RedPlugin.PLUGIN_ID);
        final AddLibraryToRedXmlFixer fixer = new AddLibraryToRedXmlFixer("UnknownPythonLibrary", false, factory);
        fixer.asContentProposal(marker).apply(null);

        assertThat(marker.exists()).isTrue();

        final RobotSuiteFile suite = new RobotModel().createSuiteFile(file);
        verify(factory).create(suite.getRobotProject(), newArrayList(suite));
        verify(discoverer).start();
        verifyNoMoreInteractions(discoverer);
    }

    @Test
    public void autodiscovererIsNotStarted_whenConfigurationFileDoesNotExist() throws Exception {
        final SimpleLibrariesAutoDiscoverer discoverer = mock(SimpleLibrariesAutoDiscoverer.class);
        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final IFile file = notConfiguredProjectProvider.createFile("suite.robot");
        final IMarker marker = file.createMarker(RedPlugin.PLUGIN_ID);
        final AddLibraryToRedXmlFixer fixer = new AddLibraryToRedXmlFixer("UnknownPythonLibrary", false, factory);
        fixer.asContentProposal(marker).apply(null);

        assertThat(marker.exists()).isTrue();

        verifyNoInteractions(factory);
        verifyNoInteractions(discoverer);
    }

}
