package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

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
public class KeywordRule implements IRule {
	
	List<String> keywords = new ArrayList<>();
	List<String> tempKeywords = new ArrayList<>();

	private IToken token;
	
	private char prevCharBeforeKeyword = ' ';
	
	private int currentReadCount = 0;
	
	public KeywordRule(IToken token, List<String> keywords) {
	    this.token = token;
		this.keywords = keywords;
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		//char[][] d = scanner.getLegalLineDelimiters();
		
		tempKeywords = keywords;
		
		int c= scanner.read();
		
		if(isInKeyword((char) c, 1, tempKeywords)) {
			if(keywordDetected(scanner)) {
				int nextCharAfterKeyword = scanner.read();
				scanner.unread();
				if((isSeparator((char) nextCharAfterKeyword) || nextCharAfterKeyword == ICharacterScanner.EOF) && isSeparator(prevCharBeforeKeyword)) {
					return token;
				} else {
				    clearScanner(scanner, currentReadCount);
				}
			}
		}
		
		prevCharBeforeKeyword = (char) c;
		scanner.unread();
		return Token.UNDEFINED;
	}
	
	private boolean keywordDetected(ICharacterScanner scanner) {
	   
		int readCount= 1;
		int c;
		while ((c= scanner.read()) != ICharacterScanner.EOF) {
			char elem = (char) c;
			
			if(elem == ' ') {
			    char nextElem = (char) scanner.read();
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
			    this.clearScanner(scanner, readCount);
				return false;
			}
			
			readCount++;
		}
		
		currentReadCount = readCount;
		if(findKeywordByLength(tempKeywords, readCount)) {
		    return true;
		} 
		
		this.clearScanner(scanner, readCount);
		return false;
	}
	
	private void clearScanner(ICharacterScanner scanner, int readCount) {
	    for (; readCount > 1; readCount--)
            scanner.unread();
	}
	
	private boolean findKeywordByLength(List<String> list, int readCount) {
	    for (String keyword : list) {
            if(keyword.length() == readCount) {
                return true;
            }
        }
	    return false;
	}
	
	private boolean isInKeyword(char c, int position, List<String> list) {
		List<String> newList = new ArrayList<>();
		boolean hasChar = false;
		for (String keyword : list) {
			if(keyword.length() >= position && keyword.charAt(position-1) == c) {
				newList.add(keyword);
				hasChar = true;
			}
		}
		
		if(hasChar)
			tempKeywords = newList;
		
		return hasChar;
	}
	
	private boolean isSeparator(char c) {
	    if(Character.isWhitespace(c) || !Character.isDefined(c)) {
	        return true;
	    }
	    return false;
	}

}
