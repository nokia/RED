/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ProjectProvider;

public class AddRemoteLibraryToRedXmlFixerTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(AddRemoteLibraryToRedXmlFixerTest.class);

    private static RobotSuiteFile suite;

    private static RobotProject robotProject;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.configure();
        suite = new RobotModel().createSuiteFile(projectProvider.createFile("suite.robot"));
        robotProject = suite.getProject();
    }

    @Test
    public void addRemoteLibraryFromQuickFix_whenRemoteLocationIsNotAddedToRedXml() throws Exception {
        final String path = "http://127.0.0.1:9000";
        final IMarker marker = suite.getFile().createMarker(RedPlugin.PLUGIN_ID);
        final AddRemoteLibraryToRedXmlFixer fixer = new AddRemoteLibraryToRedXmlFixer(path);
        fixer.asContentProposal(marker).apply(null);

        assertThat(marker.exists()).isFalse();
        assertThat(robotProject.getRobotProjectConfig().getRemoteLocations().contains(
                RemoteLocation.create("http://127.0.0.1:9000/")));
    }

    @Test
    public void remoteFixerExistInFixers_whenRemoteLocationIsNotAddedToRedXml() throws Exception {

        final String path = "http://127.0.0.1:9000";
        final IMarkerResolution fixer = new AddRemoteLibraryToRedXmlFixer(path);

        assertThat(fixer.getLabel().equals("Add 'Remote http://127.0.0.1:9000' to configuration"));
    }

}
