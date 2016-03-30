/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.PathsResolver;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.LibraryType;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditor;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibraryImporter;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;


/**
 * @author Michal Anglart
 *
 */
public class AddLibraryToRedXmlFixer extends RedXmlConfigMarkerResolution {

    private final String pathOrName;

    private final boolean isPath;

    public AddLibraryToRedXmlFixer(final String pathOrName, final boolean isPath) {
        this.pathOrName = pathOrName;
        this.isPath = isPath;
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

        private String libName;

        private final Collection<ReferencedLibrary> addedLibraries = new ArrayList<>();

        public AddLibraryProposal(final IMarker marker, final RobotSuiteFile suiteFile, final IFile externalFile,
                final String shortDescritption) {
            super(marker, externalFile, shortDescritption, null);
            this.suiteFile = suiteFile;
        }

        @Override
        public boolean apply(final IFile externalFile, final RobotProjectConfig config)
                throws ProposalApplyingException {
            return isPath ? importLibraryByPath(config) : importLibraryByName(externalFile, config);
        }

        private boolean importLibraryByName(final IFile externalFile, final RobotProjectConfig config) {
            final RobotProject project = RedPlugin.getModelManager().createProject(externalFile.getProject());
            final RobotRuntimeEnvironment env = project.getRuntimeEnvironment();
            
            Optional<File> modulePath = Optional.absent();
            try {
                modulePath = env.getModulePath(pathOrName);
            } catch (final RobotEnvironmentException e) {
                // that's fine
            }

            if (modulePath.isPresent()) {
                libName = pathOrName;
            } else {
                final String location = env.getInterpreter() == SuiteExecutor.Jython ? "PYTHONPATH/CLASSPATH"
                        : "PYTHONPATH";
                MessageDialog.openError(Display.getCurrent().getActiveShell(), "Library import problem",
                        "Unable to locate '" + pathOrName + "' module. It seems that it is not contained"
                                + " in " + location + " of " + env.getFile() + " python installation.");
                return false;
            }
            final Path path = new Path(modulePath.get().getPath());

            final ReferencedLibrary newLibrary = new ReferencedLibrary();
            final String fileExt = path.getFileExtension();
            newLibrary.setType(fileExt != null && fileExt.equals("jar") ? LibraryType.JAVA.toString()
                    : LibraryType.PYTHON.toString());
            newLibrary.setName(libName);
            newLibrary.setPath(path.toPortableString());
            config.addReferencedLibrary(newLibrary);

            addedLibraries.add(newLibrary);

            return true;
        }

        private boolean importLibraryByPath(final RobotProjectConfig config) {
            if (pathOrName.endsWith("/") || pathOrName.endsWith(".py")) {
                final IPath resolvedAbsPath = PathsResolver.resolveToAbsolutePath(suiteFile, pathOrName);
                final ReferencedLibraryImporter importer = new ReferencedLibraryImporter();
                addedLibraries.addAll(importer.importPythonLib(Display.getCurrent().getActiveShell(),
                        suiteFile.getProject().getRuntimeEnvironment(), resolvedAbsPath.toString()));

                if (addedLibraries.isEmpty()) {
                    throw new ProposalApplyingException("Unable to apply proposal");
                } else {
                    for (final ReferencedLibrary addedLibrary : addedLibraries) {
                        config.addReferencedLibrary(addedLibrary);
                    }
                    return true;
                }
            } else {
                MessageDialog.openError(Display.getCurrent().getActiveShell(), "Library import problem",
                        "The path '" + pathOrName + "' should point to either .py file or python module directory.");
                return false;
            }
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
