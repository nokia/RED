/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.nio.file.FileSystems;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.google.common.io.Files;

public class ImportSettingFilePathResolver {
    
    private ImportSettingFilePathResolver() {
    }

    public static IPath createFileRelativePath(final IPath filePath, final IPath projectPath) {
        return filePath.makeRelativeTo(projectPath);
    }

    public static IPath createFileParentRelativePath(final IPath filePath, final IPath projectPath) {
        return filePath.removeLastSegments(1).makeRelativeTo(projectPath);
    }

    public static String createResourceRelativePath(final IResource resource, final IProject currentProject) {
        if (resource.getProject().equals(currentProject)) {
            return resource.getProjectRelativePath().toString();
        } else {
            return ".." + resource.getFullPath().toString();
        }
    }

    public static String createResourceParentRelativePath(final IResource resource, final IProject currentProject) {
        if (resource.getProject().equals(currentProject)) {
            return resource.getProjectRelativePath().removeLastSegments(1).toString();
        } else {
            return ".." + resource.getFullPath().removeLastSegments(1).toString();
        }
    }
    
    public static String createFileNameWithoutExtension(final IPath path) {
        final String name = path.lastSegment();
        return Files.getNameWithoutExtension(name);
    }
    
    public static IPath createFileAbsolutePath(final IPath filePath, final IProject project) {
        java.nio.file.Path basePath = FileSystems.getDefault().getPath(project.getLocation().toString());
        java.nio.file.Path absolutePath = basePath.resolve(filePath.toString()).normalize(); 
        return new Path(absolutePath.toString());
    }
}
