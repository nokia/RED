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
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposal;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;


/**
 * @author Michal Anglart
 *
 */
public class VariablesAssistProcessor extends RedContentAssistProcessor {

    private final SuiteSourceAssistantContext assist;

    public VariablesAssistProcessor(final SuiteSourceAssistantContext assist) {
        this.assist = assist;
    }

    @Override
    protected List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.KEYWORDS_SECTION,
                SuiteSourcePartitionScanner.TEST_CASES_SECTION, SuiteSourcePartitionScanner.SETTINGS_SECTION,
                SuiteSourcePartitionScanner.VARIABLES_SECTION);
    }

    @Override
    protected String getProposalsTitle() {
        return "Variables";
    }

    @Override
    protected List<ICompletionProposal> computeProposals(final ITextViewer viewer, final int offset) {
        final IDocument document = viewer.getDocument();
        try {
            final String lineContent = DocumentUtilities.lineContentBeforeCurrentPosition(document, offset);

            if (!shouldShowProposals(lineContent, document, offset)) {
                return null;
            }

            final boolean isTsv = assist.isTsvFile();
            final Optional<IRegion> variable = DocumentUtilities.findLiveVariable(document, isTsv, offset);

            final String prefix = variable.isPresent() ? DocumentUtilities.getPrefix(document, variable, offset) : "";
            final String content = variable.isPresent()
                    ? document.get(variable.get().getOffset(), variable.get().getLength()) : "";

            final List<RedVariableProposal> variableProposals = assist.getVariables(offset);
            removeInvisibleGlobalVariables(variableProposals, getVirtualContentType(document, offset));
            
            final List<ICompletionProposal> proposals = newArrayList();
            for (final RedVariableProposal varProposal : variableProposals) {
                if (varProposal.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
                    final String additionalInfo = createSecondaryInfo(varProposal);

                    final RedCompletionProposal proposal = RedCompletionBuilder.newProposal()
                            .will(assist.getAcceptanceMode())
                            .theText(varProposal.getName())
                            .atOffset(offset - prefix.length())
                            .givenThatCurrentPrefixIs(prefix)
                            .andWholeContentIs(content)
                            .secondaryPopupShouldBeDisplayed(additionalInfo)
                            .thenCursorWillStopAtTheEndOfInsertion()
                            .currentPrefixShouldBeDecorated()
                            .proposalsShouldHaveIcon(ImagesManager.getImage(varProposal.getImage()))
                            .create();
                    proposals.add(proposal);
                }
            }
            return proposals;
        } catch (final BadLocationException e) {
            return newArrayList();
        }
    }

    private boolean shouldShowProposals(final String lineContent, final IDocument document, final int offset)
            throws BadLocationException {
        return isInApplicableContentType(document, offset)
                && DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile()) >= 1;
    }

    private void removeInvisibleGlobalVariables(final List<RedVariableProposal> varProposals, final String contentType) {
        if(!SuiteSourcePartitionScanner.KEYWORDS_SECTION.equals(contentType)) {
            varProposals.remove(RedVariableProposal.createBuiltIn("${KEYWORD_STATUS}", ""));
            varProposals.remove(RedVariableProposal.createBuiltIn("${KEYWORD_MESSAGE}", ""));
        }
        if(SuiteSourcePartitionScanner.VARIABLES_SECTION.equals(contentType)) {
            varProposals.remove(RedVariableProposal.createBuiltIn("${TEST_NAME}", ""));
            varProposals.remove(RedVariableProposal.createBuiltIn("${TEST_DOCUMENTATION}", ""));
            varProposals.remove(RedVariableProposal.createBuiltIn("${TEST_STATUS}", ""));
            varProposals.remove(RedVariableProposal.createBuiltIn("${TEST_MESSAGE}", ""));
            varProposals.remove(RedVariableProposal.createBuiltIn("@{TEST_TAGS}", "[]"));
        }
        if(SuiteSourcePartitionScanner.TEST_CASES_SECTION.equals(contentType) || SuiteSourcePartitionScanner.VARIABLES_SECTION.equals(contentType)) {
            varProposals.remove(RedVariableProposal.createBuiltIn("${SUITE_STATUS}", ""));
            varProposals.remove(RedVariableProposal.createBuiltIn("${SUITE_MESSAGE}", ""));
        }
    }

    private static String createSecondaryInfo(final RedVariableProposal varProposal) {
        String info = "Source: " + varProposal.getSource() + "\n";
        final String value = varProposal.getValue();
        if (!value.isEmpty()) {
            info += "Value: " + value + "\n";
        }
        final String comment = varProposal.getComment();
        if (!comment.isEmpty()) {
            info += "Comment: " + comment;
        }
        return info;
    }
}
