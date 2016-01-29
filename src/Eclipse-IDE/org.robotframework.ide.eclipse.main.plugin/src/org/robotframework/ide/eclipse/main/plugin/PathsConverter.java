/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

/**
 * @author Michal Anglart
 *
 */
public class PathsConverter {

    public static Escaper getUriSpecialCharsEscaper() {
        return Escapers.builder().addEscape(' ', "%20").addEscape('%', "%25").addEscape('^', "%5e").build();
    }

    public static IPath fromResourceRelativeToWorkspaceRelative(final IResource resource, final IPath path) {
        if (path.isAbsolute()) {
            throw new IllegalArgumentException("Unable to convert absolute path");
        }
        try {
            final Escaper escaper = getUriSpecialCharsEscaper();

            final String pathWithoutSpaces = escaper.escape(path.toString());
            final URI resolvedPath = new URI(escaper.escape(resource.getFullPath().toString()))
                    .resolve(pathWithoutSpaces);
            final String resolvedPathAsString = resolvedPath.getPath();
            if (resolvedPathAsString == null) {
                return null;
            }
            return new Path(resolvedPathAsString).makeRelativeTo(resource.getWorkspace().getRoot().getLocation());
        } catch (final IllegalArgumentException | URISyntaxException e) {
            return null;
        }
    }

    public static IPath fromWorkspaceRelativeToResourceRelative(final IResource resource, final IPath path) {
        if (path.isAbsolute()) {
            throw new IllegalArgumentException("Unable to convert absolute path");
        }
        resource.getFullPath().toFile().toURI().relativize(path.toFile().toURI());
        path.toFile().toURI().relativize(resource.getFullPath().toFile().toURI());
        return path.makeRelativeTo(resource.getFullPath()).removeFirstSegments(1);
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
