/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.syntaxHighlighting;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * @author mmarzec
 *
 */
class KeywordRule implements IRule {
	
    private List<String> keywords = new ArrayList<>();
    private List<String> tempKeywords = new ArrayList<>();

	private final IToken token;
	
	private char prevCharBeforeKeyword = ' ';
	
	private int currentReadCount = 0;
	
	private int columnConstraint = -1;
	
    KeywordRule(final IToken token, final List<String> keywords) {
	    this.token = token;
		this.keywords = keywords;
	}

	@Override
	public IToken evaluate(final ICharacterScanner scanner) {
		//char[][] d = scanner.getLegalLineDelimiters();
		
		tempKeywords = keywords;
		
		final int c= scanner.read();
		
		if (isColumnConstraintCompliant(scanner.getColumn(), prevCharBeforeKeyword)) {
    		
		    if(isInKeyword((char) c, 1, tempKeywords)) {
    			if(keywordDetected(scanner)) {
    				final int nextCharAfterKeyword = scanner.read();
    				int secondNextCharAfterKeyword = ICharacterScanner.EOF;
    				if(nextCharAfterKeyword != ICharacterScanner.EOF && nextCharAfterKeyword != '\t') {
    				    secondNextCharAfterKeyword = scanner.read();
    				    scanner.unread();
    				}
    				scanner.unread();
                    if (isSeparator((char) nextCharAfterKeyword) && isSeparator((char) secondNextCharAfterKeyword)
                            && (isSeparator(prevCharBeforeKeyword) || columnConstraint == 0)) {
                        return token;
    				} else {
    				    clearScanner(scanner, currentReadCount);
    				}
    			}
    		}
    		
    		
		}
		prevCharBeforeKeyword = (char) c;
		
		scanner.unread();
		return Token.UNDEFINED;
	}
	
	public void setColumnConstraint(final int column) {
	    columnConstraint = column;
	}
	
	private boolean isColumnConstraintCompliant(final int column, final char charBeforeKeyword) {
	    
	    if (columnConstraint > -1) {
	        if(columnConstraint == (column - 1)) {
	            return true;
	        }
	    } else {
	        if(charBeforeKeyword == '\t' || (column - 1) > 1) {    //tab or 2 and more spaces after new line
	            return true;
	        }
	    }
	    return false;
	}
	
	private boolean keywordDetected(final ICharacterScanner scanner) {
	   
		int readCount= 1;
		int c;
		while ((c= scanner.read()) != ICharacterScanner.EOF) {
			final char elem = (char) c;
			
			if(elem == ' ') {
			    final char nextElem = (char) scanner.read();
			    scanner.unread();
			    if(nextElem == ' ' && findKeywordByLength(tempKeywords, readCount)) {
			        currentReadCount = readCount;
			        scanner.unread();
	                return true;
			    }
			}
			
			if(elem == '\t' || elem == '\n' || elem == '\r' && findKeywordByLength(tempKeywords, readCount)) {
			    currentReadCount = readCount;
			    scanner.unread();
			    return true;
			}
			
			if(isInKeyword(elem, readCount+1, tempKeywords)) {
				if(tempKeywords.size()==1 && (readCount+1) == tempKeywords.get(0).length()) {
				    currentReadCount = readCount;
					return true;
				}
			} else {
			    this.clearScanner(scanner, readCount+1);
				return false;
			}
			
			readCount++;
		}
		
		currentReadCount = readCount;
		if(findKeywordByLength(tempKeywords, readCount)) {
		    return true;
		} 
		
		this.clearScanner(scanner, readCount+1);
		return false;
	}
	
	private void clearScanner(final ICharacterScanner scanner, int readCount) {
	    for (; readCount > 1; readCount--) {
            scanner.unread();
        }
	}
	
	private boolean findKeywordByLength(final List<String> list, final int readCount) {
	    for (final String keyword : list) {
            if(keyword.length() == readCount) {
                return true;
            }
        }
	    return false;
	}
	
	private boolean isInKeyword(final char c, final int position, final List<String> list) {
		final List<String> newList = new ArrayList<>();
		boolean hasChar = false;
		for (final String keyword : list) {
			if(keyword.length() >= position && keyword.substring(position-1, position).equalsIgnoreCase(String.valueOf(c))) {
				newList.add(keyword);
				hasChar = true;
			}
		}
		
		if(hasChar) {
            tempKeywords = newList;
        }
		
		return hasChar;
	}
	
    private boolean isSeparator(final char c) {
	    return Character.isWhitespace(c) || c == ICharacterScanner.EOF || !Character.isDefined(c);
	}

}
