/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
public class ChangeImportedPathFixer extends RedSuiteMarkerResolution {

    public static List<ChangeImportedPathFixer> createFixersForSameFile(final IFile problematicFile, final IPath invalidPath) {
        final List<ChangeImportedPathFixer> fixers = newArrayList();
        final String lastSegment = invalidPath.lastSegment();
        try {
            ResourcesPlugin.getWorkspace().getRoot().accept(new IResourceVisitor() {
                @Override
                public boolean visit(final IResource resource) throws CoreException {
                    if (resource.getType() == IResource.FILE && resource.getFullPath().lastSegment().equals(lastSegment)) {
                        final IPath resRelativePath = createCurrentFileRelativePath(problematicFile, (IFile) resource);

                        fixers.add(new ChangeImportedPathFixer(resRelativePath));
                    }
                    return true;
                }
            });
        } catch (final CoreException e) {
            return fixers;
        }
        return fixers;
    }

    private static IPath createCurrentFileRelativePath(final IFile from, final IFile to) {
        return to.getLocation().makeRelativeTo(from.getLocation()).removeFirstSegments(1);
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
            return Optional.<ICompletionProposal> of(
                    new CompletionProposal(toInsert, charStart, charEnd - charStart, toInsert.length(), image,
                            getLabel(), null, Snippets.createSnippetInfo(document, regionToChange, toInsert)));
        } catch (final CoreException e) {
            return Optional.absent();
        }
    }

}
