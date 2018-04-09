/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Optional;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposalPredicate;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposalPredicates;
import org.robotframework.ide.eclipse.main.plugin.assist.RedCodeReservedWordProposals;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposalAdapter.DocumentModification;

/**
 * @author Michal Anglart
 */
public class CodeReservedWordsAssistProcessor extends RedContentAssistProcessor {

    public CodeReservedWordsAssistProcessor(final SuiteSourceAssistantContext assist) {
        super(assist);
    }

    @Override
    protected String getProposalsTitle() {
        return "Reserved words";
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
            final int cellLength, final String userContent, final boolean atTheEndOfLine) throws BadLocationException {

        final String lineContent = DocumentUtilities.lineContentBeforeCurrentPosition(document, offset);
        final int line = DocumentUtilities.getLine(document, offset);

        final AssistProposalPredicate<String> wordsPredicate = createPredicate(lineContent, line);
        final List<? extends AssistProposal> wordsProposals = new RedCodeReservedWordProposals(wordsPredicate)
                .getReservedWordProposals(userContent);

        final List<ICompletionProposal> proposals = newArrayList();
        for (final AssistProposal proposal : wordsProposals) {
            final String contentSuffix = getContentSuffix(atTheEndOfLine, proposal);

            final DocumentModification modification = new DocumentModification(contentSuffix,
                    new Position(offset - userContent.length(), cellLength));

            proposals.add(new RedCompletionProposalAdapter(assist, proposal, modification));
        }
        return proposals;
    }

    private String getContentSuffix(final boolean atTheEndOfLine, final AssistProposal proposal) {
        if (!atTheEndOfLine) {
            return "";
        } else if (RedCodeReservedWordProposals.GHERKIN_ELEMENTS.contains(proposal.getLabel())) {
            return " ";
        } else {
            return assist.getSeparatorToFollow();
        }
    }

    private AssistProposalPredicate<String> createPredicate(final String lineContentTillOffset, final int line) {
        final int separators = DocumentUtilities.getNumberOfCellSeparators(lineContentTillOffset, assist.isTsvFile());
        final RobotLine lineModel = assist.getModel().getLinkedElement().getFileContent().get(line);
        final List<RobotToken> lineTokens = lineModel.getLineTokens();
        final Optional<RobotToken> firstToken = lineTokens.isEmpty() ? Optional.empty()
                : Optional.of(lineTokens.get(0));

        return AssistProposalPredicates.codeReservedWordsPredicate(separators, firstToken);
    }
}
