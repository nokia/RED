/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;

/**
 * @author Michal Anglart
 */
public class CombinedAssistProcessor extends RedContentAssistProcessor {

    private final List<IRedContentAssistProcessor> processors;

    private final String proposalsTitle;

    public CombinedAssistProcessor(final IRedContentAssistProcessor... assistProcessors) {
        this("Smart", assistProcessors);
    }

    public CombinedAssistProcessor(final String proposalsTitle, final IRedContentAssistProcessor... assistProcessors) {
        super(null);
        this.proposalsTitle = proposalsTitle;
        this.processors = newArrayList(assistProcessors);
    }

    @Override
    public String getProposalsTitle() {
        return proposalsTitle;
    }

    @Override
    public List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.KEYWORDS_SECTION,
                SuiteSourcePartitionScanner.TEST_CASES_SECTION, SuiteSourcePartitionScanner.TASKS_SECTION,
                SuiteSourcePartitionScanner.SETTINGS_SECTION, SuiteSourcePartitionScanner.VARIABLES_SECTION);
    }

    @Override
    public List<ICompletionProposal> computeProposals(final ITextViewer viewer, final int offset) {
        final List<ICompletionProposal> proposals = newArrayList();
        boolean proposalsFound = false;

        for (final IContentAssistProcessor processor : processors) {
            final ICompletionProposal[] newProposals = processor.computeCompletionProposals(viewer, offset);
            if (newProposals != null) {
                proposalsFound = true;
                proposals.addAll(Arrays.asList(newProposals));
            }
        }
        return !proposalsFound && proposals.isEmpty() ? null : proposals;
    }

    @Override
    protected boolean shouldShowProposals(final IDocument document, final int offset, final String lineContent)
            throws BadLocationException {
        return false;
    }

    @Override
    protected List<? extends ICompletionProposal> computeProposals(final IDocument document, final int offset,
            final int cellLength, final String userContent, final boolean atTheEndOfLine) throws BadLocationException {
        return null;
    }
}
