package org.robotframework.ide.eclipse.main.plugin.texteditor.syntaxHighlighting;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;

public class TextEditorScanner extends RuleBasedScanner {

    public TextEditorScanner(IRule[] rules) {
        setRules(rules);
    }
}
