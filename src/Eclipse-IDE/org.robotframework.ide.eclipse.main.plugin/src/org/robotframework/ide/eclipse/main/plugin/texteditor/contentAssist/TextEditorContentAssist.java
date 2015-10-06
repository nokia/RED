/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposal;
import org.robotframework.red.graphics.ImagesManager;

public class TextEditorContentAssist {
    
    private static final String PROPOSAL_SEPARATOR = "  ";  //placed after proposal text
    private static Image variableImage = ImagesManager.getImage(RedImages.getRobotScalarVariableImage());

    private static List<String> sections = new ArrayList<>();
    static {
        sections.add("*** Variables ***");
        sections.add("*** Settings ***");
        sections.add("*** Test Cases ***");
        sections.add("*** Keywords ***");
    }
    
    private static List<String> settingsSectionWords = new ArrayList<>();

    static {
        settingsSectionWords.add("Library");
        settingsSectionWords.add("Resource");
        settingsSectionWords.add("Variables");
        settingsSectionWords.add("Documentation");
        settingsSectionWords.add("Metadata");
        settingsSectionWords.add("Suite Setup");
        settingsSectionWords.add("Suite Teardown");
        settingsSectionWords.add("Force Tags");
        settingsSectionWords.add("Default Tags");
        settingsSectionWords.add("Test Setup");
        settingsSectionWords.add("Test Teardown");
        settingsSectionWords.add("Test Template");
        settingsSectionWords.add("Test Timeout");
    }
    
    private static List<String> keywordsSectionWords = new ArrayList<>();
    static {
        keywordsSectionWords.add("[Documentation]");
        keywordsSectionWords.add("[Tags]");
        keywordsSectionWords.add("[Arguments]");
        keywordsSectionWords.add("[Return]");
        keywordsSectionWords.add("[Teardown]");
        keywordsSectionWords.add("[Timeout]");
    }
    
    private static List<String> testCasesSectionWords = new ArrayList<>();
    static {
        testCasesSectionWords.add("[Documentation]");
        testCasesSectionWords.add("[Tags]");
        testCasesSectionWords.add("[Setup]");
        testCasesSectionWords.add("[Teardown]");
        testCasesSectionWords.add("[Template]");
        testCasesSectionWords.add("[Timeout]");
    }
    
    private final List<RedVariableProposal> variableProposals;
    
    private final Map<String, ContentAssistKeywordContext> keywordMap;
    
    public TextEditorContentAssist(final List<RedVariableProposal> variableProposals,
            final Map<String, ContentAssistKeywordContext> keywordMap) {
        this.variableProposals = variableProposals;
        this.keywordMap = keywordMap;
    }

    public List<RedVariableProposal> getVariables() {
        return variableProposals;
    }

    public List<RedVariableProposal> getVariables(final int offset) {
        return variableProposals;
    }

    public Map<String, ContentAssistKeywordContext> getKeywordMap() {
        return keywordMap;
    }
    
    static ICompletionProposal[] buildSectionProposals(final String replacedWord, final String lineDelimiter,
            final int offset) {

        System.out.println(lineDelimiter.replaceAll("\n", "\\\n").replaceAll("\r", "\\\r"));
        final List<ICompletionProposal> completionProposals = newArrayList();
        for (final String proposal : sections) {
            completionProposals.add(new CompletionProposal(proposal + lineDelimiter, offset, replacedWord.length(),
                    proposal.length() + lineDelimiter.length(), null, proposal, null, null));
        }
        return completionProposals.toArray(new ICompletionProposal[0]);
    }
    
    static ICompletionProposal[] buildSimpleProposals(final List<String> proposals, final String replacedWord,
            final int offset, final Image image) {

        final List<ICompletionProposal> completionProposals = newArrayList();
        for (final String proposal : proposals) {
            completionProposals.add(new CompletionProposal(proposal + PROPOSAL_SEPARATOR, offset, replacedWord.length(),
                    proposal.length() + PROPOSAL_SEPARATOR.length(), image, proposal, null, null));
        }
        return completionProposals.toArray(new ICompletionProposal[0]);
    }
    
    static ICompletionProposal[] buildKeywordsProposals(final Map<String, ContentAssistKeywordContext> proposals,
            final String replacedWord, final int offset) {

        final List<ICompletionProposal> completionProposals = newArrayList();
        String separator = "";
        for (final String proposal : proposals.keySet()) {
            final ContentAssistKeywordContext keywordContext = proposals.get(proposal);
            separator = keywordContext.getArguments().equals("[]") ? "" : PROPOSAL_SEPARATOR;
            completionProposals.add(new TextEditorCompletionProposal(proposal + separator, offset,
                    replacedWord.length(), proposal.length() + separator.length(), keywordContext.getImage(), proposal,
                    new ContextInformation(proposal, keywordContext.getArguments()), keywordContext.getDescription(),
                    keywordContext.getLibName()));
        }

        return completionProposals.toArray(new ICompletionProposal[0]);
    }
    
