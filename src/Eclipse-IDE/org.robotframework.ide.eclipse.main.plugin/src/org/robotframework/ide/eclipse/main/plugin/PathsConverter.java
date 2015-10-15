/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import java.net.URI;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author Michal Anglart
 *
 */
public class PathsConverter {

    public static IPath fromResourceRelativeToWorkspaceRelative(final IResource resource, final IPath path) {
        if (path.isAbsolute()) {
            throw new IllegalArgumentException("Unable to convert absolute path");
        }
        final URI resolvedPath = resource.getLocation().toFile().toURI().resolve(path.toPortableString());
        return new Path(resolvedPath.getPath()).makeRelativeTo(resource.getWorkspace().getRoot().getLocation());
    }

    public static IPath toWorkspaceRelativeIfPossible(final IPath fullPath) {
        final IPath wsPath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
        if (wsPath.isPrefixOf(fullPath)) {
            return fullPath.makeRelativeTo(wsPath);
        } else {
            return fullPath;
        }
    }

    public static IPath toAbsoluteFromWorkspaceRelativeIfPossible(final IPath workspaceRelativePath) {
        if (workspaceRelativePath.isAbsolute()) {
            return workspaceRelativePath;
        } else {
            final IPath wsPath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
            return wsPath.append(workspaceRelativePath);
        }
    }

}
