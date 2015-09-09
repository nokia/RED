package org.robotframework.ide.eclipse.main.plugin.texteditor.syntaxHighlighting;

import java.util.List;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;

/**
 * @author mmarzec
 */
public class SectionRule extends KeywordRule {

    public SectionRule(final IToken token, final List<String> keywords) {
        super(token, keywords);
    }

    protected boolean isColumnConstraintCompliant(final int column, final char charBeforeKeyword) {
        return true;
    }

    protected int extractSecondNextCharAfterKeyword(final ICharacterScanner scanner, final int nextCharAfterKeyword) {
        return ICharacterScanner.EOF;
    }
}
