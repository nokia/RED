/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static com.google.common.base.Functions.toStringFunction;
import static com.google.common.collect.Lists.transform;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.project.ResolvedImportPath;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.RelativeTo;
import org.rf.ide.core.project.RobotProjectConfig.RelativityPoint;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.escape.Escaper;

public class RedEclipseProjectConfig {

    private final RobotProjectConfig config;

    public RedEclipseProjectConfig(final RobotProjectConfig config) {
        this.config = config;
    }

    public File toAbsolutePath(final SearchPath path, final IProject containingProject) {
        final IPath asPath = new Path(path.getLocation());
        if (asPath.isAbsolute()) {
            return asPath.toFile();
        }
        final File wsAbsolute = resolveToAbsolutePath(
                getRelativityLocation(config.getRelativityPoint(), containingProject), asPath)
                .toFile();
        final IPath wsRelative = RedWorkspace.Paths
                .toWorkspaceRelativeIfPossible(new Path(wsAbsolute.getAbsolutePath()));
        final IResource member = containingProject.getWorkspace().getRoot().findMember(wsRelative);
        if (member == null) {
            return wsAbsolute;
        } else {
            return member.getLocation().toFile();
        }
    }

    @VisibleForTesting
    static IPath resolveToAbsolutePath(final IPath base, final IPath child) throws PathResolvingException {
        if (child.isAbsolute()) {
            return child;
        } else {
            try {
                final Escaper escaper = ResolvedImportPath.URI_SPECIAL_CHARS_ESCAPER;

                final String portablePath = base.toPortableString();
                final URI filePath = new URI(escaper.escape(portablePath));
                final URI pathUri = filePath.resolve(escaper.escape(child.toString()));
                return new Path(ResolvedImportPath.reverseUriSpecialCharsEscapes(pathUri.toString()));
            } catch (final Exception e) {
                throw new PathResolvingException("Unable to parse path", e);
            }
        }
    }

    private static IPath getRelativityLocation(final RelativityPoint relativityPoint,
            final IProject containingProject) {
        final IPath result = relativityPoint.getRelativeTo() == RelativeTo.WORKSPACE
                ? containingProject.getWorkspace().getRoot().getLocation() : containingProject.getLocation();
        return result.addTrailingSeparator();
    }

    public EnvironmentSearchPaths createEnvironmentSearchPaths(final IProject project) {
        return new EnvironmentSearchPaths(
                transform(getResolvedClassPaths(project), toStringFunction()),
                transform(getResolvedPythonPaths(project), toStringFunction()));
    }

    private List<File> getResolvedPythonPaths(final IProject containingProject) {
        return getResolvedPaths(containingProject, config.getPythonPath());
    }

    private List<File> getResolvedClassPaths(final IProject containingProject) {
        return getResolvedPaths(containingProject, config.getClassPath());
    }

    private List<File> getResolvedPaths(final IProject containingProject, final List<SearchPath> paths) {
        final List<File> files = new ArrayList<>();
        for (final SearchPath path : paths) {
            try {
                files.add(toAbsolutePath(path, containingProject));
            } catch (final PathResolvingException e) {
                // nothing to do in that case
            }
        }
        return files;
    }

    public static class PathResolvingException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public PathResolvingException(final String message) {
            super(message);
        }

        public PathResolvingException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
