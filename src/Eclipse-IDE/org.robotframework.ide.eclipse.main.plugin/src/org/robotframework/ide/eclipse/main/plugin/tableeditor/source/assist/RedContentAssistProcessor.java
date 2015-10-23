/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Michal Anglart
 *
 */
public abstract class RedContentAssistProcessor extends DefaultContentAssistProcessor {

    protected abstract String getProposalsTitle();

    @Override
    public final ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int offset) {
        final List<? extends ICompletionProposal> proposals = computeProposals(viewer, offset);
        return proposals == null ? null : proposals.toArray(new ICompletionProposal[0]);
    }

    protected abstract List<? extends ICompletionProposal> computeProposals(ITextViewer viewer, int offset);

}
