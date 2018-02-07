/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposalPredicate;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposalPredicates;
import org.robotframework.ide.eclipse.main.plugin.assist.RedWithNameProposals;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposalAdapter.DocumentModification;

public class WithNameAssistProcessor extends RedContentAssistProcessor {

    public WithNameAssistProcessor(final SuiteSourceAssistantContext assist) {
        super(assist);
    }

    @Override
    protected String getProposalsTitle() {
        return "With name";
    }

    @Override
    protected List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.SETTINGS_SECTION);
    }

    @Override
    protected boolean shouldShowProposals(final IDocument document, final int offset, final String lineContent)
            throws BadLocationException {
        return isInApplicableContentType(document, offset) && lineContent.toLowerCase().startsWith("library")
                && DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile()) > 1;
    }

    @Override
    protected List<? extends ICompletionProposal> computeProposals(final IDocument document, final int offset,
            final int cellLength, final String userContent, final boolean atTheEndOfLine) throws BadLocationException {

        final String lineContent = DocumentUtilities.lineContentBeforeCurrentPosition(document, offset);
        final boolean isInLastCell = atTheEndOfLine ? true
                : DocumentUtilities.isInLastCellOfLine(document, offset, assist.isTsvFile());
        final String separator = assist.getSeparatorToFollow();

        final AssistProposalPredicate<String> wordsPredicate = createPredicate(lineContent);
        final List<? extends AssistProposal> wordsProposals = new RedWithNameProposals(wordsPredicate)
                .getWithNameProposals(userContent);

        final List<ICompletionProposal> proposals = newArrayList();
        for (final AssistProposal proposal : wordsProposals) {
            final List<String> args = isInLastCell ? proposal.getArguments() : new ArrayList<>();
            final String contentSuffix = args.isEmpty() ? "" : separator + String.join(separator, args);

            final Position toReplace = new Position(offset - userContent.length(), cellLength);
            final Position toSelect = contentSuffix.equals("")
                    ? new Position(offset - userContent.length() + proposal.getContent().length(), 0)
                    : new Position(offset - userContent.length() + proposal.getContent().length() + separator.length(),
                            proposal.getArguments().get(0).length());

            final DocumentModification modification = new DocumentModification(contentSuffix, toReplace, toSelect);

            proposals.add(new RedCompletionProposalAdapter(proposal, modification));
        }
        return proposals;
    }

    private AssistProposalPredicate<String> createPredicate(final String lineContentTillOffset) {
        final int separators = DocumentUtilities.getNumberOfCellSeparators(lineContentTillOffset, assist.isTsvFile());
        return AssistProposalPredicates.withNamePredicate(separators);
    }
}
