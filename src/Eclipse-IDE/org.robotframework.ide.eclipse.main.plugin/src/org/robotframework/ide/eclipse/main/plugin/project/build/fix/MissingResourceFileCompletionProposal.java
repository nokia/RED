/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.library.SourceOpeningSupport;
import org.robotframework.red.graphics.ImagesManager;

/**
 * @author Lukasz Wlodarczyk
 */
class MissingResourceFileCompletionProposal implements ICompletionProposal {

    private final String label, additionalInfo;

    private final IMarker marker;

    private final IPath path;

    private final IProject project;

    MissingResourceFileCompletionProposal(final String label, final String additionalInfo, final IMarker marker,
            final IPath path) {
        this.label = label;
        this.additionalInfo = additionalInfo;
        this.marker = marker;
        this.path = path.removeFirstSegments(1);
        this.project = marker.getResource().getWorkspace().getRoot().getProject(path.segment(0));
    }

    @Override
    public void apply(final IDocument document) {
        createPathFolders(path);
        final IFile file = project.getFile(path.toPortableString());
        if (!file.exists()) {
            if (createFile(file)) {
                try {
                    marker.delete();
                } catch (final CoreException e) {
                    StatusManager.getManager().handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, e.getMessage()),
                            StatusManager.SHOW);
                }
                SourceOpeningSupport
                        .tryToOpenInEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
            } else {
                MessageDialog.openError(Display.getDefault().getActiveShell(), "Cannot create the file",
                        "Unfortunately, this file could not be created properly.");
            }
        }
    }

    @Override
    public Point getSelection(final IDocument document) {
        return null;
    }

    @Override
    public String getAdditionalProposalInfo() {
        return additionalInfo;
    }

    @Override
    public String getDisplayString() {
        return label;
    }

    @Override
    public Image getImage() {
        return ImagesManager.getImage(RedImages.getImageForFileWithExtension(path.getFileExtension()));
    }

    @Override
    public IContextInformation getContextInformation() {
        return null;
    }

    private boolean createFile(final IFile file) {
        try {
            file.create(new ByteArrayInputStream(new byte[0]), true, null);
        } catch (final CoreException e1) {
            return false;
        }
        return true;
    }

    private boolean createPathFolders(final IPath path) {
        for (int i = 0; i < path.segmentCount() - 1; i++) {
            final IFolder folder = project.getFolder(path.uptoSegment(i + 1));
            if (!folder.exists()) {
                try {
                    folder.create(IResource.NONE, true, null);
                } catch (final CoreException e) {
                    return false;
                }
            }
        }
        return true;
    }
}
