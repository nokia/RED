/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.fix;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
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
 * @author mmarzec
 */
public class ChangeKeywordNameFixer extends RedSuiteMarkerResolution {

    private final String keywordOccurrence;

    private final String keywordDefinition;

    public ChangeKeywordNameFixer(final String keywordOccurrence, final String keywordDefinition,
            final String keywordSource) {
        this.keywordOccurrence = keywordOccurrence;
        if (keywordOccurrence != null && keywordDefinition != null && !keywordSource.isEmpty()
                && keywordOccurrence.startsWith(keywordSource) && !keywordOccurrence.equals(keywordSource)) {
            this.keywordDefinition = keywordSource + "." + keywordDefinition;
        } else {
            this.keywordDefinition = keywordDefinition;
        }
    }

    @Override
    public String getLabel() {
        return "Change '" + keywordOccurrence + "' to '" + keywordDefinition + "'";
    }

    @Override
    public Optional<ICompletionProposal> asContentProposal(final IMarker marker, final IDocument document,
            final RobotSuiteFile suiteModel) {
        if (keywordOccurrence == null || keywordDefinition == null) {
            return Optional.absent();
        }

        final String toInsert = keywordDefinition;

        final Image image = ImagesManager.getImage(RedImages.getChangeImage());
        try {
            final int charStart = (int) marker.getAttribute(IMarker.CHAR_START);
            final int charEnd = (int) marker.getAttribute(IMarker.CHAR_END);
            final IRegion regionToChange = new Region(charStart, charEnd - charStart);
            return Optional.<ICompletionProposal> of(new CompletionProposal(toInsert, charStart, charEnd - charStart,
                    toInsert.length(), image, getLabel(), null, Snippets.createSnippetInfo(document, regionToChange,
                            toInsert)));
        } catch (final CoreException e) {
            return Optional.absent();
        }
    }
}
