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
	List<String> temp = new ArrayList<>();
	
	{
		keywords.add("Log");
		keywords.add("Log Many");
		keywords.add("Log Variables");
		keywords.add("Replace Variables");
	}
	
	private IToken token;
	
	public KeywordRule(IToken token) {
		this.token = token;
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		//char[][] d = scanner.getLegalLineDelimiters();
		
		temp = keywords;
		
		int c= scanner.read();
		
		if(isInKeyword((char) c, 1, temp)) {
			if(keywordDetected(scanner)) {
				int next = scanner.read();
				char nextChar = (char) next;
				scanner.unread();
				if(nextChar == ' ' || nextChar == '\t' || nextChar == '\n' || next == ICharacterScanner.EOF) {
					return token;
				}
			}
		}
		
		scanner.unread();
		return Token.UNDEFINED;
	}
	
	private boolean keywordDetected(ICharacterScanner scanner) {
		int readCount= 1;
		int c;
		while ((c= scanner.read()) != ICharacterScanner.EOF) {
			char elem = (char) c;
			
			if(isInKeyword(elem, readCount+1, temp)) {
				if(temp.size()==1 && (readCount+1) == temp.get(0).length()) {
					return true;
				}
			} else {
				
				for (; readCount > 0; readCount--)
					scanner.unread();
				
				return false;
			}
			
			readCount++;
		}
		
		for (; readCount > 0; readCount--)
			scanner.unread();
		
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
			temp = newList;
		
		return hasChar;
	}

}