    static ICompletionProposal[] buildVariablesProposals(final List<RedVariableProposal> proposals,
            final String replacedWord, final int offset) {

        final List<ICompletionProposal> completionProposals = newArrayList();
        for (final RedVariableProposal proposal : proposals) {
            final String variableName = proposal.getName();
            String info = "Source: " + proposal.getSource() + "\n";

            final String variableValue = proposal.getValue();
            if (variableValue != null && !variableValue.isEmpty()) {
                info += "Value: " + variableValue + "\n";
            }
            final String variableComment = proposal.getComment();
            if (variableComment != null && !variableComment.isEmpty()) {
                info += "Comment: " + variableComment;
            }

            completionProposals.add(new TextEditorCompletionProposal(variableName, offset, replacedWord.length(),
                    variableName.length(), variableImage, variableName, null, info, null));
        }

        return completionProposals.toArray(new ICompletionProposal[0]);
    }
    
    static List<String> filterProposals(final List<String> allProposals, final String filter) {
        final List<String> filteredProposals = newArrayList();
        for (final String word : allProposals) {
            if (word.toLowerCase().startsWith(filter.toLowerCase())) {
                filteredProposals.add(word);
            }
        }
        return filteredProposals;
    }
    
    static Map<String, ContentAssistKeywordContext> filterKeywordsProposals(
            final Map<String, ContentAssistKeywordContext> keywordMap, final String filter) {
        final Map<String, ContentAssistKeywordContext> keywordProposals = new LinkedHashMap<>();
        for (final Iterator<String> i = keywordMap.keySet().iterator(); i.hasNext();) {
            final String keyword = i.next();
            if (keyword.toLowerCase().startsWith(filter.toLowerCase())) {
                keywordProposals.put(keyword, keywordMap.get(keyword));
            }
        }
        return keywordProposals;
    }
    
    static List<RedVariableProposal> filterVariablesProposals(final List<RedVariableProposal> variables,
            final String filter) {
        final List<RedVariableProposal> filteredProposals = newArrayList();
        for (final RedVariableProposal variable : variables) {
            if (variable.getName().toLowerCase().contains(filter.toLowerCase())) {
                filteredProposals.add(variable);
            }
        }
        return filteredProposals;
    }
    
    static String readEnteredWord(final int offset, final IDocument document) throws BadLocationException {
        String currentWord = "";
        char currentChar;
        int currentOffset = offset;
        while (currentOffset >= 0 && document.getChar(currentOffset) != '\n') {
            currentChar = document.getChar(currentOffset);
            currentWord = currentChar + currentWord;
            currentOffset--;
        }
        
        return currentWord;
    }
    
    static String readEnteredKeyword(final int offset, final IDocument document) throws BadLocationException {
        String currentWord = "";
        char currentChar, prevChar;
        int currentOffset = offset;
        while (currentOffset > 0) {
            currentChar = document.getChar(currentOffset);
            prevChar = document.getChar(currentOffset - 1);
            if (Character.isWhitespace(currentChar)) {
                if (Character.isWhitespace(prevChar) && prevChar != '\n' || currentChar == '\t') {
                    break;
                } else if (prevChar == '\n') {
                    return null;
                }
            }
            if (prevChar == '\n') {
                return null;
            }
            currentWord = currentChar + currentWord;
            currentOffset--;
        }
        return currentWord;
    }
    
    static String readEnteredVariable(final int offset, final IDocument document) throws BadLocationException {
        String currentWord = "";
        char currentChar, prevChar;
        int currentOffset = offset;
        while (currentOffset > 0 && document.getChar(currentOffset) != '\n') {
            currentChar = document.getChar(currentOffset);
            prevChar = document.getChar(currentOffset - 1);
            if ((Character.isWhitespace(currentChar) && Character.isWhitespace(prevChar)) || currentChar == '\t') {
                break;
            }
            currentWord = currentChar + currentWord;
            currentOffset--;
        }
        
        return currentWord;
    }
    
    static boolean shouldShowVariablesProposals(final String currentWord) {
        return currentWord.startsWith("$") || currentWord.startsWith("@") || currentWord.startsWith("&")
                || currentWord.startsWith("{");
    }

    static List<String> getSettingsSectionWords() {
        return settingsSectionWords;
    }

    static List<String> getKeywordsSectionWords() {
        return keywordsSectionWords;
    }

    static List<String> getTestCasesSectionWords() {
        return testCasesSectionWords;
    }
}
