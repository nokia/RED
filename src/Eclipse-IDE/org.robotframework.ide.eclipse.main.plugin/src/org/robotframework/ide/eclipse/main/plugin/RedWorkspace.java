/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import java.io.File;
import java.net.URI;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

public class RedWorkspace {

    private final IWorkspaceRoot root;

    public RedWorkspace(final IWorkspaceRoot root) {
        this.root = root;
    }

    public IResource forUri(final URI absolutePath) {
        final IContainer[] containers = root.findContainersForLocationURI(absolutePath);
        for (final IContainer container : containers) {
            final IPath realLocation = container.getLocation();
            if (realLocation != null) {
                final File realLocationAsFile = realLocation.toFile();
                if (realLocationAsFile.exists() && realLocationAsFile.isDirectory()) {
                    return container;
                }
            }
        }
        final IFile[] files = root.findFilesForLocationURI(absolutePath);
        for (final IFile file : files) {
            final IPath realLocation = file.getLocation();
            if (realLocation != null) {
                final File realLocationAsFile = realLocation.toFile();
                if (realLocationAsFile.exists() && realLocationAsFile.isFile()) {
                    return file;
                }
            }
        }
        return null;
    }

    public static class Paths {

        public static IPath fromWorkspaceRelativeToResourceRelative(final IResource resource, final IPath path) {
            if (path.isAbsolute()) {
                throw new IllegalArgumentException("Unable to convert absolute path");
            }
            resource.getFullPath().toFile().toURI().relativize(path.toFile().toURI());
            path.toFile().toURI().relativize(resource.getFullPath().toFile().toURI());
            return path.makeRelativeTo(resource.getFullPath()).removeFirstSegments(1);
        }

        public static IPath toWorkspaceRelativeIfPossible(final IPath fullPath) {
            return toRelativeIfPossible(ResourcesPlugin.getWorkspace().getRoot().getLocation(), fullPath);
        }

        public static IPath toRelativeIfPossible(final IPath relativityPoint, final IPath fullPath) {
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
