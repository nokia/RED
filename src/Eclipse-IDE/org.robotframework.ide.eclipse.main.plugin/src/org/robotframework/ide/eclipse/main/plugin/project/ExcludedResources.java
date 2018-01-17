/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ExcludedFolderPath;

public class ExcludedResources {

    public static boolean isHiddenInEclipse(final IResource resource) {
        for (final String segment : resource.getFullPath().segments()) {
            if (!segment.isEmpty() && segment.charAt(0) == '.') {
                return true;
            }
        }
        return false;
    }

    public static boolean isInsideExcludedPath(final IResource resource, final RobotProjectConfig projectConfig) {
        final List<ExcludedFolderPath> excludedPaths = projectConfig.getExcludedPath();
        for (final ExcludedFolderPath excludedPath : excludedPaths) {
            if (Path.fromPortableString(excludedPath.getPath()).isPrefixOf(resource.getProjectRelativePath())) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasRequiredSize(final IFile file, final RobotProjectConfig projectConfig) {
        if (projectConfig.isValidatedFileSizeCheckingEnabled()) {
            final IPath fileLocation = file.getLocation();
            if (fileLocation != null) {
                final long fileSizeInKilobytes = fileLocation.toFile().length() / 1024;
                long maxFileSize;
                try {
                    maxFileSize = Long.parseLong(projectConfig.getValidatedFileMaxSize());
                } catch (final NumberFormatException e) {
                    maxFileSize = Long.parseLong(projectConfig.getValidatedFileDefaultMaxSize());
                }
                return fileSizeInKilobytes <= maxFileSize;
            }
        }
        return true;
    }
}
