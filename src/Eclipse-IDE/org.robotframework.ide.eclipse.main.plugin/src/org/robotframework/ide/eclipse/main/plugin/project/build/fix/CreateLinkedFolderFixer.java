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
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.ide.undo.CreateFolderOperation;

import com.google.common.annotations.VisibleForTesting;

public class CreateLinkedFolderFixer implements IMarkerResolution {

    private final String absoluteResourcePath;

    private final String resourcePath;

    public CreateLinkedFolderFixer(final String absolutePath, final String path) {
        absoluteResourcePath = absolutePath;
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
                return executeCreateFolderOperation(marker.getResource(), monitor);
            }
        };
        job.schedule();
    }

    @VisibleForTesting
    IStatus executeCreateFolderOperation(final IResource resource, final IProgressMonitor monitor) {
        final File resourceFile = new File(absoluteResourcePath);
        final IWorkspaceRoot root = resource.getWorkspace().getRoot();
        final IPath suitePath = resource.getParent().getFullPath();
        final IFolder folderHandle = createFolderHandle(resourceFile, root, suitePath, "");
        final URI linkLocation = resourceFile.getParentFile().toURI();

        final IUndoableOperation operation = new CreateFolderOperation(folderHandle, linkLocation, "New Folder");
        try {
            operation.execute(monitor, null);
            return Status.OK_STATUS;
        } catch (final ExecutionException e) {
            return Status.CANCEL_STATUS;
        }
    }

    private static IFolder createFolderHandle(final File currentFile, final IWorkspaceRoot root, final IPath suitePath,
            final String folderName) {
        final String parentContainerName = currentFile.getParentFile().getName();
        final String newFolderName = createNewFolderName(folderName, parentContainerName);
        final IPath newFolderPath = suitePath.append(newFolderName);
        final IFolder newFolderHandle = root.getFolder(newFolderPath);
        if (newFolderHandle.exists()) {
            final File nextParent = parentContainerName.isEmpty() ? currentFile : currentFile.getParentFile();
            return createFolderHandle(nextParent, root, suitePath, newFolderName);
        }
        return newFolderHandle;
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
}
