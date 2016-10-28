/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.model.locators.PathsResolver;
import org.robotframework.ide.eclipse.main.plugin.model.locators.PathsResolver.PathResolvingException;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.SearchPath;

import com.google.common.base.Optional;

public class ImportSearchPaths {

    private final RobotProject project;

    public ImportSearchPaths(final RobotProject project) {
        this.project = project;
    }

    public Collection<IPath> getAbsolutePaths(final RobotSuiteFile suiteFile, final IPath relativeImportPath) {
        final List<IPath> paths = new ArrayList<>();

        final IPath wsRelativePath = PathsConverter.fromResourceRelativeToWorkspaceRelative(suiteFile.getFile(),
                relativeImportPath);
        final IResource resource = suiteFile.getFile().getWorkspace().getRoot().findMember(wsRelativePath);
        if (resource != null && resource.getLocation() != null) {
            paths.add(resource.getLocation());
        }

        final List<IPath> modulesPaths = PathsResolver
                .resolveToAbsolutePossiblePaths(suiteFile.getProject().getModuleSearchPaths(), relativeImportPath);
        for (final IPath moduleSearchPath : modulesPaths) {
            paths.add(moduleSearchPath);
        }

        final RobotProjectConfig configuration = project.getRobotProjectConfig();
        if (configuration != null) {
            for (final SearchPath searchPath : configuration.getPythonPath()) {
                try {
                    final File parentPath = searchPath.toAbsolutePath(project.getProject(),
                            configuration.getRelativityPoint());
                    final IPath path = PathsResolver.resolveToAbsolutePath(
                            new Path(parentPath.getAbsolutePath() + File.separator), relativeImportPath);
                    paths.add(path);
                } catch (final PathResolvingException e) {
                    // we don't want to handle syntax-problematic paths
                }
            }
        }
        return paths;
    }

    public Optional<MarkedPath> getAbsolutePath(final RobotSuiteFile suiteFile, final IPath relativeImportPath) {
        final IPath wsRelativePath = PathsConverter.fromResourceRelativeToWorkspaceRelative(suiteFile.getFile(),
                relativeImportPath);
        final IResource resource = suiteFile.getFile().getWorkspace().getRoot().findMember(wsRelativePath);
        if (resource != null && resource.exists()) {
            return Optional.of(new MarkedPath(resource.getLocation(), PathRelativityPoint.FILE));
        }

        final List<IPath> modulesPaths = PathsResolver
                .resolveToAbsolutePossiblePaths(suiteFile.getProject().getModuleSearchPaths(), relativeImportPath);
        for (final IPath moduleSearchPath : modulesPaths) {
            final File asFile = moduleSearchPath.toFile();
            if (asFile.exists()) {
                return Optional.of(new MarkedPath(moduleSearchPath, PathRelativityPoint.MODULE_SEARCH_PATH));
            }
        }

        final RobotProjectConfig configuration = project.getRobotProjectConfig();
        if (configuration == null) {
            return Optional.absent();
        }

        for (final SearchPath searchPath : configuration.getPythonPath()) {
            try {
                final File parentPath = searchPath.toAbsolutePath(project.getProject(),
                        configuration.getRelativityPoint());
                final IPath path = PathsResolver.resolveToAbsolutePath(
                        new Path(parentPath.getAbsolutePath() + File.separator),
                        relativeImportPath);
                final File asFile = new File(path.toOSString());
                if (asFile.exists()) {
                    return Optional.of(new MarkedPath(new Path(asFile.getAbsolutePath()),
                            PathRelativityPoint.PROJECT_CONFIG_PATH));
                }
            } catch (final PathResolvingException e) {
                // we don't want to handle syntax-problematic paths
            }
        }
        return Optional.absent();
    }

    public static class MarkedPath {

        private final IPath path;

        private final PathRelativityPoint relativity;

        public MarkedPath(final IPath path, final PathRelativityPoint relativity) {
            this.path = path;
            this.relativity = relativity;
        }

        public IPath getPath() {
            return path;
        }

        public PathRelativityPoint getRelativity() {
            return relativity;
        }
    }

    public static enum PathRelativityPoint {
        NONE,
        FILE,
        MODULE_SEARCH_PATH,
        PROJECT_CONFIG_PATH
    }
}
