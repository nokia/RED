package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * @author mmarzec
 *
 */
public class TxtScanner extends RuleBasedScanner {

	public TxtScanner() {
		
		TextAttribute sectionTextAttribute = new TextAttribute(SWTResourceManager.getColor(SWT.COLOR_RED));
		IToken sectionToken = new Token(sectionTextAttribute);
		TextAttribute variableTextAttribute = new TextAttribute(SWTResourceManager.getColor(SWT.COLOR_DARK_GREEN));
		IToken variableToken = new Token(variableTextAttribute);
		TextAttribute keywordTextAttribute = new TextAttribute(SWTResourceManager.getColor(0,174,249));
		IToken keywordToken = new Token(keywordTextAttribute);
		
		IRule[] rules = new IRule[3];
		
		rules[0] = new SingleLineRule("***", "***", sectionToken);
		
		rules[1] = new SingleLineRule("${", "}", variableToken);
		
		rules[2] = new KeywordRule(keywordToken);
		
		setRules(rules);
	}
}
