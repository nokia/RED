/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.junit.Test;

public class RulesTest {

    @Test
    public void forLoop_item_textWithVariableAtBeginning_isNotVariableDeclaration_GITHUB_issue_21()
            throws BadLocationException {
        // prepare
        Token token = new Token(null);
        SuiteSourceTokenScanner scanner = new SuiteSourceTokenScanner(null);
        final String text = "|   |    ";
        final String last = "\\    |    ${s} a";
        final String full = text + last;

        Document document = new Document(full);
        scanner.setRange(document, 0, full.length());
        for (int i = 0; i < text.length(); i++) {
            scanner.read();
        }

        // execute
        IRule createKeywordCallRule = Rules.createKeywordCallRule(token);
        IToken evaluate = createKeywordCallRule.evaluate(scanner);

        // verify
        assertThat(evaluate.isUndefined()).isTrue();
        assertThat(evaluate).isSameAs(Token.UNDEFINED);
    }
}
