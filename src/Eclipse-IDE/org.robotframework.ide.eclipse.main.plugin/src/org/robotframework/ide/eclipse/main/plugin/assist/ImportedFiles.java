/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.robotframework.ide.eclipse.main.plugin.project.ASuiteFileDescriber;

import com.google.common.annotations.VisibleForTesting;

class ImportedFiles {

    static List<IFile> getPythonFiles() {
        final IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
        return getMatchingFiles(wsRoot, new FileMatcher() {

            @Override
            public boolean matches(final IFile file) {
                return "py".equalsIgnoreCase(file.getFileExtension());
            }
        });
    }

    static List<IFile> getResourceFiles(final IFile importingFile) {
        final IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
        return getMatchingFiles(wsRoot, new FileMatcher() {

            @Override
            public boolean matches(final IFile file) {
                return !file.equals(importingFile) && ASuiteFileDescriber.isResourceFile(file);
            }
        });
    }

    private static List<IFile> getMatchingFiles(final IResource wsRoot, final FileMatcher matcher) {
        final List<IFile> matchingFiles = newArrayList();
        try {
            wsRoot.accept(new IResourceVisitor() {

                @Override
                public boolean visit(final IResource resource) throws CoreException {
                    if (resource.getType() == IResource.FILE) {
                        final IFile file = (IFile) resource;
                        if (matcher.matches(file)) {
                            matchingFiles.add(file);
                        }
                    }
                    return true;
                }
            });
        } catch (final CoreException e) {
            // ok, we'll return what we've gathered so far
        }
        return matchingFiles;
    }

    static Comparator<IFile> createComparator(final String projectFolderName) {
        final Comparator<IPath> pathsComparator = createPathsComparator(projectFolderName);
        return new Comparator<IFile>() {

            @Override
            public int compare(final IFile file1, final IFile file2) {
                return pathsComparator.compare(file1.getFullPath(), file2.getFullPath());
            }
        };
    }

    @VisibleForTesting
    static Comparator<IPath> createPathsComparator(final String projectFolderName) {
        return new Comparator<IPath>() {

            @Override
            public int compare(final IPath path1, final IPath path2) {

                if (path1.segment(0).equals(projectFolderName) && !path2.segment(0).equals(projectFolderName)) {
                    return -1;
                } else if (!path1.segment(0).equals(projectFolderName) && path2.segment(0).equals(projectFolderName)) {
                    return 1;
                } else {
                    int i = 0;
                    for (; i < path1.segmentCount() && i < path2.segmentCount(); i++) {
                        if (i == path1.segmentCount() - 1 && i < path2.segmentCount() - 1) {
                            return -1;
                        } else if (i < path1.segmentCount() - 1 && i == path2.segmentCount() - 1) {
                            return 1;
                        }

                        final int segmentResult = path1.segment(i).compareTo(path2.segment(i));
                        if (segmentResult != 0) {
                            return segmentResult;
                        }
                    }

                    if (i >= path1.segmentCount() && i >= path2.segmentCount()) {
                        return 0;
                    }
                    return i < path1.segmentCount() ? -1 : 1;
                }
            }
        };
    }

    private interface FileMatcher {

        boolean matches(IFile file);
    }
}
