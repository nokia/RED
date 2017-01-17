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
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposalPredicate;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposalPredicates;
import org.robotframework.ide.eclipse.main.plugin.assist.RedWithNameProposals;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposalAdapter.DocumentationModification;

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
            final int cellLength, final String prefix, final boolean atTheEndOfLine) throws BadLocationException {

        final String lineContent = DocumentUtilities.lineContentBeforeCurrentPosition(document, offset);
        final int line = DocumentUtilities.getLine(document, offset);

        final AssistProposalPredicate<String> wordsPredicate = createPredicate(lineContent, line);
        final List<? extends AssistProposal> wordsProposals = new RedWithNameProposals(wordsPredicate)
                .getWithNameProposals(prefix);

        final List<ICompletionProposal> proposals = newArrayList();
        for (final AssistProposal proposal : wordsProposals) {
            final String additional = getAdditionalContent(atTheEndOfLine, proposal);

            final DocumentationModification modification = new DocumentationModification(additional,
                    new Position(offset - prefix.length(), cellLength),
                    new Position(offset - prefix.length() + proposal.getContent().length()
                            + assist.getSeparatorToFollow().length(), 5));

            proposals.add(new RedCompletionProposalAdapter(proposal, modification));
        }
        return proposals;
    }

    private String getAdditionalContent(final boolean atTheEndOfLine, final AssistProposal proposal) {
            return assist.getSeparatorToFollow() + "alias";
    }


    private AssistProposalPredicate<String> createPredicate(final String lineContentTillOfsset, final int line) {
        final int separators = DocumentUtilities.getNumberOfCellSeparators(lineContentTillOfsset, assist.isTsvFile());
        return AssistProposalPredicates.withNamePredicate(separators);
    }
}
