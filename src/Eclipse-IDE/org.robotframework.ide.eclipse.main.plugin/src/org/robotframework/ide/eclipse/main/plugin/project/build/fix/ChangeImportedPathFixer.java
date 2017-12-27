/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.graphics.ImagesManager;

/**
 * @author Michal Anglart
 */
public class ChangeImportedPathFixer extends RedSuiteMarkerResolution {

    public static List<ChangeImportedPathFixer> createFixersForSameFile(final IFile problematicFile,
            final IPath invalidPath) {
        final List<IPath> filePaths = findFilePathsWithSameLastSegment(invalidPath.lastSegment());
        return filePaths.stream()
                .sorted(createPathsComparator(problematicFile.getProject()))
                .map(path -> path.makeRelativeTo(problematicFile.getFullPath()).removeFirstSegments(1))
                .filter(path -> !invalidPath.equals(path))
                .map(ChangeImportedPathFixer::new)
                .collect(Collectors.toList());
    }

    private static List<IPath> findFilePathsWithSameLastSegment(final String lastSegment) {
        final List<IPath> paths = new ArrayList<>();
        try {
            ResourcesPlugin.getWorkspace().getRoot().accept(new IResourceVisitor() {

                @Override
                public boolean visit(final IResource resource) throws CoreException {
                    if (resource.getType() == IResource.FILE
                            && resource.getFullPath().lastSegment().equals(lastSegment)) {
                        paths.add(resource.getFullPath());
                    }
                    return true;
                }
            });
        } catch (final CoreException e) {
            // ok, we'll return what we've gathered so far
        }
        return paths;
    }

    private static Comparator<IPath> createPathsComparator(final IProject project) {
        return new Comparator<IPath>() {

            @Override
            public int compare(final IPath path1, final IPath path2) {
                final String projectFolderName = project.getFullPath().segment(0);
                final boolean isFromProject1 = path1.segment(0).equals(projectFolderName);
                final boolean isFromProject2 = path2.segment(0).equals(projectFolderName);
                return Boolean.compare(isFromProject2, isFromProject1);
            }
        };
    }

    private final IPath validFileRelativePath;

    public ChangeImportedPathFixer(final IPath validFileRelativePath) {
        this.validFileRelativePath = validFileRelativePath;
    }

    @Override
    public String getLabel() {
        return "Change to " + validFileRelativePath.toString();
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        final String toInsert = validFileRelativePath.toString();

        final Image image = ImagesManager
                .getImage(RedImages.getImageForFileWithExtension(validFileRelativePath.getFileExtension()));
        try {
            final int charStart = (int) marker.getAttribute(IMarker.CHAR_START);
            final int charEnd = (int) marker.getAttribute(IMarker.CHAR_END);
            final IRegion regionToChange = new Region(charStart, charEnd - charStart);
            if (toInsert.equals(document.get(charStart, regionToChange.getLength()))) {
                return Optional.empty();
            }
            return Optional.of(new CompletionProposal(toInsert, charStart, charEnd - charStart, toInsert.length(),
                    image, getLabel(), null, Snippets.createSnippetInfo(document, regionToChange, toInsert)));
        } catch (final CoreException | BadLocationException e) {
            return Optional.empty();
        }
    }

}
