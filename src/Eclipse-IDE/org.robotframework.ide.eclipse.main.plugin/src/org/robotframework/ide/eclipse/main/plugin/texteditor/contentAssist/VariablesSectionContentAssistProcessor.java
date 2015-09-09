/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist;

import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class VariablesSectionContentAssistProcessor implements IContentAssistProcessor {

    private String lastError = null;

    public VariablesSectionContentAssistProcessor() {
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int offset) {

        final IDocument document = viewer.getDocument();
        int currentOffset = offset - 1;

        try {
            String currentWord = "";

            if (document.getChar(currentOffset) == '*') {
                currentWord = TextEditorContentAssist.readEnteredWord(currentOffset, document);
                return TextEditorContentAssist.buildSectionProposals(currentWord, offset - currentWord.length());
            } else {
                currentWord = TextEditorContentAssist.readEnteredVariable(currentOffset, document);
                final Map<String, String> filteredProposals = TextEditorContentAssist.filterVariablesProposals(
                        TextEditorContentAssist.getVariables(), currentWord);
                if (!filteredProposals.isEmpty()) {
                    return TextEditorContentAssist.buildVariablesProposals(filteredProposals, currentWord, offset
                            - currentWord.length());
                }
            }
            lastError = null;
        } catch (final BadLocationException e) {
            e.printStackTrace();
            lastError = e.getMessage();
        }

        return null;
    }

    @Override
    public IContextInformation[] computeContextInformation(final ITextViewer viewer, final int offset) {

        return new IContextInformation[0];
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return null;
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return lastError;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return new TextEditorContextValidator(this);
    }

}
