package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * @author mmarzec
 *
 */
public class TextEditorContentAssistProcessor implements IContentAssistProcessor {
	
	private String lastError = null;
	
	private TextEditorContextValidator validator = new TextEditorContextValidator(this);
	
	Map<String, String> sections = new LinkedHashMap<>();
	{
		sections.put("*** Variables ***", "");
		sections.put("*** Settings ***", "");
		sections.put("*** Test Cases ***", "");
		sections.put("*** Keywords ***", "");
	}
	
	Map<String, String> availableKeywords = new LinkedHashMap<>();
	{
		availableKeywords.put("Log", "Logs the given message with the given level.");
		availableKeywords.put("Log Many", "Logs the given messages as separate entries with the INFO level.");
		availableKeywords.put("Log Variables", "Logs all variables in the current scope with given log level.");
		availableKeywords.put("Replace Variables", "Replaces variables in the given text with their current values.");
	}
	
	Map<String, String> args = new LinkedHashMap<>();
	{
	    args.put("Log", "Arguments:[ message | level=INFO ]");
	    args.put("Log Many", "Arguments:[ *messages ]");
	    args.put("Log Variables", "Arguments:[ level=INFO ]");
	    args.put("Replace Variables", "Arguments:[ text ]");
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		
		IDocument document = viewer.getDocument();
	    int currOffset = offset - 1;
	    
	    try {
	        String currWord = "";
	        char currChar, prevChar;
	        
	        if(currOffset < 0 || document.getChar(currOffset) == '\n') {
	        	return buildProposals(sections, "", currOffset+1);
	        }
	        
	        while (currOffset > 0) {
	        	currChar = document.getChar(currOffset);
	        	prevChar = document.getChar(currOffset-1);
	        	if (Character.isWhitespace(currChar)) {
	        		if(Character.isWhitespace(prevChar) && prevChar != '\n' || currChar == '\t') {
	        			break;
	        		} else if(prevChar == '\n') {
	        			return new ICompletionProposal[0];
	        		}
	        	}
	        	currWord = currChar + currWord;
	        	currOffset--;
	        }
	        
	        Map<String, String> suggestions = new LinkedHashMap<>();
	        for (Iterator<String> i =  availableKeywords.keySet().iterator(); i.hasNext();) {
				String keyword = (String) i.next();
				if(keyword.startsWith(currWord)) {
					suggestions.put(keyword, availableKeywords.get(keyword));
				}
		    }
	        
	        ICompletionProposal[] proposals = null;
	        if (suggestions.size() > 0) {
	          proposals = buildProposals(suggestions, currWord, offset - currWord.length());
	          lastError = null;
	        }
	        return proposals;
	    } catch (BadLocationException e) {
	        e.printStackTrace();
	        lastError = e.getMessage();
	    }
	    
		return null;
	}
	
	private ICompletionProposal[] buildProposals(Map<String, String> suggestions, String replacedWord, int offset) {
		
		if(suggestions.size() == 0) {
			return new ICompletionProposal[0];
		}
		    
		ICompletionProposal[] proposals = new ICompletionProposal[suggestions.size()];
		    
	    int index = 0;
	    for (Iterator<String> i =  suggestions.keySet().iterator(); i.hasNext();) {
            String keywordSuggestion = (String) i.next();
            proposals[index] = new CompletionProposal(keywordSuggestion, offset, replacedWord.length(),
                    keywordSuggestion.length(), null, keywordSuggestion, new ContextInformation(keywordSuggestion,
                            args.get(keywordSuggestion)), suggestions.get(keywordSuggestion).toString());
            index++;
			
	    }
		
	    return proposals;
	}
	
	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		
		IContextInformation[] info = new IContextInformation[4];
		info[0] = new ContextInformation("Log", "Arguments:[ message | level=INFO ]");
		info[1] = new ContextInformation("Log Many", "Arguments:[ *messages ]");
		info[2] = new ContextInformation("Log Variables", "Arguments:[ level=INFO ]");
		info[3] = new ContextInformation("Replace Variables", "Arguments:[ text ]");
		
		return info;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		// TODO Auto-generated method stub
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
