/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedTemplateArgumentsProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedTemplateArgumentsProposals;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposalAdapter.DocumentModification;

import com.google.common.annotations.VisibleForTesting;

public class TemplateArgumentsAssistProcessor extends KeywordCallsAssistProcessor {

    public TemplateArgumentsAssistProcessor(final AssistantContext assist) {
        super(assist);
    }

    @Override
    public String getProposalsTitle() {
        return "Template arguments";
    }

    @Override
    public List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.TEST_CASES_SECTION, SuiteSourcePartitionScanner.TASKS_SECTION);
    }

    @Override
    protected boolean shouldShowProposals(final IDocument document, final int offset, final String lineContent)
            throws BadLocationException {
        return isInApplicableContentType(document, offset)
                && DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile()) == 1
                && ModelUtilities.isEmptyLine(assist.getModel(), offset)
                && ModelUtilities.getTemplateInUse(assist.getModel(), offset).isPresent();
    }

    @Override
    protected List<? extends ICompletionProposal> computeProposals(final IDocument document, final int offset,
            final int cellLength, final String userContent, final boolean atTheEndOfLine) throws BadLocationException {

        final String templateKeyword = ModelUtilities.getTemplateInUse(assist.getModel(), offset).get();
        final List<RedTemplateArgumentsProposal> argProposals = new RedTemplateArgumentsProposals(assist.getModel())
                .getRedTemplateArgumentsProposal(templateKeyword);

        final List<ICompletionProposal> proposals = new ArrayList<>();

        final String separator = assist.getSeparatorToFollow();
        for (final RedTemplateArgumentsProposal proposal : argProposals) {
            final String contentSuffix = proposal.getContent().isEmpty() || proposal.getArguments().isEmpty() ? ""
                    : (separator + String.join(separator, proposal.getArguments()));

            final Position toReplace = new Position(offset, cellLength);

            final DocumentModification modification = new DocumentModification(contentSuffix, toReplace, () -> {
                final Collection<IRegion> regionsToLinkedEdit = calculateRegionsForLinkedMode(proposal,
                        toReplace.getOffset(), separator.length());
                return createOperationsToPerformAfterAccepting(regionsToLinkedEdit, proposal);
            });
            proposals.add(new RedCompletionProposalAdapter(assist, proposal, modification, null));
        }

        return proposals;
    }

    @VisibleForTesting
    static Collection<IRegion> calculateRegionsForLinkedMode(final RedTemplateArgumentsProposal proposal,
            final int startOffset, final int separatorLength) {
        final Collection<IRegion> regions = new ArrayList<>();
        int offset = startOffset;
        if (!proposal.getContent().isEmpty()) {
            regions.add(new Region(offset, proposal.getContent().length()));
            offset += proposal.getContent().length() + separatorLength;
        }
        for (final String requiredArg : proposal.getArguments()) {
            regions.add(new Region(offset, requiredArg.length()));
            offset += requiredArg.length() + separatorLength;
        }
        return regions;
    }
}
