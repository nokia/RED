/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.ChangeToFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.RedSuiteMarkerResolution;

class GeneralSettingsImportsFixes {

    static List<RedSuiteMarkerResolution> changeByPathImportToByName(final IMarker marker, final IPath invalidPath) {
        final IFile problematicFile = (IFile) marker.getResource();

        final RobotModel model = RedPlugin.getModelManager().getModel();
        final RobotProject robotProject = model.createRobotProject(problematicFile.getProject());

        final String lastSegmentWithoutExtension = invalidPath.removeFileExtension().lastSegment();
        return robotProject.getLibraryDescriptorsStream()
                .filter(LibraryDescriptor::isReferencedLibrary)
                .filter(desc -> desc.getName().equals(lastSegmentWithoutExtension))
                .map(LibraryDescriptor::getName)
                .map(ChangeToFixer::new)
                .collect(toList());
    }

    static List<RedSuiteMarkerResolution> changeByPathImportToOtherPathWithSameFileName(final IMarker marker,
            final IPath invalidPath) {
        final IFile problematicFile = (IFile) marker.getResource();

        final List<IPath> filePaths = findFilePathsWithSameLastSegment(invalidPath.lastSegment());
        return filePaths.stream()
                .sorted(createPathsComparator(problematicFile.getProject()))
                .map(path -> path.makeRelativeTo(problematicFile.getFullPath()).removeFirstSegments(1))
                .filter(path -> !invalidPath.equals(path))
                .map(path -> new ChangeToFixer(RobotProblem.getRegionOf(marker), path.toString(),
                        RedImages.getImageForFileWithExtension(path.getFileExtension())))
                .collect(toList());
    }

    private static List<IPath> findFilePathsWithSameLastSegment(final String lastSegment) {
        final List<IPath> paths = new ArrayList<>();
        try {
            ResourcesPlugin.getWorkspace().getRoot().accept(resource -> {
                if (resource.getType() == IResource.FILE && resource.getFullPath().lastSegment().equals(lastSegment)) {
                    paths.add(resource.getFullPath());
                }
                return true;
            });
        } catch (final CoreException e) {
            // ok, we'll return what we've gathered so far
        }
        return paths;
    }

    private static Comparator<IPath> createPathsComparator(final IProject project) {
        return (path1, path2) -> {
            final String projectFolderName = project.getFullPath().segment(0);
            final boolean isFromProject1 = path1.segment(0).equals(projectFolderName);
            final boolean isFromProject2 = path2.segment(0).equals(projectFolderName);
            return Boolean.compare(isFromProject2, isFromProject1);
        };
    }
}
