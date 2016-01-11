/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.locators;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

/**
 * @author Michal Anglart
 *
 */
public class PathsResolver {

    static List<IPath> getWorkspaceRelativeResourceFilesPaths(final RobotSuiteFile file) {
        return getWorkspaceRelativePaths(file.getResourcesPaths(), file);
    }

    private static List<IPath> getWorkspaceRelativePaths(final List<IPath> paths, final RobotSuiteFile file) {
        final List<IPath> resultPaths = newArrayList();

        final RobotProject project = file.getProject();
        for (final IPath path : paths) {
            final IPath resolvedPath = isParameterized(path) ? resolveParametrizedPath(project, path.toPortableString())
                    : path;

            final IPath r = normalizePath(file, resolvedPath);
            if (r != null) {
                resultPaths.add(r);
            }
        }
        return resultPaths;
    }
    
    public static IPath resolveParametrizedPath(final RobotProject project, final String path) {
        return Path.fromPortableString(project.resolve(path).replaceAll("\\\\", "/"));
    }

    private static IPath normalizePath(final RobotSuiteFile file, final IPath resolvedPath) {
        if (isParameterized(resolvedPath)) {
            return null;
        }

        final IWorkspaceRoot workspaceRoot = file.getFile().getWorkspace().getRoot();
        if (resolvedPath.isAbsolute() && workspaceRoot.getLocation().isPrefixOf(resolvedPath)) {
                return resolvedPath.makeRelativeTo(workspaceRoot.getLocation());
        } else if (!resolvedPath.isAbsolute()) {
            return PathsConverter.fromResourceRelativeToWorkspaceRelative(file.getFile(), resolvedPath);
        }
        // we don't handle the case when path is absolute, but outside of workspace. this could be
        // done in special cases, but i think we don't want to do it
        return null;
    }

    /**
     * Returns absolute path to place given in path argument which is used in given file. Exception is thrown when
     * path is parameterized. If the path is relative then target location is searched first relative to given file
     * and then relative to directories taken from project module search paths. All those searched paths will be 
     * returned.
     * 
     * @param file
     * @param path
     * @return
     */
    public static List<IPath> resolveToAbsolutePossiblePaths(final RobotSuiteFile file, final String path)
            throws PathResolvingException {
        return resolveToAbsolutePossiblePath(file, new Path(path));
    }

    private static List<IPath> resolveToAbsolutePossiblePath(final RobotSuiteFile file, final IPath path)
            throws PathResolvingException {
        final List<IPath> paths = newArrayList(resolveToAbsolutePath(file, path));
        final Escaper escaper = Escapers.builder().addEscape(' ', "%20").build();
        for (final File f : file.getProject().getModuleSearchPaths()) {
            final URI resolvedPath = f.toURI().resolve(escaper.escape(path.toString()));
            paths.add(new Path(resolvedPath.getPath()));
        }
        return paths;
    }

    public static IPath resolveToAbsolutePath(final RobotSuiteFile file, final String path)
            throws PathResolvingException {
        return resolveToAbsolutePath(file, new Path(path));
    }

    private static IPath resolveToAbsolutePath(final RobotSuiteFile file, final IPath path)
            throws PathResolvingException {
        if (isParameterized(path)) {
            throw new PathResolvingException("Given path is parameterized");
        } else if (path.isAbsolute()) {
            return path;
        } else {
            try {
                final Escaper escaper = Escapers.builder().addEscape(' ', "%20").build();

                final String portablePath = file.getFile().getLocation().toPortableString();
                final URI filePath = new URI(escaper.escape(portablePath));
                final URI pathUri = filePath.resolve(escaper.escape(path.toString()));

                return new Path(pathUri.toString().replaceAll("%20", " "));
            } catch (final URISyntaxException | IllegalArgumentException e) {
                throw new PathResolvingException("Path syntax problem", e);
            }
        }
    }

    private static boolean isParameterized(final IPath path) {
        return Pattern.compile("[@$&%]\\{[^\\}]+\\}").matcher(path.toPortableString()).find();
    }

    public static class PathResolvingException extends RuntimeException {

        public PathResolvingException(final String message) {
            super(message);
        }

        public PathResolvingException(final String message, final Throwable cause) {
            super(message, cause);
        }

    }
}
