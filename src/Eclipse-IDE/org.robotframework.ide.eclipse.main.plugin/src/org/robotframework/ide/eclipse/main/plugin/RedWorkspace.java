/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import java.io.File;
import java.net.URI;
import java.util.Optional;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;

public class RedWorkspace {

    private final IWorkspaceRoot root;

    public RedWorkspace() {
        this(ResourcesPlugin.getWorkspace().getRoot());
    }

    public RedWorkspace(final IWorkspaceRoot root) {
        this.root = root;
    }

    public IResource forUri(final URI absolutePath) {
        final Optional<IContainer> container = containerForUri(absolutePath);
        if (container.isPresent()) {
            return container.get();
        }
        return fileForUri(absolutePath).orElse(null);
    }

    public boolean hasContainerForUri(final URI absolutePath) {
        return containerForUri(absolutePath).isPresent();
    }

    public Optional<IContainer> containerForUri(final URI absolutePath) {
        final IContainer[] containers = absolutePath == null ? new IContainer[0]
                : root.findContainersForLocationURI(absolutePath);
        for (final IContainer container : containers) {
            final IPath realLocation = container.getLocation();
            if (realLocation != null) {
                final File realLocationAsFile = realLocation.toFile();
                if (realLocationAsFile.exists() && realLocationAsFile.isDirectory()) {
                    return Optional.of(container);
                }
            }
        }
        return Optional.empty();
    }

    public boolean hasFileForUri(final URI absolutePath) {
        return fileForUri(absolutePath).isPresent();
    }

    public Optional<IFile> fileForUri(final URI absolutePath) {
        final IFile[] files = absolutePath == null ? new IFile[0] : root.findFilesForLocationURI(absolutePath);
        for (final IFile file : files) {
            final IPath realLocation = file.getLocation();
            if (realLocation != null) {
                final File realLocationAsFile = realLocation.toFile();
                if (realLocationAsFile.exists() && realLocationAsFile.isFile()) {
                    return Optional.of(file);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Gets local uri with file:// scheme (if possible) for given resource or returns actual
     * location uri otherwise
     */
    public static URI tryToGetLocalUri(final IResource workspaceResource) {
        return getLocalFile(workspaceResource).map(File::toURI)
                .orElseGet(() -> workspaceResource.getLocationURI());
    }

    /**
     * Gets local file for given resource if possible
     */
    public static Optional<File> getLocalFile(final IResource workspaceResource) {
        try {
            final URI workspaceUri = workspaceResource.getLocationURI();
            final IFileSystem fileSystem = EFS.getFileSystem(workspaceUri.getScheme());
            return Optional.ofNullable(fileSystem.getStore(workspaceUri)).flatMap(RedWorkspace::toLocalFile);

        } catch (final CoreException e) {
            return Optional.empty();
        }
    }

    private static Optional<File> toLocalFile(final IFileStore fileStore) {
        try {
            return Optional.ofNullable(fileStore.toLocalFile(EFS.NONE, new NullProgressMonitor()));
        } catch (final CoreException e) {
            return Optional.empty();
        }
    }

    public static class Paths {

        public static IPath toWorkspaceRelativeIfPossible(final IPath fullPath) {
            return toRelativeIfPossible(ResourcesPlugin.getWorkspace().getRoot().getLocation(), fullPath);
        }

        private static IPath toRelativeIfPossible(final IPath relativityPoint, final IPath fullPath) {
            if (relativityPoint.isPrefixOf(fullPath)) {
                return fullPath.makeRelativeTo(relativityPoint);
            } else {
                return fullPath;
            }
        }

        public static IPath toAbsoluteFromWorkspaceRelativeIfPossible(final IPath workspaceRelativePath) {
            if (workspaceRelativePath.isAbsolute()) {
                return workspaceRelativePath;
            } else {
                final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                final IResource member = root.findMember(workspaceRelativePath);
                return member == null ? root.getLocation().append(workspaceRelativePath) : member.getLocation();
            }
        }
    }
}
