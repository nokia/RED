/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.locators;

import static com.google.common.collect.Lists.newArrayList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * @author Michal Anglart
 *
 */
class PathsNormalizer {

    static List<IPath> getNormalizedVariableFilesPaths(final RobotSuiteFile file) {
        return getNormalizedPaths(file.getVariablesPaths(), file.getFile().getFullPath());
    }

    static List<IPath> getNormalizedResourceFilesPaths(final RobotSuiteFile file) {
        return getNormalizedPaths(file.getResourcesPaths(), file.getFile().getFullPath());
    }

    private static List<IPath> getNormalizedPaths(final List<IPath> relativePaths, final IPath path) {
        // FIXME : those paths can be parameterized (which is unfortunately
        // recommended by UG :( ) and additionally can point to python module search path
        try {
            final URI filePath = new URI(path.toPortableString());
            return Lists.transform(relativePaths, new Function<IPath, IPath>() {

                @Override
                public IPath apply(final IPath path) {
                    if (path.isAbsolute()) {
                        return path;
                    } else {
                        final URI pathUri = filePath.resolve(path.toString());
                        String pathUriAsString = pathUri.getPath();
                        if (pathUriAsString.startsWith("/") && !pathUri.isAbsolute()) {
                            pathUriAsString = pathUriAsString.substring(1);
                        }
                        return new Path(pathUriAsString);
                    }
                }
            });
        } catch (final URISyntaxException e) {
            // TODO : handle
            return newArrayList();
        }
    }
}
