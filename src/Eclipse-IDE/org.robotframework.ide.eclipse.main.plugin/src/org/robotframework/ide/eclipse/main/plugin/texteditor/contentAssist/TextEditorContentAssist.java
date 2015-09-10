/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.HashMap;
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
    private static final String SECTION_PROPOSAL_SEPARATOR = "\n";
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
    
    private static List<RedVariableProposal> variableProposals = newArrayList();
    
    private TextEditorContentAssist() {
        
    }
    
    public static ICompletionProposal[] buildSectionProposals(final String replacedWord,
            final int offset) {

        final ICompletionProposal[] completionProposals = new ICompletionProposal[sections.size()];
        int index = 0;
        for (String proposal : sections) {
            completionProposals[index] = new CompletionProposal(proposal + SECTION_PROPOSAL_SEPARATOR, offset,
                    replacedWord.length(), proposal.length() + SECTION_PROPOSAL_SEPARATOR.length(), null, proposal, null,
                    null);
            index++;
        }
        return completionProposals;
    }
    
    public static ICompletionProposal[] buildSimpleProposals(final List<String> proposals, final String replacedWord,
            final int offset, final Image image) {

        if (proposals.size() == 0) {
            return new ICompletionProposal[0];
        }
        final ICompletionProposal[] completionProposals = new ICompletionProposal[proposals.size()];
        int index = 0;
        for (String proposal : proposals) {
            completionProposals[index] = new CompletionProposal(proposal + PROPOSAL_SEPARATOR, offset,
                    replacedWord.length(), proposal.length() + PROPOSAL_SEPARATOR.length(), image, proposal, null, null);
            index++;
        }
        return completionProposals;
    }
    
    public static ICompletionProposal[] buildKeywordsProposals(final Map<String, ContentAssistKeywordContext> proposals,
            final String replacedWord, final int offset) {

        if (proposals.size() == 0) {
            return new ICompletionProposal[0];
        }

        final ICompletionProposal[] completionProposals = new ICompletionProposal[proposals.size()];
        String separator = "";
        int index = 0;
        for (Iterator<String> i = proposals.keySet().iterator(); i.hasNext();) {
            final String proposal = (String) i.next();
            final ContentAssistKeywordContext keywordContext = proposals.get(proposal);
            
            if(!keywordContext.getArguments().equals("[]")) {
                separator = PROPOSAL_SEPARATOR;
            } else {
                separator = "";
            }
            completionProposals[index] = new TextEditorCompletionProposal(proposal + separator, offset,
                    replacedWord.length(), proposal.length() + separator.length(), keywordContext.getImage(),
                    proposal, new ContextInformation(proposal, keywordContext.getArguments()),
                    keywordContext.getDescription(), keywordContext.getLibName());
            index++;

        }

        return completionProposals;
    }
    
    public static ICompletionProposal[] buildVariablesProposals(final List<RedVariableProposal> proposals,
            final String replacedWord, final int offset) {

        if (proposals.size() == 0) {
            return new ICompletionProposal[0];
        }

        final ICompletionProposal[] completionProposals = new ICompletionProposal[proposals.size()];
        int index = 0;
        for (RedVariableProposal proposal : proposals) {
            final String variableName = proposal.getName();
            String info = "Source: " + proposal.getSource() + "\n";
            
            final String variableValue = proposal.getValue();
            if(variableValue != null && !variableValue.equals("")) {
                info += "Value: " + variableValue + "\n";
            }
            final String variableComment = proposal.getComment();
            if(variableComment != null && !variableComment.equals("")) {
                info += "Comment: " + variableComment;
            }
            
            completionProposals[index] = new TextEditorCompletionProposal(variableName, offset,
                    replacedWord.length(), variableName.length(), variableImage, variableName, null, info, null);
            index++;
        }

        return completionProposals;
    }
    
    public static List<String> filterProposals(final List<String> allProposals, final String filter) {
        List<String> filteredProposals = newArrayList();
        for (String word : allProposals) {
            if (word.toLowerCase().startsWith(filter.toLowerCase())) {
                filteredProposals.add(word);
            }
        }
        return filteredProposals;
    }
    
    public static Map<String, ContentAssistKeywordContext> filterKeywordsProposals(
            final Map<String, ContentAssistKeywordContext> keywordMap, final String filter) {
        Map<String, ContentAssistKeywordContext> keywordProposals = new LinkedHashMap<>();
        for (Iterator<String> i = keywordMap.keySet().iterator(); i.hasNext();) {
            String keyword = (String) i.next();
            if (keyword.toLowerCase().startsWith(filter.toLowerCase())) {
                keywordProposals.put(keyword, keywordMap.get(keyword));
            }
        }
        return keywordProposals;
    }
    
    public static List<RedVariableProposal> filterVariablesProposals(final List<RedVariableProposal> variables,
            final String filter) {
        List<RedVariableProposal> filteredProposals = newArrayList();
        for (RedVariableProposal variable : variables) {
            if (variable.getName().toLowerCase().contains(filter.toLowerCase())) {
                filteredProposals.add(variable);
            }
        }
        return filteredProposals;
    }
    
    public static String readEnteredWord(final int offset, final IDocument document) throws BadLocationException {
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
    
    public static String readEnteredKeyword(final int offset, final IDocument document) throws BadLocationException {
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
    
    public static String readEnteredVariable(final int offset, final IDocument document) throws BadLocationException {
        String currentWord = "";
        char currentChar, prevChar;
        int currentOffset = offset;
        while (currentOffset > 0 && document.getChar(currentOffset) != '\n') {
            currentChar = document.getChar(currentOffset);
            prevChar = document.getChar(currentOffset - 1);
            if (Character.isWhitespace(currentChar) && Character.isWhitespace(prevChar)) {
                break;
            }
            currentWord = currentChar + currentWord;
            currentOffset--;
        }
        
        return currentWord;
    }
    
    public static boolean shouldShowVariablesProposals(final String currentWord) {
        return currentWord.startsWith("$") || currentWord.startsWith("@") || currentWord.startsWith("&")
                || currentWord.startsWith("{");
    }

    public static List<String> getSettingsSectionWords() {
        return settingsSectionWords;
    }

    public static List<String> getKeywordsSectionWords() {
        return keywordsSectionWords;
    }

    public static List<String> getTestCasesSectionWords() {
        return testCasesSectionWords;
    }

    public static List<RedVariableProposal> getVariables() {
        return variableProposals;
    }

    public static void setVariables(List<RedVariableProposal> proposals) {
        variableProposals = proposals;
    }
}
