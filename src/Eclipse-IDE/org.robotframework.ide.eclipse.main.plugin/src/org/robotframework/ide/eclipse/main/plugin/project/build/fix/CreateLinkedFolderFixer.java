/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.io.File;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.ide.undo.CreateFolderOperation;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;

import com.google.common.annotations.VisibleForTesting;

public class CreateLinkedFolderFixer implements IMarkerResolution {

    private final String resourcePath;

    public CreateLinkedFolderFixer(final String path) {
        resourcePath = path;
    }

    @Override
    public String getLabel() {
        return "Create linked folder for '" + resourcePath + "' resource";
    }

    @Override
    public void run(final IMarker marker) {
        final WorkspaceJob job = new WorkspaceJob("Creating linked folder") {

            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                return executeCreateFolderOperation(monitor, marker);
            }
        };
        job.schedule();
    }

    @VisibleForTesting
    IStatus executeCreateFolderOperation(final IProgressMonitor monitor, final IMarker marker) {
        final File resourceFile = new File(toAbsolute(resourcePath));
        final URI linkTargetPath = URI.create(addFileSchemeToPath(resourceFile.getParent().replace("\\", "/")));
        final IPath suitePath = marker.getResource().getParent().getFullPath();
        final IFolder newFolderHandle = createFolderHandle(resourceFile, marker, suitePath, "");

        AbstractOperation operation = new CreateFolderOperation(newFolderHandle, linkTargetPath, "New Folder");
        try {
            operation.execute(monitor, null);
            return Status.OK_STATUS;
        } catch (ExecutionException e) {
            return Status.CANCEL_STATUS;
        }
    }

    private static IFolder createFolderHandle(final File currentFile, final IMarker marker,
            final IPath suitePath, final String folderName) {
        final String parentContainerName = getParentContainer(currentFile).getName();
        final String newFolderName = createNewFolderName(folderName, parentContainerName);
        final IPath newFolderPath = suitePath.append(newFolderName);
        final IFolder newFolderHandle = marker.getResource().getWorkspace().getRoot().getFolder(newFolderPath);
        if (newFolderHandle.exists()) {
            final File nextParent = parentContainerName.isEmpty() ? currentFile
                    : getParentContainer(currentFile);
            return createFolderHandle(nextParent, marker, suitePath, newFolderName);
        }
        return newFolderHandle;
    }

    private static File getParentContainer(final File file) {
        return new File(file.getParent());
    }

    private static String createNewFolderName(final String folderName, final String parentContainerName) {
        if (folderName.isEmpty()) {
            return parentContainerName;
        }
        return parentContainerName.isEmpty() ? addSuffix(folderName) : parentContainerName + "_" + folderName;
    }

    private static String addSuffix(final String folderName) {
        final Pattern pattern = Pattern.compile("(.*)\\((\\d+)\\)$");
        final Matcher matcher = pattern.matcher(folderName);
        if (matcher.matches()) {
            final int oldSuffix = Integer.parseInt(matcher.group(2));
            final int newSuffix = oldSuffix + 1;
            return matcher.group(1) + "(" + newSuffix + ")";
        }
        return folderName + "(1)";
    }

    private static String toAbsolute(final String path) {
        return RedWorkspace.Paths.toAbsoluteFromWorkspaceRelativeIfPossible(Path.fromPortableString(path)).toOSString();
    }

    private static String addFileSchemeToPath(final String path) {
        return "file:/" + path;
    }
}