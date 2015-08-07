package org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;

/**
 * @author mmarzec
 */
public class KeywordsContentAssistProcessor implements IContentAssistProcessor {

    private String lastError = null;

    private boolean showSections;

    private TextEditorContextValidator validator = new TextEditorContextValidator(this);

    private Map<String, TextEditorContentAssistKeywordContext> sections = new LinkedHashMap<>();
    {
        sections.put("*** Variables ***", null);
        sections.put("*** Settings ***", null);
        sections.put("*** Test Cases ***", null);
        sections.put("*** Keywords ***", null);
    }

    private Map<String, TextEditorContentAssistKeywordContext> keywordMap;

    public KeywordsContentAssistProcessor(Map<String, TextEditorContentAssistKeywordContext> keywordMap) {
        this.keywordMap = keywordMap;
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {

        IDocument document = viewer.getDocument();
        int currOffset = offset - 1;

        try {
            String currWord = "";
            char currChar, prevChar;

            if (currOffset < 0 || document.getChar(currOffset) == '\n') {
                showSections = true;
                return buildProposals(sections, "", currOffset + 1);
            } else {
                showSections = false;
            }

            if (currOffset == 0) {
                return new ICompletionProposal[0];
            }

            while (currOffset > 0) {
                currChar = document.getChar(currOffset);
                prevChar = document.getChar(currOffset - 1);
                if (Character.isWhitespace(currChar)) {
                    if (Character.isWhitespace(prevChar) && prevChar != '\n' || currChar == '\t') {
                        break;
                    } else if (prevChar == '\n') {
                        return new ICompletionProposal[0];
                    }
                }
                if (prevChar == '\n') {
                    return new ICompletionProposal[0];
                }
                currWord = currChar + currWord;
                currOffset--;
            }

            Map<String, TextEditorContentAssistKeywordContext> keywordProposals = new LinkedHashMap<>();
            for (Iterator<String> i = keywordMap.keySet().iterator(); i.hasNext();) {
                String keyword = (String) i.next();
                if (keyword.startsWith(currWord)) {
                    keywordProposals.put(keyword, keywordMap.get(keyword));
                }
            }

            ICompletionProposal[] proposals = null;
            if (keywordProposals.size() > 0) {
                proposals = buildProposals(keywordProposals, currWord, offset - currWord.length());
                lastError = null;
            }
            return proposals;
        } catch (BadLocationException e) {
            e.printStackTrace();
            lastError = e.getMessage();
        }

        return null;
    }

    private ICompletionProposal[] buildProposals(final Map<String, TextEditorContentAssistKeywordContext> proposals,
            final String replacedWord, final int offset) {

        if (proposals.size() == 0) {
            return new ICompletionProposal[0];
        }

        ICompletionProposal[] completionProposals = new ICompletionProposal[proposals.size()];

        int index = 0;
        for (Iterator<String> i = proposals.keySet().iterator(); i.hasNext();) {
            String proposal = (String) i.next();

            ContextInformation contextInfo = null;
            String additionalInfo = null;
            String sourceName = null;
            Image image = null;
            if (!showSections) {
                TextEditorContentAssistKeywordContext keywordContext = proposals.get(proposal);
                contextInfo = new ContextInformation(proposal, keywordContext.getArguments());
                additionalInfo = keywordContext.getDescription();
                sourceName = keywordContext.getLibName();
                image = keywordContext.getImage();
            }

            completionProposals[index] = new TextEditorCompletionProposal(proposal, offset, replacedWord.length(),
                    proposal.length(), image, proposal, contextInfo, additionalInfo, sourceName);
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
        return validator;
    }

}
