/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscoverer;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscoverer.DiscovererFactory;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditor;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibraryFinder;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibraryFinder.IncorrectLibraryPathException;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibraryFinder.UnknownLibraryException;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibraryImporter;
import org.robotframework.red.graphics.ImagesManager;

/**
 * @author Michal Anglart
 */
public class AddLibraryToRedXmlFixer extends RedXmlConfigMarkerResolution {

    private final String pathOrName;

    private final boolean isPath;

    private final DiscovererFactory discovererFactory;

    public AddLibraryToRedXmlFixer(final String pathOrName, final boolean isPath) {
        this(pathOrName, isPath, (project, suites) -> new LibrariesAutoDiscoverer(project, suites, pathOrName));
    }

    AddLibraryToRedXmlFixer(final String pathOrName, final boolean isPath, final DiscovererFactory discovererFactory) {
        this.pathOrName = pathOrName;
        this.isPath = isPath;
        this.discovererFactory = discovererFactory;
    }

    @Override
    public String getLabel() {
        return "Discover '" + pathOrName + "' and add to configuration";
    }

    @Override
    protected ICompletionProposal asContentProposal(final IMarker marker, final IFile externalFile) {
        final RobotSuiteFile file = RedPlugin.getModelManager().createSuiteFile((IFile) marker.getResource());
        return new AddLibraryProposal(marker, file, externalFile, getLabel());
    }

    private class AddLibraryProposal extends RedConfigFileCompletionProposal {

        private final RobotSuiteFile suiteFile;

        private final Collection<ReferencedLibrary> addedLibraries = new ArrayList<>();

        public AddLibraryProposal(final IMarker marker, final RobotSuiteFile suiteFile, final IFile externalFile,
                final String shortDescription) {
            super(marker, externalFile, shortDescription, null);
            this.suiteFile = suiteFile;
        }

        @Override
        public boolean apply(final IFile externalFile, final RobotProjectConfig config) {
            final Shell shell = Display.getCurrent().getActiveShell();
            try {
                final ReferencedLibraryFinder libraryFinder = new ReferencedLibraryFinder(suiteFile,
                        new ReferencedLibraryImporter(shell));
                if (isPath) {
                    addedLibraries.addAll(libraryFinder.findByPath(config, pathOrName));
                } else {
                    addedLibraries.addAll(libraryFinder.findByName(config, pathOrName));
                }
                addedLibraries.forEach(config::addReferencedLibrary);
            } catch (final UnknownLibraryException e) {
                startAutoDiscovering();
            } catch (final IncorrectLibraryPathException e) {
                MessageDialog.openError(shell, "Library import problem", e.getMessage());
            }
            return !addedLibraries.isEmpty();
        }

        private void startAutoDiscovering() {
            discovererFactory.create(suiteFile.getProject(), newArrayList(suiteFile.getFile())).start();
        }

        @Override
        protected void openDesiredPageInEditor(final RedProjectEditor editor) {
            editor.openLibrariesPage();
        }

        @Override
        protected void fireEvents() {
            if (!addedLibraries.isEmpty()) {
                final RedProjectConfigEventData<Collection<ReferencedLibrary>> eventData = new RedProjectConfigEventData<>(
                        externalFile, addedLibraries);
                eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED, eventData);
            }
        }

        @Override
        public String getAdditionalProposalInfo() {
            return isPath ? "Add '" + pathOrName + "' location to red.xml file"
                    : "Try to discover location of '" + pathOrName + "' library and add it to red.xml file";
        }

        @Override
        public Image getImage() {
            return ImagesManager.getImage(RedImages.getMagnifierImage());
        }
    }
}
