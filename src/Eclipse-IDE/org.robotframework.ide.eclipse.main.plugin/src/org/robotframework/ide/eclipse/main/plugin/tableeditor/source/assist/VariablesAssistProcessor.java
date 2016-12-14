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
import org.eclipse.jface.text.IRegion;
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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionProposalAdapter.DocumentationModification;

import com.google.common.base.Optional;


/**
 * @author Michal Anglart
 *
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
                SuiteSourcePartitionScanner.TEST_CASES_SECTION,
                SuiteSourcePartitionScanner.SETTINGS_SECTION,
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
            final int cellLength, final String prefix) throws BadLocationException {

        final Optional<IRegion> liveVarRegion = DocumentUtilities.findLiveVariable(document, assist.isTsvFile(), offset);
        final String actualPrefix = DocumentUtilities.getPrefix(document, liveVarRegion, offset);
        final int wholeLength = liveVarRegion.isPresent() ? liveVarRegion.get().getLength() + 1 : 0;

        final int line = DocumentUtilities.getLine(document, offset);
        final AssistProposalPredicate<String> globalVarPredicate = createGlobalVarPredicate(offset, line,
                assist.getModel());

        final List<? extends AssistProposal> variableProposals = new RedVariableProposals(assist.getModel(),
                globalVarPredicate).getVariableProposals(actualPrefix, offset);

        final List<ICompletionProposal> proposals = newArrayList();
        for (final AssistProposal varProposal : variableProposals) {
            final DocumentationModification modification = new DocumentationModification("",
                    assist.getAcceptanceMode().positionToReplace(offset, actualPrefix.length(), wholeLength));

            proposals.add(new RedCompletionProposalAdapter(varProposal, modification));
        }
        return proposals;
    }

    private AssistProposalPredicate<String> createGlobalVarPredicate(final int offset, final int line,
            final RobotSuiteFile model) {
        final List<RobotLine> fileContent = model.getLinkedElement().getFileContent();
        final List<RobotToken> lineTokens = fileContent.get(line).getLineTokens();
        final int lastTokenOffset = lineTokens.isEmpty() ? offset
                : lineTokens.get(lineTokens.size() - 1).getStartOffset();

        final Optional<? extends RobotElement> element = model.findElement(lastTokenOffset);
        return element.isPresent() ? AssistProposalPredicates.globalVariablePredicate(element.get())
                : AssistProposalPredicates.<String> alwaysTrue();
    }
}
