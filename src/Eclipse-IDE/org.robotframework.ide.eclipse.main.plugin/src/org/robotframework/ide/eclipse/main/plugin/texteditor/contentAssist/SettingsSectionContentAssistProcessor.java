package org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.ImagesManager;

public class SettingsSectionContentAssistProcessor implements IContentAssistProcessor {

    private String lastError = null;

    private Image image = ImagesManager.getImage(RedImages.getRobotSettingImage());

    private boolean isSectionProposalsActive;

    private List<String> words = new ArrayList<>();
    {
        words.add("Library");
        words.add("Resource");
        words.add("Variables");
        words.add("Documentation");
        words.add("Metadata");
        words.add("Suite Setup");
        words.add("Suite Teardown");
        words.add("Force Tags");
        words.add("Default Tags");
        words.add("Test Setup");
        words.add("Test Teardown");
        words.add("Test Template");
        words.add("Test Timeout");
    }

    private List<String> sections = new ArrayList<>();
    {
        sections.add("*** Variables ***");
        sections.add("*** Settings ***");
        sections.add("*** Test Cases ***");
        sections.add("*** Keywords ***");
    }

    public SettingsSectionContentAssistProcessor() {

    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {

        IDocument document = viewer.getDocument();
        int currOffset = offset - 1;

        try {
            String currWord = "";
            char currChar;

            isSectionProposalsActive = false;

            if (document.getChar(currOffset) == '\n') {
                ICompletionProposal[] proposals = buildProposals(words, currWord, offset - currWord.length());
                return proposals;
            } else if (document.getChar(currOffset) == '*') {
                while (currOffset >= 0 && document.getChar(currOffset) != '\n') {
                    currChar = document.getChar(currOffset);
                    currWord = currChar + currWord;
                    currOffset--;
                }
                isSectionProposalsActive = true;
                return buildProposals(sections, currWord, offset - currWord.length());
            } else {
                while (currOffset > 0 && document.getChar(currOffset) != '\n') {
                    currChar = document.getChar(currOffset);
                    currWord = currChar + currWord;
                    currOffset--;
                }

                List<String> wordProposals = newArrayList();
                for (String word : words) {
                    if (word.startsWith(currWord)) {
                        wordProposals.add(word);
                    }
                }

                if (!wordProposals.isEmpty()) {
                    return buildProposals(wordProposals, currWord, offset - currWord.length());
                }

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

        Image proposalImage = null;
        if (!isSectionProposalsActive) {
            proposalImage = image;
        }

        int index = 0;
        for (String proposal : proposals) {
            completionProposals[index] = new TextEditorCompletionProposal(proposal, offset, replacedWord.length(),
                    proposal.length(), proposalImage, proposal, null, null, null);
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
