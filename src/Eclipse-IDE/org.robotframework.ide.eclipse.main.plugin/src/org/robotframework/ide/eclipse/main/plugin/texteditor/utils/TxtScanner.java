package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import static com.google.common.collect.Sets.newHashSet;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Display;

/**
 * @author mmarzec
 *
 */
public class TxtScanner extends RuleBasedScanner {

    // TODO : colors for syntax highlighting should be handled via some other
    // mechanism (preferences probably)

    private final Set<Resource> resources = newHashSet();
    
    public TxtScanner(final Display display, List<String> keywordList) {
        final Color redSystemColor = display.getSystemColor(SWT.COLOR_RED);
        final Color greenSystemColor = display.getSystemColor(SWT.COLOR_DARK_GREEN);
        final Color graySystemColor = display.getSystemColor(SWT.COLOR_DARK_GRAY);
        final Color userColor = new Color(display, 0, 128, 192);
        resources.add(userColor);
        final Color userSettingsColor = new Color(display, 149, 0, 85);
        resources.add(userSettingsColor);

        final IToken sectionToken = new Token(new TextAttribute(redSystemColor));
        final IToken variableToken = new Token(new TextAttribute(greenSystemColor));
        final IToken commentToken = new Token(new TextAttribute(graySystemColor));
        final IToken keywordToken = new Token(new TextAttribute(userColor, null, SWT.BOLD));
        final IToken settingsToken = new Token(new TextAttribute(userSettingsColor));
        
        WordRule settingsWordRule = new WordRule(new IWordDetector() {
            
            @Override
            public boolean isWordStart(char c) {
                return !Character.isWhitespace(c);
            }
            
            @Override
            public boolean isWordPart(char c) {
                return !Character.isWhitespace(c);
            }
        }); 
        settingsWordRule.addWord("Library", settingsToken);
        settingsWordRule.addWord("Resource", settingsToken);
        
        
        
        setRules(new IRule[] { 
                new SingleLineRule("***", "***", sectionToken),
                new SingleLineRule("${", "}", variableToken),
                new SingleLineRule("@{", "}", variableToken), 
                new SingleLineRule("&{", "}", variableToken),
                new EndOfLineRule("#", commentToken),
                new KeywordRule(keywordToken, keywordList),
                settingsWordRule
        });
	}

    public void disposeResources() {
        for (final Resource resource : resources) {
            resource.dispose();
        }
    }
}
