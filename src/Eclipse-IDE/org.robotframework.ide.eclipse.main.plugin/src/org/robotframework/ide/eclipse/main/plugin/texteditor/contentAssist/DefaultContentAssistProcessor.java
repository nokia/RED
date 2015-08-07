package org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class DefaultContentAssistProcessor implements IContentAssistProcessor {

    private String lastError = null;

    private List<String> sections = new ArrayList<>();
    {
        sections.add("*** Variables ***");
        sections.add("*** Settings ***");
        sections.add("*** Test Cases ***");
        sections.add("*** Keywords ***");
    }

    public DefaultContentAssistProcessor() {

    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {

        IDocument document = viewer.getDocument();
        int currOffset = offset - 1;

        try {
            String currWord = "";
            char currChar;

            if (currOffset < 0 || document.getChar(currOffset) == '\n') {
                return buildProposals(sections, currWord, offset - currWord.length());
            } else if (document.getChar(currOffset) == '*' || document.getChar(currOffset) == ' ') {
                while (currOffset >= 0 && document.getChar(currOffset) != '\n') {
                    currChar = document.getChar(currOffset);
                    currWord = currChar + currWord;
                    currOffset--;
                }
                return buildProposals(sections, currWord, offset - currWord.length());
            }
            
            lastError = null;
        } catch (BadLocationException e) {
            e.printStackTrace();
            lastError = e.getMessage();
        }

        return null;
    }

    private ICompletionProposal[] buildProposals(final List<String> proposals, final String replacedWord,
            final int offset) {

        if (proposals.size() == 0) {
            return new ICompletionProposal[0];
        }

        ICompletionProposal[] completionProposals = new ICompletionProposal[proposals.size()];

        int index = 0;
        for (String proposal : proposals) {
            completionProposals[index] = new TextEditorCompletionProposal(proposal, offset, replacedWord.length(),
                    proposal.length(), null, proposal, null, null, null);
            index++;

        }

        return completionProposals;
    }

    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {

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
