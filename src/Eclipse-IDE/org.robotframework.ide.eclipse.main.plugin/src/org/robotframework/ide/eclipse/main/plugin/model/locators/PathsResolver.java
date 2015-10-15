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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Michal Anglart
 *
 */
public class PathsResolver {

    static List<IPath> getAbsoluteVariableFilesPaths(final RobotSuiteFile file) {
        return getNormalizedPaths(file.getVariablesPaths(), file.getFile().getLocation());
    }

    static List<IPath> getAbsoluteResourceFilesPaths(final RobotSuiteFile file) {
        return getNormalizedPaths(file.getResourcesPaths(), file.getFile().getLocation());
    }

    private static List<IPath> getNormalizedPaths(final List<IPath> relativePaths, final IPath location) {
        // FIXME : those paths can be parameterized (which is unfortunately
        // recommended by UG :( ) and additionally can point to python module search path
        return newArrayList(Iterables.filter(Lists.transform(relativePaths, new Function<IPath, IPath>() {

            @Override
            public IPath apply(final IPath path) {
                if (isParameterized(path)) {
                    return null;
                } else if (path.isAbsolute()) {
                    return path;
                } else {
                    final URI resolvedPath = location.toFile().toURI().resolve(path.toPortableString());
                    return new Path(resolvedPath.getPath());
                }
            }
        }), Predicates.notNull()));
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
    public static List<IPath> resolveToAbsolutePath(final RobotSuiteFile file, final String path)
            throws PathResolvingException {
        return resolveToAbsolutePath(file, new Path(path));
    }

    private static List<IPath> resolveToAbsolutePath(final RobotSuiteFile file, final IPath path)
            throws PathResolvingException {
        if (isParameterized(path)) {
            throw new PathResolvingException("Given path is parameterized");
        } else if (path.isAbsolute()) {
            return newArrayList(path);
        } else {
            try {
                final URI filePath = new URI(file.getFile().getLocation().toPortableString());
                final URI pathUri = filePath.resolve(path.toString());

                final List<IPath> paths = newArrayList();
                paths.add(new Path(pathUri.toString()));
                for (final File f : file.getProject().getModuleSearchPaths()) {
                    final URI resolvedPath = f.toURI().resolve(path.toString());
                    paths.add(new Path(resolvedPath.getPath()));
                }
                return paths;
            } catch (final URISyntaxException e) {
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
