/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Michal Anglart
 *
 */
public abstract class RedContentAssistProcessor extends DefaultContentAssistProcessor {

    protected abstract String getProposalsTitle();

    protected abstract List<String> getApplicableContentTypes();

    @Override
    public final ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int offset) {
        final List<? extends ICompletionProposal> proposals = computeProposals(viewer, offset);
        return proposals == null ? null : proposals.toArray(new ICompletionProposal[0]);
    }

    protected abstract List<? extends ICompletionProposal> computeProposals(ITextViewer viewer, int offset);

    protected final boolean isInApplicableContentType(final IDocument document, final int offset)
            throws BadLocationException {
        // it is valid to show those proposals when we are in variables content type or in default
        // section at the end of document when previous content type is a variable table
        final String contentType = document.getContentType(offset);
        return getApplicableContentTypes().contains(contentType)
                || (contentType == IDocument.DEFAULT_CONTENT_TYPE && offset > 0 && offset == document.getLength()
                        && getApplicableContentTypes().contains(document.getContentType(offset - 1)));
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
