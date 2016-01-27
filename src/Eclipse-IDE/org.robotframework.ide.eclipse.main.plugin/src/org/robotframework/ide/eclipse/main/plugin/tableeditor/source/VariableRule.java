/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

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
            int charactersRead = 0;
            if (startDetected(scanner)) {
                int next = CharacterScannerUtilities.eat(scanner, 2);
                charactersRead = 2;

                int balance = 1;
                while (true) {
                    if (startDetected(scanner)) {
                        next = CharacterScannerUtilities.eat(scanner, 2);
                        charactersRead += 2;
                        balance++;
                    } else if (endDetected(scanner)) {
                        next = CharacterScannerUtilities.eat(scanner, 1);
                        charactersRead += 1;
                        balance--;
                        if (balance == 0) {
                            return token;
                        }
                    } else if (isCellEnd(scanner, next)) {
                        for (int i = 0; i < charactersRead; i++) {
                            scanner.unread();
                        }
                        return Token.UNDEFINED;

                    } else {
                        charactersRead += 1;
                        next = scanner.read();
                    }
                }
            }
        }
        return Token.UNDEFINED;
    }

    private boolean isCellEnd(final ICharacterScanner scanner, final int next) {
        return next == -1 || next == '\n' || next == '\r' || next == '\t'
                || CharacterScannerUtilities.lookAhead(scanner, 2).equals("  ");
    }

    private boolean startDetected(final ICharacterScanner scanner) {
        final String lookahed = CharacterScannerUtilities.lookAhead(scanner, 2);
        return "${".equals(lookahed) || "@{".equals(lookahed) || "&{".equals(lookahed) || "%{".equals(lookahed);
    }

    private boolean endDetected(final ICharacterScanner scanner) {
        return CharacterScannerUtilities.lookAhead(scanner, 1).equals("}");
    }
}
