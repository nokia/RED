/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.rf.ide.core.project.ImportPath;
import org.rf.ide.core.project.ResolvedImportPath;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;

import com.google.common.base.Optional;

/**
 * @author Lukasz Wlodarczyk
 */
public class CreateResourceFileFixer extends RedSuiteMarkerResolution {

    private final String nonExistingResource;

    public CreateResourceFileFixer(final String nonExistingFile) {
        this.nonExistingResource = nonExistingFile;
    }

    @Override
    public String getLabel() {
        return "Create missing " + nonExistingResource + " file.";
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(IMarker marker, IDocument document,
            RobotSuiteFile suiteModel) {
        final String srcPath = marker.getAttribute(AdditionalMarkerAttributes.PATH, null);
        final File file = new File(srcPath);
        try {
            file.getCanonicalPath();
        } catch (IOException e) {
            return createEmptyProposal();
        }
        final IPath fullPath = new Path(ResolvedImportPath
                .from(ImportPath.from(srcPath))
                .get()
                .resolveInRespectTo(suiteModel.getFile().getParent().getLocation().toFile().toURI())
                .toString());
        final IPath workspaceDir = new Path(
                ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().toURI().toString());
        if (workspaceDir.matchingFirstSegments(fullPath) != workspaceDir.segmentCount()) {
            return createEmptyProposal();
        }
        final IPath path = new Path(ResolvedImportPath
                .reverseUriSpecialCharsEscapes(fullPath.removeFirstSegments(workspaceDir.segmentCount()).toString()));
        if (path.segmentCount() < 2) {
            return createEmptyProposal();
        }
        if (!ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0)).isAccessible()) {
            return createEmptyProposal();
        }
        final String ext = path.getFileExtension();
        if (path != null && path.isValidPath(path.toPortableString()) && ext != null && !ext.isEmpty()) {
            return createProposal(marker, suiteModel, path);
        } else {
            return createEmptyProposal();
        }
    }

    private Optional<ICompletionProposal> createProposal(IMarker marker, RobotSuiteFile suiteModel, IPath path) {
        MissingResourceFileCompletionProposal proposal = new MissingResourceFileCompletionProposal(getLabel(),
                "<b>" + nonExistingResource
                        + "</b><br> file will be created and opened for edition.<br><br>Resource path must be valid, "
                        + "inside project directory and must include file extension. Any missing parent directories will be also created.",
                marker, path);
        return Optional.<ICompletionProposal> of(proposal);
    }

    private Optional<ICompletionProposal> createEmptyProposal() {
        EmptyCompletionProposal proposal = new EmptyCompletionProposal(
                "Missing resource file cannot be auto-created",
                "Please check your file path if you want to use this Quick Fix."
                        + "<br>Only valid and legal path to the file with explicit file extension "
                        + "inside existing open project in this workspace can be used."
                        + "<br>All missing parent directories inside project will be auto-generated.");
        return Optional.<ICompletionProposal> of(proposal);
    }
}
