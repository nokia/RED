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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;


/**
 * @author Michal Anglart
 *
 */
public class CombinedAssistProcessor extends RedContentAssistProcessor {

    private final List<RedContentAssistProcessor> processors;

    public CombinedAssistProcessor(final RedContentAssistProcessor... assistProcessors) {
        super(null);
        this.processors = newArrayList(assistProcessors);
    }

    @Override
    protected String getProposalsTitle() {
        return "Smart";
    }

    @Override
    protected List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.KEYWORDS_SECTION,
                SuiteSourcePartitionScanner.TEST_CASES_SECTION,
                SuiteSourcePartitionScanner.SETTINGS_SECTION,
                SuiteSourcePartitionScanner.VARIABLES_SECTION);
    }

    @Override
    protected List<ICompletionProposal> computeProposals(final ITextViewer viewer, final int offset) {
        final List<ICompletionProposal> proposals = newArrayList();
        boolean noProposalsFound = true;

        for (final RedContentAssistProcessor processor : processors) {
            final List<? extends ICompletionProposal> newProposals = processor.computeProposals(viewer, offset);
            if (newProposals != null) {
                noProposalsFound = false;
                proposals.addAll(newProposals);
            }
        }
        return noProposalsFound && proposals.isEmpty() ? null : proposals;
    }

    @Override
    protected boolean shouldShowProposals(final IDocument document, final int offset, final String lineContent)
            throws BadLocationException {
        return false;
    }

    @Override
    protected List<? extends ICompletionProposal> computeProposals(final IDocument document, final int offset,
            final int cellLength, final String prefix, final boolean atTheEndOfLine) throws BadLocationException {
        return null;
    }
}
