/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditor;
import org.robotframework.red.graphics.ImagesManager;


public class AddRemoteLibraryToRedXmlFixer extends RedXmlConfigMarkerResolution {

    private final String path;

    public AddRemoteLibraryToRedXmlFixer(final String path) {
        this.path = path;
    }

    @Override
    public String getLabel() {
        return "Add 'Remote " + path + "' to configuration";
    }

    @Override
    protected ICompletionProposal asContentProposal(final IMarker marker, final IFile externalFile) {
        final RobotSuiteFile file = RedPlugin.getModelManager().createSuiteFile((IFile) marker.getResource());
        return new AddLibraryProposal(marker, file, externalFile, getLabel());
    }

    private class AddLibraryProposal extends RedConfigFileCompletionProposal {

        private final RobotSuiteFile suiteFile;

        public AddLibraryProposal(final IMarker marker, final RobotSuiteFile suiteFile, final IFile externalFile,
                final String shortDescription) {
            super(marker, externalFile, shortDescription, null);
            this.suiteFile = suiteFile;
        }

        @Override
        public boolean apply(final IFile externalFile, final RobotProjectConfig config) {
            final RemoteLocation remoteLibrary = RemoteLocation.create(path);
            config.addRemoteLocation(remoteLibrary);
            return !config.getRemoteLocations().isEmpty();
        }

        @Override
        protected void openDesiredPageInEditor(final RedProjectEditor editor) {
            editor.openLibrariesPage();
        }

        @Override
        protected void fireEvents() {
            final RobotProject robotProject = suiteFile.getProject();
            final RobotProjectConfig config = robotProject.getRobotProjectConfig();
            final RedProjectConfigEventData<Collection<RemoteLocation>> eventData = new RedProjectConfigEventData<>(
                    externalFile, config.getRemoteLocations());
            eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED, eventData);
        }

        @Override
        public String getAdditionalProposalInfo() {
            return "Add 'Remote' library under '" + path + "' location to red.xml file";
        }

        @Override
        public Image getImage() {
            return ImagesManager.getImage(RedImages.getLibraryImage());
        }
    }
}
