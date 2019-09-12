/*
 * Copyright 2019 Nokia Solutions and Networks
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
import org.robotframework.ide.eclipse.main.plugin.assist.LibraryAliasReservedWordProposals;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposalAdapter.DocumentModification;

import com.google.common.collect.Streams;

public class CodeReservedWordsInSettingsAssistProcessor extends RedContentAssistProcessor {

    public CodeReservedWordsInSettingsAssistProcessor(final SuiteSourceAssistantContext assist) {
        super(assist);
    }

    @Override
    public String getProposalsTitle() {
        return "Reserved words";
    }

    @Override
    public List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.SETTINGS_SECTION);
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
        final boolean isInLastCell = atTheEndOfLine ? true
                : DocumentUtilities.isInLastCellOfLine(document, offset, assist.isTsvFile());

        final List<? extends AssistProposal> libraryAliasProposals = createLibraryAliasProposals(userContent, lineModel,
                cellIndex);
        final List<? extends AssistProposal> disableSettingProposals = createDisableSettingProposals(userContent,
                lineModel, cellIndex);

        final List<ICompletionProposal> proposals = newArrayList();
        Streams.concat(libraryAliasProposals.stream(), disableSettingProposals.stream()).forEach(proposal -> {
            final String contentSuffix = getContentSuffix(proposal, isInLastCell);

            final int startOffset = offset - userContent.length();
            final Position toReplace = new Position(startOffset, cellLength);
            final Position toSelect = getSelection(proposal, startOffset, isInLastCell);

            final DocumentModification modification = new DocumentModification(contentSuffix, toReplace, toSelect);

            proposals.add(new RedCompletionProposalAdapter(assist, proposal, modification));
        });
        return proposals;
    }

    private List<? extends AssistProposal> createLibraryAliasProposals(final String userContent,
            final RobotLine lineModel, final int cellIndex) {
        final Optional<RobotToken> firstTokenInLine = lineModel.tokensStream().findFirst();
        final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                .libraryAliasReservedWordPredicate(cellIndex, firstTokenInLine);
        return new LibraryAliasReservedWordProposals(predicate).getReservedWordProposals(userContent);
    }

    private List<? extends AssistProposal> createDisableSettingProposals(final String userContent,
            final RobotLine lineModel, final int cellIndex) {
        final Optional<RobotToken> firstTokenInLine = lineModel.tokensStream().findFirst();
        final AssistProposalPredicate<String> predicate = AssistProposalPredicates
                .disableSettingInSettingsReservedWordPredicate(cellIndex, firstTokenInLine);
        return new DisableSettingReservedWordProposals(predicate).getReservedWordProposals(userContent);
    }

    private String getContentSuffix(final AssistProposal proposal, final boolean isInLastCell) {
        if (isInLastCell && LibraryAliasReservedWordProposals.WITH_NAME.equals(proposal.getLabel())) {
            return assist.getSeparatorToFollow() + String.join(assist.getSeparatorToFollow(), proposal.getArguments());
        } else {
            return "";
        }
    }

    private Position getSelection(final AssistProposal proposal, final int startOffset, final boolean isInLastCell) {
        if (isInLastCell && LibraryAliasReservedWordProposals.WITH_NAME.equals(proposal.getLabel())) {
            return new Position(startOffset + proposal.getContent().length() + assist.getSeparatorToFollow().length(),
                    proposal.getArguments().get(0).length());
        } else {
            return new Position(startOffset + proposal.getContent().length(), 0);
        }
    }
}
