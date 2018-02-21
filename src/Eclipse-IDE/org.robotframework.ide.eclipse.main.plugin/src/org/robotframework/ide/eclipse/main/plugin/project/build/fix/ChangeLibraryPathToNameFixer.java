/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.graphics.ImagesManager;

public class ChangeLibraryPathToNameFixer extends RedSuiteMarkerResolution {

    public static List<ChangeLibraryPathToNameFixer> createFixersForSameFile(final IFile problematicFile,
            final IPath invalidPath) {
        final RobotModel model = RedPlugin.getModelManager().getModel();
        final RobotProject robotProject = model.createRobotProject(problematicFile.getProject());

        final String lastSegmentWithoutExtension = invalidPath.removeFileExtension().lastSegment();

        return robotProject.getLibraryDescriptorsStream()
                .filter(LibraryDescriptor::isReferencedLibrary)
                .filter(desc -> desc.getName().equals(lastSegmentWithoutExtension))
                .map(LibraryDescriptor::getName)
                .map(ChangeLibraryPathToNameFixer::new)
                .collect(toList());
    }

    private final String libraryName;

    public ChangeLibraryPathToNameFixer(final String libraryName) {
        this.libraryName = libraryName;
    }

    @Override
    public String getLabel() {
        return "Change to " + libraryName;
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        final String toInsert = libraryName;

        final Image image = ImagesManager.getImage(RedImages.getChangeImage());
        try {
            final int charStart = (int) marker.getAttribute(IMarker.CHAR_START);
            final int charEnd = (int) marker.getAttribute(IMarker.CHAR_END);
            final IRegion regionToChange = new Region(charStart, charEnd - charStart);
            return Optional.of(new CompletionProposal(toInsert, charStart, charEnd - charStart, toInsert.length(),
                    image, getLabel(), null, Snippets.createSnippetInfo(document, regionToChange, toInsert)));
        } catch (final CoreException e) {
            return Optional.empty();
        }
    }

}
