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
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposalPredicate;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposalPredicates;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposalAdapter.DocumentModification;

/**
 * @author Michal Anglart
 */
public class VariablesAssistProcessor extends RedContentAssistProcessor {

    public VariablesAssistProcessor(final SuiteSourceAssistantContext assist) {
        super(assist);
    }

    @Override
    protected String getProposalsTitle() {
        return "Variables";
    }

    @Override
    protected List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.KEYWORDS_SECTION,
                SuiteSourcePartitionScanner.TEST_CASES_SECTION, SuiteSourcePartitionScanner.SETTINGS_SECTION,
                SuiteSourcePartitionScanner.VARIABLES_SECTION);
    }

    @Override
    protected boolean shouldShowProposals(final IDocument document, final int offset, final String lineContent)
            throws BadLocationException {
        return isInApplicableContentType(document, offset)
                && DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile()) >= 1;
    }

    @Override
    protected List<? extends ICompletionProposal> computeProposals(final IDocument document, final int offset,
            final int cellLength, final String userContent, final boolean atTheEndOfLine) throws BadLocationException {

        final Optional<IRegion> liveVarRegion = DocumentUtilities.findLiveVariable(document, assist.isTsvFile(),
                offset);
        final String liveVarPrefix = DocumentUtilities.getPrefix(document, liveVarRegion, offset);

        final String userContentToReplace;
        final int lengthToReplace;
        if (liveVarPrefix.isEmpty()) {
            userContentToReplace = userContent;
            lengthToReplace = cellLength;
        } else {
            userContentToReplace = liveVarPrefix;
            final Optional<IRegion> varRegion = DocumentUtilities.findVariable(document, assist.isTsvFile(), offset);
            if (varRegion.isPresent()) {
                lengthToReplace = varRegion.get().getLength();
            } else {
                lengthToReplace = liveVarRegion.map(region -> offset - region.getOffset()).orElse(0);
            }
        }

        final int line = DocumentUtilities.getLine(document, offset);
        final int lastTokenOffset = lastTokenOffset(offset, line, assist.getModel());

        final AssistProposalPredicate<String> globalVarPredicate = createGlobalVarPredicate(lastTokenOffset,
                assist.getModel());

        final List<? extends AssistProposal> variableProposals = new RedVariableProposals(assist.getModel(),
                globalVarPredicate).getVariableProposals(userContentToReplace, lastTokenOffset);

        final List<ICompletionProposal> proposals = newArrayList();
        for (final AssistProposal varProposal : variableProposals) {
            final DocumentModification modification = new DocumentModification("",
                    new Position(offset - userContentToReplace.length(), lengthToReplace));

            proposals.add(new RedCompletionProposalAdapter(assist, varProposal, modification));
        }
        return proposals;
    }

    private AssistProposalPredicate<String> createGlobalVarPredicate(final int lastTokenOffset,
            final RobotSuiteFile model) {
        final Optional<? extends RobotElement> element = model.findElement(lastTokenOffset);
        return element.isPresent() ? AssistProposalPredicates.globalVariablePredicate(element.get())
                : AssistProposalPredicates.alwaysTrue();
    }

    private int lastTokenOffset(final int offset, final int line, final RobotSuiteFile model) {
        final List<RobotLine> fileContent = model.getLinkedElement().getFileContent();
        for (int i = line; i >= 0; i--) {
            final List<RobotToken> lineTokens = fileContent.get(i).getLineTokens();
            if (!lineTokens.isEmpty()) {
                return lineTokens.get(lineTokens.size() - 1).getStartOffset();
            }
        }
        return offset;
    }
}
