/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
public abstract class RedContentAssistProcessor extends DefaultContentAssistProcessor {

    protected final SuiteSourceAssistantContext assist;

    protected ITextViewer viewer;

    public RedContentAssistProcessor(final SuiteSourceAssistantContext assist) {
        this.assist = assist;
    }

    protected abstract String getProposalsTitle();

    protected abstract List<String> getApplicableContentTypes();

    @Override
    public final ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int offset) {
        final List<? extends ICompletionProposal> proposals = computeProposals(viewer, offset);
        return proposals == null ? null : proposals.toArray(new ICompletionProposal[0]);
    }

    protected List<? extends ICompletionProposal> computeProposals(final ITextViewer viewer, final int offset) {
        this.viewer = viewer;
        final IDocument document = viewer.getDocument();
        try {
            final String lineContent = DocumentUtilities.lineContentBeforeCurrentPosition(document, offset);
            if (shouldShowProposals(document, offset, lineContent)) {
                final boolean isTsv = assist.isTsvFile();
                final Optional<IRegion> cellRegion = DocumentUtilities.findLiveCellRegion(document, isTsv, offset);
                final String prefix = DocumentUtilities.getPrefix(document, cellRegion, offset);
                final int contentLength = cellRegion.isPresent() ? cellRegion.get().getLength() : 0;

                return computeProposals(document, offset, contentLength, prefix, isAtTheEndOfLine(document, offset));
            }
        } catch (final BadLocationException e) {
            // we'll return nothing then
        }
        return null;
    }

    private boolean isAtTheEndOfLine(final IDocument document, final int offset) throws BadLocationException {
        // at the end of line when only whitespaces follow current position at this line
        final IRegion lineRegion = document.getLineInformationOfOffset(offset);
        return document.get(offset, lineRegion.getOffset() + lineRegion.getLength() - offset).trim().isEmpty();
    }

    protected abstract boolean shouldShowProposals(final IDocument document, final int offset, final String lineContent)
            throws BadLocationException;

    protected abstract List<? extends ICompletionProposal> computeProposals(IDocument document, final int offset,
            final int cellLength, final String prefix, boolean isAtTheEndOfLine) throws BadLocationException;

    protected final boolean isInApplicableContentType(final IDocument document, final int offset)
            throws BadLocationException {
        return getApplicableContentTypes().contains(getVirtualContentType(document, offset));
    }

    protected final String getVirtualContentType(final IDocument document, final int offset)
            throws BadLocationException {
        final String contentType = document.getContentType(offset);
        if (contentType != IDocument.DEFAULT_CONTENT_TYPE) {
            return contentType;
        } else if (offset > 0 && offset == document.getLength()) {
            return document.getContentType(offset - 1);
        }
        return contentType;
    }
}
