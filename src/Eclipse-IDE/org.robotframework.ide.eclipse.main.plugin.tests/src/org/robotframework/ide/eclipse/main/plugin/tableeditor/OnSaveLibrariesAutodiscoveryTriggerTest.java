/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.LibrariesAutoDiscoverer;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Suppliers;

public class OnSaveLibrariesAutodiscoveryTriggerTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(OnSaveLibrariesAutodiscoveryTriggerTest.class);

    private static RobotModel model = new RobotModel();

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("suite_with_known_libraries.robot",
                "*** Settings ***",
                "Library  known1",
                "Library  known2");
        projectProvider.createFile("suite_with_unknown_library.robot",
                "*** Settings ***",
                "Library  known1",
                "Library  unknown",
                "Library  known2");

        final ReferencedLibrary knownLib1 = ReferencedLibrary.create(LibraryType.PYTHON, "known1", "");
        final LibrarySpecification knownLib1spec = new LibrarySpecification();
        knownLib1spec.setName("known1");
        final ReferencedLibrary knownLib2 = ReferencedLibrary.create(LibraryType.PYTHON, "known2", "");
        final LibrarySpecification knownLib2spec = new LibrarySpecification();
        knownLib2spec.setName("known2");
        final Map<ReferencedLibrary, LibrarySpecification> libs = new HashMap<>();
        libs.put(knownLib1, knownLib1spec);
        libs.put(knownLib2, knownLib2spec);

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(knownLib1);
        config.addReferencedLibrary(knownLib2);
        projectProvider.configure(config);

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setReferencedLibraries(libs);
        robotProject.setStandardLibraries(new HashMap<String, LibrarySpecification>());
    }
    
    @AfterClass
    public static void afterSuite() {
        model = null;
    }

    @Test
    public void autodiscovererIsNotStarted_whenSuiteDoesNotContainUnknownLibrary() {
        turnOnAutoDiscoveringInProjectConfig();

        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.getFile("suite_with_known_libraries.robot"));
        final LibrariesAutoDiscoverer discoverer = mock(LibrariesAutoDiscoverer.class);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(suite,
                Suppliers.ofInstance(discoverer));

        trigger.startLibrariesAutoDiscoveryIfRequired();

        verifyZeroInteractions(discoverer);

        turnOffAutoDiscoveringInProjectConfig();
    }

    @Test
    public void autodiscovererIsNotStarted_whenItIsDisabled() {
        turnOffAutoDiscoveringInProjectConfig();

        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.getFile("suite_with_unknown_library.robot"));
        final LibrariesAutoDiscoverer discoverer = mock(LibrariesAutoDiscoverer.class);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(suite,
                Suppliers.ofInstance(discoverer));

        trigger.startLibrariesAutoDiscoveryIfRequired();

        verifyZeroInteractions(discoverer);

        turnOnAutoDiscoveringInProjectConfig();
    }

    @Test
    public void autodiscovererStarts_whenUnknownLibraryIsDetected() {
        turnOnAutoDiscoveringInProjectConfig();

        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.getFile("suite_with_unknown_library.robot"));
        final LibrariesAutoDiscoverer discoverer = mock(LibrariesAutoDiscoverer.class);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(suite,
                Suppliers.ofInstance(discoverer));
        
        trigger.startLibrariesAutoDiscoveryIfRequired();
        
        verify(discoverer).start();
        verifyNoMoreInteractions(discoverer);

        turnOffAutoDiscoveringInProjectConfig();
    }

    private void turnOnAutoDiscoveringInProjectConfig() {
        final RobotProjectConfig config = model.createRobotProject(projectProvider.getProject())
                .getRobotProjectConfig();
        config.setReferencedLibrariesAutoDiscoveringEnabled(true);
    }

    private void turnOffAutoDiscoveringInProjectConfig() {
        final RobotProjectConfig config = model.createRobotProject(projectProvider.getProject())
                .getRobotProjectConfig();
        config.setReferencedLibrariesAutoDiscoveringEnabled(false);
    }

}
