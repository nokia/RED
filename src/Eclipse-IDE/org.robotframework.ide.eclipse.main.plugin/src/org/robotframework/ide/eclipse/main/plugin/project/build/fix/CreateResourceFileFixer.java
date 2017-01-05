/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

/**
 * @author Lukasz Wlodarczyk
 */
public class CreateResourceFileFixer extends RedSuiteMarkerResolution {

    private final Optional<ICompletionProposal> proposal;

    private final String label;

    public static CreateResourceFileFixer createFixer(final String nonExistingFile, final IMarker marker) {
        final IPath pathToCreate = getValidPathToCreate(marker);
        if (pathToCreate == null || !isExtensionValid(pathToCreate.getFileExtension())) {
            return new CreateResourceFileFixer(CreateResourceFileFixer.createEmptyProposal(),
                    "Missing resource file cannot be auto-created");
        } else {
            return new CreateResourceFileFixer(
                    CreateResourceFileFixer.createProposal(marker, pathToCreate, nonExistingFile),
                    "Create missing " + nonExistingFile + " file");
        }
    }

    @VisibleForTesting
    static IPath getValidPathToCreate(final IMarker marker) {
        final String srcPath = marker.getAttribute(AdditionalMarkerAttributes.PATH, null);
        final File file = new File(srcPath);
        try {
            file.getCanonicalPath();
        } catch (IOException e) {
            return null;
        }
        final IPath fullPath = new Path(ResolvedImportPath.from(ImportPath.from(srcPath))
                .get()
                .resolveInRespectTo(marker.getResource().getLocationURI())
                .toString());
        final IPath workspaceDir = new Path(marker.getResource().getWorkspace().getRoot().getLocationURI().toString());
        if (workspaceDir.matchingFirstSegments(fullPath) != workspaceDir.segmentCount()) {
            return null;
        }
        final IPath pathToCreate = new Path(ResolvedImportPath
                .reverseUriSpecialCharsEscapes(fullPath.removeFirstSegments(workspaceDir.segmentCount()).toString()));
        if (pathToCreate == null || pathToCreate.segmentCount() < 2
                || !ResourcesPlugin.getWorkspace().getRoot().getProject(pathToCreate.segment(0)).isAccessible()
                || !pathToCreate.isValidPath(pathToCreate.toPortableString())) {
            return null;
        } else {
            return pathToCreate;
        }
    }

    // this method should be implemented elsewhere
    private static boolean isExtensionValid(final String extension) {
        final List<String> validExts = Arrays
                .asList(new String[] { "html", "htm", "xhtml", "tsv", "txt", "rst", "robot", "rest" });
        return extension != null && validExts.contains(extension.toLowerCase());
    }

    public CreateResourceFileFixer(final Optional<ICompletionProposal> proposal, final String label) {
        this.proposal = proposal;
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(IMarker marker, IDocument document,
            RobotSuiteFile suiteModel) {
        return proposal;
    }

    private static Optional<ICompletionProposal> createProposal(IMarker marker, IPath path, String res) {
        MissingResourceFileCompletionProposal proposal = new MissingResourceFileCompletionProposal(
                "Create missing " + res + " file",
                "<b>" + res
                        + "</b><br> file will be created and opened for edition.<br><br>Resource path must be valid, "
                        + "inside project directory and must include file extension. Any missing parent directories will be also created.",
                marker, path);
        return Optional.<ICompletionProposal> of(proposal);
    }

    private static Optional<ICompletionProposal> createEmptyProposal() {
        EmptyCompletionProposal proposal = new EmptyCompletionProposal("Missing resource file cannot be auto-created",
                "Please check your file path if you want to use this Quick Fix."
                        + "<br>Only valid and legal path to the file with explicit file extension "
                        + "inside existing open project in this workspace can be used."
                        + "<br>All missing parent directories inside project will be auto-generated.");
        return Optional.<ICompletionProposal> of(proposal);
    }
}
