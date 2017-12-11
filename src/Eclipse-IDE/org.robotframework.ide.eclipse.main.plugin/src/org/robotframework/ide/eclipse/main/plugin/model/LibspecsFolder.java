/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

public class LibspecsFolder {

    private static final String LIBSPEC_FILE_EXTENSION = ".libspec";

    private static final String FOLDER_NAME = "libspecs";

    private final IFolder folder;

    public LibspecsFolder(final IFolder folder) {
        this.folder = folder;
    }

    public IFolder getResource() {
        return folder;
    }

    public static LibspecsFolder get(final IProject project) {
        return new LibspecsFolder(project.getFolder(FOLDER_NAME));
    }

    public static LibspecsFolder createIfNeeded(final IProject project) throws CoreException {
        final LibspecsFolder libspecsFolder = get(project);
        if (!libspecsFolder.exists()) {
            libspecsFolder.folder.create(IResource.FORCE | IResource.DERIVED, true, null);
        }
        return libspecsFolder;
    }

    public boolean exists() {
        return folder.exists();
    }

    public void removeNonSpecResources() throws CoreException {
        if (!folder.exists()) {
            return;
        }
        for (final IResource resource : folder.members(IContainer.INCLUDE_HIDDEN)) {
            if (resource.exists() && !resource.getName().endsWith(LIBSPEC_FILE_EXTENSION)) {
                resource.delete(true, null);
            }
        }
    }

    public void removeContent() throws CoreException {
        if (!folder.exists()) {
            return;
        }
        for (final IResource resource : folder.members(IContainer.INCLUDE_HIDDEN)) {
            if (resource.exists()) {
                resource.delete(true, null);
            }
        }
    }

    public void remove() throws CoreException {
        if (exists()) {
            final IResource project = folder.getProject();
            folder.delete(true, null);
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
        }
    }

    public boolean shouldRegenerateLibspecs(final IResourceDelta delta, final int kind) {
        if (delta == null || kind == IncrementalProjectBuilder.FULL_BUILD) {
            return true;
        }

        final IFile cfgFile = folder.getProject().getFile(RobotProjectConfig.FILENAME);
        // full build is being performed or config file has changed
        return delta.findMember(cfgFile.getProjectRelativePath()) != null
                || delta.findMember(folder.getProjectRelativePath()) != null
                        && libspecFileChanged(delta.findMember(folder.getProjectRelativePath()));
    }

    private boolean libspecFileChanged(final IResourceDelta changedLibspecFolder) {
        if (changedLibspecFolder.getKind() == IResourceDelta.ADDED
                || changedLibspecFolder.getKind() == IResourceDelta.REMOVED) {
            return true;
        } else if (changedLibspecFolder.getKind() == IResourceDelta.CHANGED) {
            return changedLibspecFolder.getAffectedChildren().length > 0;
        }
        return false;
    }

    public List<IFile> collectLibspecsToRegenerate(final List<String> libraryNames, final String version) {
        return libraryNames.stream()
                .map(this::getSpecFile)
                .filter(specFile -> !specFile.exists() || !hasSameVersion(specFile, version))
                .collect(toList());
    }

    private static boolean hasSameVersion(final IFile specFile, final String version) {
        return version.startsWith(String.format("Robot Framework %s (", LibrarySpecification.getVersion(specFile)));
    }

    public IFile getSpecFile(final String libraryName) {
        return getFile(libraryName + LIBSPEC_FILE_EXTENSION);
    }

    public IFile getFile(final String name) {
        return folder.getFile(name);
    }

    public IResource[] members() throws CoreException {
        return folder.members();
    }
}
