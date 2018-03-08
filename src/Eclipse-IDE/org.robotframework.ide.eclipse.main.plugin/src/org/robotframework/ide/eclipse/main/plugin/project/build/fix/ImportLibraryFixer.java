/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import java.util.Optional;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.red.graphics.ImagesManager;

/**
 * @author Michal Anglart
 */
public class ImportLibraryFixer extends RedSuiteMarkerResolution {

    private final String libName;

    public ImportLibraryFixer(final String libName) {
        this.libName = libName;
    }

    @Override
    public String getLabel() {
        return "Import '" + libName + "' library";
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        final String lineDelimiter = DocumentUtilities.getDelimiter(document);
        final String cellSeparator = getSeparator(suiteModel);

        final String lineToInsert = lineDelimiter + "Library" + cellSeparator + libName;

        final Optional<RobotSettingsSection> section = suiteModel.findSection(RobotSettingsSection.class);
        if (section.isPresent()) {
            final int line = section.get().getHeaderLine();
            try {
                final IRegion lineInformation = document.getLineInformation(line - 1);
                final int offset = lineInformation.getOffset() + lineInformation.getLength();
                return Optional.of(new CompletionProposal(lineToInsert, offset, 0, lineToInsert.length(),
                        ImagesManager.getImage(RedImages.getBookImage()), getLabel(), null, null));
            } catch (final BadLocationException e) {
                return Optional.empty();
            }

        } else {
            final String toInsert = "*** Settings ***" + lineToInsert + lineDelimiter + lineDelimiter;
            return Optional.of(new CompletionProposal(toInsert, 0, 0, toInsert.length(),
                    ImagesManager.getImage(RedImages.getBookImage()), getLabel(), null, null));
        }
    }
}
