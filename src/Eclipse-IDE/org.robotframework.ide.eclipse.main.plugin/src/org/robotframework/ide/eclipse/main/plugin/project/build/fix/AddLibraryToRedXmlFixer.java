/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.project.ImportPath;
import org.rf.ide.core.project.ImportSearchPaths;
import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.project.ResolvedImportPath;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscoverer;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditor;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibraryImporter;
import org.robotframework.red.graphics.ImagesManager;

/**
 * @author Michal Anglart
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

        private final Collection<ReferencedLibrary> addedLibraries = new ArrayList<>();

        public AddLibraryProposal(final IMarker marker, final RobotSuiteFile suiteFile, final IFile externalFile,
                final String shortDescription) {
            super(marker, externalFile, shortDescription, null);
            this.suiteFile = suiteFile;
        }

        @Override
        public boolean apply(final IFile externalFile, final RobotProjectConfig config) {
            try {
                if (isPath) {
                    addedLibraries.addAll(importLibraryByPath(config, pathOrName));
                } else {
                    addedLibraries.addAll(importLibraryByName(config));
                }
                addedLibraries.forEach(config::addReferencedLibrary);
            } catch (final RobotEnvironmentException e) {
                startAutoDiscovering(suiteFile.getProject());
            } catch (final LibraryPathException e) {
                MessageDialog.openError(Display.getCurrent().getActiveShell(), "Library import problem",
                        e.getMessage());
            }
            return !addedLibraries.isEmpty();
        }

        private Collection<ReferencedLibrary> importLibraryByName(final RobotProjectConfig config)
                throws LibraryPathException {
            final RobotProject robotProject = suiteFile.getProject();
            final String currentFileDirectoryPath = suiteFile.getFile().getParent().getLocation().toOSString();
            final EnvironmentSearchPaths searchPaths = new RedEclipseProjectConfig(config)
                    .createEnvironmentSearchPaths(robotProject.getProject());
            searchPaths.addPythonPath(currentFileDirectoryPath);
            searchPaths.addClassPath(currentFileDirectoryPath);

            final Optional<File> modulePath = robotProject.getRuntimeEnvironment().getModulePath(pathOrName,
                    searchPaths);
            if (!modulePath.isPresent()) {
                throw new RobotEnvironmentException("No path found");
            }
            final File moduleFile = modulePath.get();
            if (moduleFile.getAbsolutePath().endsWith(".jar")) {
                final IPath resolvedAbsPath = new Path(moduleFile.getAbsolutePath());
                final ReferencedLibraryImporter importer = new ReferencedLibraryImporter();
                return importer.importJavaLib(Display.getCurrent().getActiveShell(),
                        robotProject.getRuntimeEnvironment(), robotProject.getProject(), config,
                        resolvedAbsPath.toString());
            } else {
                if (moduleFile.isDirectory() && new File(moduleFile, "__init__.py").exists()) {
                    return Collections.singletonList(ReferencedLibrary.create(LibraryType.PYTHON, pathOrName,
                            new Path(moduleFile.getPath()).toPortableString()));
                } else {
                    return importLibraryByPath(config, moduleFile.getAbsolutePath());
                }
            }
        }

        private Collection<ReferencedLibrary> importLibraryByPath(final RobotProjectConfig config, final String path)
                throws LibraryPathException {
            if (path.endsWith("/") || path.endsWith(".py")) {
                final Map<String, String> vars = suiteFile.getProject().getRobotProjectHolder().getVariableMappings();
                final ResolvedImportPath resolvedPath = ResolvedImportPath.from(ImportPath.from(path), vars).get();

                final PathsProvider pathsProvider = suiteFile.getProject().createPathsProvider();
                final Optional<URI> absolutePath = new ImportSearchPaths(pathsProvider)
                        .findAbsoluteUri(suiteFile.getFile().getLocationURI(), resolvedPath);
                if (!absolutePath.isPresent()) {
                    throw new LibraryPathException("Unable to find library under '" + path + "' location.");
                }

                final ReferencedLibraryImporter importer = new ReferencedLibraryImporter();
                final RobotProject robotProject = suiteFile.getProject();
                return importer.importPythonLib(Display.getCurrent().getActiveShell(),
                        robotProject.getRuntimeEnvironment(), robotProject.getProject(), config,
                        new File(absolutePath.get()).getAbsolutePath());
            } else {
                throw new LibraryPathException(
                        "The path '" + path + "' should point to either .py file or python module directory.");
            }
        }

        private void startAutoDiscovering(final RobotProject project) {
            final List<IResource> suites = new ArrayList<>();
            suites.add(suiteFile.getFile());
            new LibrariesAutoDiscoverer(project, suites, pathOrName).start();
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

    @SuppressWarnings("serial")
    private static class LibraryPathException extends Exception {

        LibraryPathException(final String message) {
            super(message);
        }

    }
}
