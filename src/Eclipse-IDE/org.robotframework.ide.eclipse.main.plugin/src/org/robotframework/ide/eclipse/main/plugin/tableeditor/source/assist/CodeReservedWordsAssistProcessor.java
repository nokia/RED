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
import org.robotframework.ide.eclipse.main.plugin.assist.DisableSettingReservedWordProposals;
import org.robotframework.ide.eclipse.main.plugin.assist.ForLoopReservedWordsProposals;
import org.robotframework.ide.eclipse.main.plugin.assist.GherkinReservedWordProposals;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposalAdapter.DocumentModification;

import com.google.common.collect.Streams;

/**
 * @author Michal Anglart
 */
public class CodeReservedWordsAssistProcessor extends RedContentAssistProcessor {

    public CodeReservedWordsAssistProcessor(final AssistantContext assist) {
        super(assist);
    }

    @Override
    public String getProposalsTitle() {
        return "Reserved words";
    }

    @Override
    public List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.TEST_CASES_SECTION, SuiteSourcePartitionScanner.TASKS_SECTION,
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

        final int line = DocumentUtilities.getLine(document, offset);
        final RobotLine lineModel = assist.getModel().getLinkedElement().getFileContent().get(line);
        final String lineContent = DocumentUtilities.lineContentBeforeCurrentPosition(document, offset);
        final int cellIndex = DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile());

        final List<? extends AssistProposal> loopsProposals = createForLoopsProposals(userContent, lineModel,
                cellIndex);
        final List<? extends AssistProposal> gherkinProposals = createGherkinProposals(userContent, cellIndex);
        final List<? extends AssistProposal> disableSettingProposals = createDisableSettingProposals(userContent,
                lineModel, cellIndex);

        final List<ICompletionProposal> proposals = newArrayList();
        Streams.concat(loopsProposals.stream(), gherkinProposals.stream(), disableSettingProposals.stream())
                .forEach(proposal -> {
                    final String contentSuffix = getContentSuffix(proposal, atTheEndOfLine);

                    final DocumentModification modification = new DocumentModification(contentSuffix,
                            new Position(offset - userContent.length(), cellLength));

                    proposals.add(new RedCompletionProposalAdapter(assist, proposal, modification));
                });
        return proposals;
    }

    private List<? extends AssistProposal> createForLoopsProposals(final String userContent, final RobotLine lineModel,
            final int cellIndex) {
        final Optional<RobotToken> firstTokenInLine = lineModel.tokensStream().findFirst();
        final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                .forLoopReservedWordsPredicate(cellIndex, firstTokenInLine);
        return new ForLoopReservedWordsProposals(predicate).getReservedWordProposals(userContent);
    }

    private List<? extends AssistProposal> createGherkinProposals(final String userContent, final int cellIndex) {
        final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                .gherkinReservedWordsPredicate(cellIndex);
        return new GherkinReservedWordProposals(predicate).getReservedWordProposals(userContent);
    }

    private List<? extends AssistProposal> createDisableSettingProposals(final String userContent,
            final RobotLine lineModel, final int cellIndex) {
        final Optional<RobotToken> firstTokenInLine = lineModel.tokensStream().findFirst();
        final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                .disableSettingReservedWordPredicate(cellIndex, firstTokenInLine);
        return new DisableSettingReservedWordProposals(predicate).getReservedWordProposals(userContent);
    }

    private String getContentSuffix(final AssistProposal proposal, final boolean atTheEndOfLine) {
        if (!atTheEndOfLine) {
            return "";
        } else if (GherkinReservedWordProposals.GHERKIN_ELEMENTS.contains(proposal.getLabel())) {
            return " ";
        } else if (DisableSettingReservedWordProposals.NONE.equals(proposal.getLabel())) {
            return "";
        } else {
            return assist.getSeparatorToFollow();
        }
    }
}
