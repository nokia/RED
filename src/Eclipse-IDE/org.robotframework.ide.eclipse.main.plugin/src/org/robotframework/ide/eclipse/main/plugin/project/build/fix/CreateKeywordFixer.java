/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.RedMarkerResolution;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
public class CreateKeywordFixer extends RedMarkerResolution {

    private final String keywordName;

    public CreateKeywordFixer(final String keywordName) {
        this.keywordName = keywordName;
    }

    @Override
    public String getLabel() {
        return "Create '" + keywordName + "' keyword";
    }

    @Override
    public ICompletionProposal asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        if (keywordName != null) {
            final Optional<RobotKeywordsSection> section = suiteModel.findSection(RobotKeywordsSection.class);
            final String lineDelimiter = getLineDelimiter(document);
            if (section.isPresent()) {
                final String toInsert = lineDelimiter + keywordName + lineDelimiter + "    " + lineDelimiter;
                final int line = section.get().getHeaderLine();
                try {
                    final IRegion lineInformation = document.getLineInformation(line - 1);
                    final int offset = lineInformation.getOffset() + lineInformation.getLength();
                    return new CompletionProposal(toInsert, offset, 0, toInsert.length() - 1,
                            ImagesManager.getImage(RedImages.getUserKeywordImage()), getLabel(), null, null);
                } catch (final BadLocationException e) {
                    throw new IllegalStateException("Unable to determine position for new keyword", e);
                }

            } else {
                final String toInsert = lineDelimiter + lineDelimiter + "*** Keywords ***" + lineDelimiter + keywordName
                        + lineDelimiter + "    ";
                final int offset = document.getLength();
                return new CompletionProposal(toInsert, offset, 0, toInsert.length(),
                        ImagesManager.getImage(RedImages.getUserKeywordImage()), getLabel(), null, null);
            }
        }
        return null;
    }

    private static String getLineDelimiter(final IDocument document) {
        try {
            final String delimiter = document.getLineDelimiter(0);
            return delimiter == null ? "\n" : delimiter;
        } catch (final BadLocationException e) {
            return "\n";
        }
    }
}
