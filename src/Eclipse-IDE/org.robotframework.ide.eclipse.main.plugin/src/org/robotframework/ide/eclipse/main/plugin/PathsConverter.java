/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * @author Michal Anglart
 *
 */
public class PathsConverter {

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
