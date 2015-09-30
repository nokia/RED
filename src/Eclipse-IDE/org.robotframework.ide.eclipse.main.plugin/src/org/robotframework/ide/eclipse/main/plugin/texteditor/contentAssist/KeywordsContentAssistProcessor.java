/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposal;
import org.robotframework.red.graphics.ImagesManager;

/**
 * @author mmarzec
 */
public class KeywordsContentAssistProcessor implements IContentAssistProcessor {
    
    private final Image settingImage = ImagesManager.getImage(RedImages.getRobotSettingImage());

    private String lastError = null;

    private final TextEditorContextValidator validator = new TextEditorContextValidator(this);
    
    private final TextEditorContentAssist textEditorContentAssist;

    public KeywordsContentAssistProcessor(final TextEditorContentAssist textEditorContentAssist) {
        this.textEditorContentAssist = textEditorContentAssist;
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int offset) {
        lastError = null;
        final IDocument document = viewer.getDocument();
        final int currentOffset = offset - 1;

        try {
            String currentWord = "";

            if (currentOffset < 0 || document.getChar(currentOffset) == '\n' || document.getChar(currentOffset) == '*') {
                currentWord = TextEditorContentAssist.readEnteredWord(currentOffset, document);
                return TextEditorContentAssist.buildSectionProposals(currentWord, offset - currentWord.length());
            } else {

                currentWord = TextEditorContentAssist.readEnteredKeyword(currentOffset, document);
                if (currentWord == null) {
                    return new ICompletionProposal[0];
                }
                
                if(currentWord.startsWith("[")) {
                    return TextEditorContentAssist.buildSimpleProposals(TextEditorContentAssist.getKeywordsSectionWords(),
                            currentWord, offset - currentWord.length(), settingImage);
                }
                
                if (TextEditorContentAssist.shouldShowVariablesProposals(currentWord)) {
                    currentWord = TextEditorContentAssist.readEnteredVariable(currentOffset, document);
                    final List<RedVariableProposal> filteredProposals = TextEditorContentAssist.filterVariablesProposals(
                            textEditorContentAssist.getVariables(), currentWord);
                    if (!filteredProposals.isEmpty()) {
                        return TextEditorContentAssist.buildVariablesProposals(filteredProposals, currentWord, offset
                                - currentWord.length());
                    } else {
                        return new ICompletionProposal[0];
                    }
                }
                
                final Map<String, ContentAssistKeywordContext> keywordProposals = TextEditorContentAssist
                        .filterKeywordsProposals(textEditorContentAssist.getKeywordMap(), currentWord);
                if (keywordProposals.isEmpty()) {
                    return null;
                }
                return TextEditorContentAssist.buildKeywordsProposals(keywordProposals, currentWord,
                        offset - currentWord.length());
            }
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
        return validator;
    }

}
