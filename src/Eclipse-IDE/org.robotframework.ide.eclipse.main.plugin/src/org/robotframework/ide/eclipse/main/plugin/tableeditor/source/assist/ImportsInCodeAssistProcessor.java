/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedImportProposals;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposalAdapter.DocumentationModification;

public class ImportsInCodeAssistProcessor extends RedContentAssistProcessor {

    public ImportsInCodeAssistProcessor(final SuiteSourceAssistantContext assist) {
        super(assist);
    }

    @Override
    protected String getProposalsTitle() {
        return "Imports";
    }

    @Override
    protected List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.TEST_CASES_SECTION,
                SuiteSourcePartitionScanner.KEYWORDS_SECTION);
    }

    @Override
    protected boolean shouldShowProposals(final IDocument document, final int offset, final String lineContent)
            throws BadLocationException {
        return isInApplicableContentType(document, offset)
                && DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile()) > 0;
    }

    @Override
    protected List<? extends ICompletionProposal> computeProposals(final IDocument document, final int offset,
            final int cellLength, final String prefix) throws BadLocationException {

        final List<? extends AssistProposal> wordsProposals = new RedImportProposals(assist.getModel())
                .getImportsProposals(prefix);
        final List<ICompletionProposal> proposals = newArrayList();
        for (final AssistProposal proposal : wordsProposals) {
            final DocumentationModification modification = new DocumentationModification("",
                    assist.getAcceptanceMode().positionToReplace(offset, prefix.length(), cellLength), true);

            proposals.add(new RedCompletionProposalAdapter(proposal, modification));
        }
        return proposals;
    }

}
