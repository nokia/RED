/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;

public class DefaultContentAssistProcessor implements IContentAssistProcessor {

    private String lastError = null;

    public DefaultContentAssistProcessor() {
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int offset) {
        final IDocument document = viewer.getDocument();

        final String lineDelimiter = DocumentUtilities.getDelimiter(document);
        final int currentOffset = offset - 1;
        try {
            String currentWord = "";

            if (currentOffset < 0 || document.getChar(currentOffset) == '\n') {
                return TextEditorContentAssist.buildSectionProposals(currentWord, lineDelimiter,
                        offset - currentWord.length());
            } else if (document.getChar(currentOffset) == '*' || document.getChar(currentOffset) == ' ') {
                currentWord = TextEditorContentAssist.readEnteredWord(currentOffset, document);
                return TextEditorContentAssist.buildSectionProposals(currentWord, lineDelimiter,
                        offset - currentWord.length());
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
