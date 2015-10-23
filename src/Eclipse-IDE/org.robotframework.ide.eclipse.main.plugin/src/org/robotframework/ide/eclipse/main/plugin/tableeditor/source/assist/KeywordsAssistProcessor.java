/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;


/**
 * @author Michal Anglart
 *
 */
public class KeywordsAssistProcessor extends RedContentAssistProcessor {

    @Override
    protected String getProposalsTitle() {
        return "Keywords";
    }

    @Override
    protected List<ICompletionProposal> computeProposals(final ITextViewer viewer, final int offset) {
        final IDocument document = viewer.getDocument();
        try {
            DocumentUtilities.findVariable(document, offset);
            return newArrayList();
        } catch (final BadLocationException e) {
            return newArrayList();
        }
    }
}
