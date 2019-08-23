/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditor;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.LibrariesProjectConfigurationEditorPart;
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
        return new AddLibraryProposal(marker, externalFile, getLabel());
    }

    private class AddLibraryProposal extends RedConfigFileCompletionProposal {

        private RemoteLocation addedLocation;

        public AddLibraryProposal(final IMarker marker, final IFile externalFile, final String shortDescription) {
            super(marker, externalFile, shortDescription, null);
        }

        @Override
        public boolean apply(final IFile externalFile, final RobotProjectConfig config) {
            addedLocation = RemoteLocation.create(path);
            config.addRemoteLocation(addedLocation);
            return !config.getRemoteLocations().isEmpty();
        }

        @Override
        protected void openDesiredPageInEditor(final RedProjectEditor editor) {
            editor.openPage(LibrariesProjectConfigurationEditorPart.class);
        }

        @Override
        protected void fireEvents() {
            final RedProjectConfigEventData<Collection<RemoteLocation>> eventData = new RedProjectConfigEventData<>(
                    externalFile, newArrayList(addedLocation));
            eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_ARGUMENTS_ADDED, eventData);
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
