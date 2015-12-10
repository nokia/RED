/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Sets.newHashSet;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;


/**
 * @author Michal Anglart
 *
 */
class VariableRule implements IPredicateRule {

    private final IToken token;

    VariableRule(final IToken token) {
        this.token = token;
    }

    @Override
    public IToken evaluate(final ICharacterScanner scanner) {
        return evaluate(scanner, false);
    }

    @Override
    public IToken getSuccessToken() {
        return token;
    }

    @Override
    public IToken evaluate(final ICharacterScanner scanner, final boolean resume) {
        if (resume) {
            if (endDetected(scanner)) {
                return token;
            }
        } else {
            if (startDetected(scanner)) {
                int balance = 1;
                int next = 0;
                while (true) {
                    if (endDetected(scanner)) {
                        balance--;
                    }
                    if (balance == 0 || next == -1 || next == '\n' || next == '\r'
                            || next == '\t' && CharacterScannerUtilities.lookAhead(scanner, 2).equals("  ")) {
                        break;
                    }
                    next = scanner.read();

                    if (startDetected(scanner)) {
                        balance++;
                    }
                }
                scanner.read();
                return token;
            }
        }
        return Token.UNDEFINED;
    }

    private boolean startDetected(final ICharacterScanner scanner) {
        return newHashSet("${", "@{", "&{", "%{").contains(CharacterScannerUtilities.lookAhead(scanner, 2));
    }

    private boolean endDetected(final ICharacterScanner scanner) {
        return CharacterScannerUtilities.lookAhead(scanner, 1).equals("}");
    }
}
