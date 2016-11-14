/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.locators;

import static com.google.common.collect.Lists.newArrayList;

import java.net.URI;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.project.ImportPath;
import org.rf.ide.core.project.ImportSearchPaths;
import org.rf.ide.core.project.ResolvedImportPath;
import org.rf.ide.core.project.ResolvedImportPath.MalformedPathImportException;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
class ResourceImportsPathsResolver {

    static List<IPath> getWorkspaceRelativeResourceFilesPaths(final RobotSuiteFile file) {
        return getWorkspaceRelativePaths(file.getResourcesPaths(), file);
    }

    private static List<IPath> getWorkspaceRelativePaths(final List<String> paths, final RobotSuiteFile file) {
        final List<IPath> resultPaths = newArrayList();

        final RobotProject project = file.getProject();
        for (final String path : paths) {
            final ImportPath resourceImportPath = ImportPath.from(path);
            final Optional<ResolvedImportPath> resolvedImportPath = resolvePath(project, resourceImportPath);
            if (!resolvedImportPath.isPresent()) {
                continue;
            }

            final URI absolutePath;
            if (resourceImportPath.isAbsolute()) {
                absolutePath = resolvedImportPath.get().getUri();
            } else {
                final Optional<URI> markedUri = new ImportSearchPaths(project.createPathsProvider())
                        .findAbsoluteUri(file.getFile().getLocationURI(), resolvedImportPath.get());
                if (!markedUri.isPresent()) {
                    continue;
                }
                absolutePath = markedUri.get();
            }

            final IWorkspaceRoot workspaceRoot = file.getFile().getWorkspace().getRoot();
            final RedWorkspace redWorkspace = new RedWorkspace(workspaceRoot);
            final IResource resource = redWorkspace.forUri(absolutePath);
            if (resource != null) {
                resultPaths.add(resource.getFullPath());
            }
        }
        return resultPaths;
    }

    private static Optional<ResolvedImportPath> resolvePath(final RobotProject project, final ImportPath importPath) {
        try {
            return ResolvedImportPath.from(importPath, project.getRobotProjectHolder().getVariableMappings());
        } catch (final MalformedPathImportException e) {
            return Optional.absent();
        }
    }
}
