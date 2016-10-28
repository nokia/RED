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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.model.ImportSearchPaths;
import org.robotframework.ide.eclipse.main.plugin.model.ImportSearchPaths.MarkedPath;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.base.Optional;
import com.google.common.escape.Escaper;

/**
 * @author Michal Anglart
 *
 */
public class PathsResolver {

    private static final Pattern parametrizedPathPattern = Pattern.compile("[@$&%]\\{[^\\}]+\\}");

    private static final Pattern notEscapedWindowsPathSeparatorPattern = Pattern
            .compile("^.*[^\\\\][\\\\]{1}[^\\\\].*$");

    static List<IPath> getWorkspaceRelativeResourceFilesPaths(final RobotSuiteFile file) {
        return getWorkspaceRelativePaths(file.getResourcesPaths(), file);
    }

    private static List<IPath> getWorkspaceRelativePaths(final List<IPath> paths, final RobotSuiteFile file) {
        final List<IPath> resultPaths = newArrayList();

        final RobotProject project = file.getProject();
        for (final IPath path : paths) {
            final IPath resolvedPath = isParameterized(path) ? resolveParametrizedPath(project, path.toPortableString())
                    : path;

            if (isParameterized(resolvedPath)) {
                // it should be all-resolved by now
                continue;
            }
            
            final IPath absolutePath;
            if (resolvedPath.isAbsolute()) {
                absolutePath = resolvedPath;
            } else {
                final Optional<MarkedPath> markedAbsolutePath = new ImportSearchPaths(file.getProject())
                        .getAbsolutePath(file, resolvedPath);
                absolutePath = markedAbsolutePath.isPresent() ? markedAbsolutePath.get().getPath() : null;
            }

            final IWorkspaceRoot workspaceRoot = file.getFile().getWorkspace().getRoot();
            if (workspaceRoot.getLocation().isPrefixOf(absolutePath)) {
                resultPaths.add(absolutePath.makeRelativeTo(workspaceRoot.getLocation()));
            } else {
                final IFile[] files = workspaceRoot.findFilesForLocationURI(absolutePath.toFile().toURI());
                if (files.length > 0) {
                    resultPaths.add(files[0].getFullPath());
                }
            }
        }
        return resultPaths;
    }
    
    public static IPath resolveParametrizedPath(final RobotProject project, final String path) {
        return Path.fromPortableString(project.resolve(path).replaceAll("\\\\", "/"));
    }

    public static IPath resolveParametrizedPath(final RobotProject project, final IPath path) {
        return resolveParametrizedPath(project, path.toPortableString());
    }

    /**
     * Returns absolute paths obtained by resolving given locations against given relative path.
     * 
     * @param file
     * @param path
     * @return
     */

    public static List<IPath> resolveToAbsolutePossiblePaths(final List<File> files, final IPath path)
            throws PathResolvingException {
        final List<IPath> paths = new ArrayList<>();
        final Escaper escaper = PathsConverter.getUriSpecialCharsEscaper();
        for (final File f : files) {
            final String resolvedPath = f.toURI().resolve(escaper.escape(path.toString())).getPath();
            if (resolvedPath != null) {
                paths.add(new Path(resolvedPath));
            }
        }
        return paths;
    }

    public static IPath resolveToAbsolutePath(final RobotSuiteFile file, final String path)
            throws PathResolvingException {
        return resolveToAbsolutePath(file, new Path(path));
    }

    private static IPath resolveToAbsolutePath(final RobotSuiteFile file, final IPath path)
            throws PathResolvingException {
        return resolveToAbsolutePath(file.getFile() != null ? file.getFile().getLocation() : null, path);
    }

    /**
     * Resolves given relative path to absolute path. The path is treated as relative to
     * the path given in {@code absolute} parameter. The {@code relative} path is returned
     * if it is absolute.
     * 
     * @param base
     * @param child
     * @return
     * @throws PathResolvingException
     *             is thrown when:
     *             - path is RF-parameterized (using ${var})
     *             - when given absolute path is null
     *             - in case of paths syntax problems
     */
    public static IPath resolveToAbsolutePath(final IPath base, final IPath child)
            throws PathResolvingException {
        if (isParameterized(child)) {
            throw new PathResolvingException("Given path is parameterized");
        } else if (child.isAbsolute()) {
            return child;
        } else if (base == null) {
            throw new PathResolvingException("Given path is not located physically in the file system");
        } else {
            try {
                final Escaper escaper = PathsConverter.getUriSpecialCharsEscaper();

                final String portablePath = base.toPortableString();
                final URI filePath = new URI(escaper.escape(portablePath));
                final URI pathUri = filePath.resolve(escaper.escape(child.toString()));

                return new Path(PathsConverter.reverseUriSpecialCharsEscapes(pathUri.toString()));
            } catch (final URISyntaxException | IllegalArgumentException e) {
                throw new PathResolvingException("Path syntax problem", e);
            }
        }
    }

    public static boolean isParameterized(final IPath path) {
        return parametrizedPathPattern.matcher(path.toPortableString()).find();
    }
    
    public static boolean hasNotEscapedWindowsPathSeparator(final String path) {
        return notEscapedWindowsPathSeparatorPattern.matcher(path).find(); // e.g. c:\lib.py
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
