package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

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
        return name.substring(0, name.length() - ("." + path.getFileExtension()).length());
    }
}
