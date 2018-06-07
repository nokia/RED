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
import java.util.Optional;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.rf.ide.core.RedURI;
import org.rf.ide.core.project.ImportPath;
import org.rf.ide.core.project.ResolvedImportPath;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Lukasz Wlodarczyk
 */
public class CreateResourceFileFixer extends RedSuiteMarkerResolution {

    private final Optional<ICompletionProposal> proposal;

    private final String label;

    public static CreateResourceFileFixer createFixer(final String nonExistingFile, final IMarker marker) {
        return getValidPathToCreate(marker)
                .map(path -> new CreateResourceFileFixer(
                        CreateResourceFileFixer.createProposal(marker, path, nonExistingFile),
                        "Create missing '" + nonExistingFile + "' file"))
                .orElseGet(() -> new CreateResourceFileFixer(CreateResourceFileFixer.createEmptyProposal(),
                        "Missing resource file cannot be auto-created"));
    }

    @VisibleForTesting
    static Optional<IPath> getValidPathToCreate(final IMarker marker) {
        final String srcPath = marker.getAttribute(AdditionalMarkerAttributes.PATH, null);
        final File file = new File(srcPath);
        try {
            file.getCanonicalPath();
        } catch (final IOException e) {
            return Optional.empty();
        }
        final IPath fullPath = new Path(ResolvedImportPath.from(ImportPath.from(srcPath))
                .get()
                .resolveInRespectTo(marker.getResource().getLocationURI())
                .toString());
        final IPath workspaceDir = new Path(marker.getResource().getWorkspace().getRoot().getLocationURI().toString());
        if (workspaceDir.matchingFirstSegments(fullPath) != workspaceDir.segmentCount()) {
            return Optional.empty();
        }
        final IPath pathToCreate = new Path(RedURI
                .reverseUriSpecialCharsEscapes(fullPath.removeFirstSegments(workspaceDir.segmentCount()).toString()));
        if (isValidPathToCreate(pathToCreate)) {
            return Optional.of(pathToCreate);
        } else {
            return Optional.empty();
        }
    }

    private static boolean isValidPathToCreate(final IPath path) {
        return path.segmentCount() > 1 && isExtensionValid(path.getFileExtension())
                && ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0)).isAccessible()
                && path.isValidPath(path.toPortableString());
    }

    // this method should be implemented elsewhere
    private static boolean isExtensionValid(final String extension) {
        final List<String> validExts = Arrays.asList("html", "htm", "xhtml", "tsv", "txt", "rst", "robot", "rest");
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
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        return proposal;
    }

    private static Optional<ICompletionProposal> createProposal(final IMarker marker, final IPath path,
            final String res) {
        final MissingResourceFileCompletionProposal proposal = new MissingResourceFileCompletionProposal(
                "Create missing '" + res + "' file",
                "<b>" + res
                        + "</b><br> file will be created and opened for edition.<br><br>Resource path must be valid, "
                        + "inside project directory and must include robot resource extension. Any missing parent directories will be also created.",
                marker, path);
        return Optional.of(proposal);
    }

    private static Optional<ICompletionProposal> createEmptyProposal() {
        final EmptyCompletionProposal proposal = new EmptyCompletionProposal(
                "Missing resource file cannot be auto-created",
                "Please check your file path if you want to use this Quick Fix."
                        + "<br>Only valid and legal path to the file with explicit robot resource extension "
                        + "inside existing open project in this workspace can be used."
                        + "<br>All missing parent directories inside project will be auto-generated.");
        return Optional.of(proposal);
    }
}
