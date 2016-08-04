/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import java.util.regex.Pattern;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.rf.ide.core.testdata.text.read.recognizer.header.KeywordsTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.header.SettingsTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.header.TestCasesTableHeaderRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.header.VariablesTableHeaderRecognizer;

/**
 * @author Michal Anglart
 */
class SectionPartitionRule implements IPredicateRule {

    private final Section sectionType;

    private final IToken token;

    SectionPartitionRule(final Section sectionType, final IToken token) {
        this.sectionType = sectionType;
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
                int next = 0;
                while (!endDetected(scanner) && next != -1) {
                    next = scanner.read();
                }
                return token;
            }
        }
        return Token.UNDEFINED;
    }

    private boolean startDetected(final ICharacterScanner scanner) {
        if (scanner.getColumn() != 0) {
            return false;
        }
        final int readAdditionally = readBeforeSection(scanner);
        final String sectionHeader = getSectionHeader(scanner);
        if (!sectionHeader.isEmpty()) {
            if (sectionType.matches(sectionHeader)) {
                return true;
            } else {
                for (int i = 0; i < sectionHeader.length() + readAdditionally; i++) {
                    scanner.unread();
                }
            }
        }
        return false;
    }

    private boolean endDetected(final ICharacterScanner scanner) {
        if (scanner.getColumn() != 0) {
            return false;
        }
        final int readAdditionally = readBeforeSection(scanner);
        final String sectionHeader = getSectionHeader(scanner);
        if (!sectionHeader.isEmpty()) {
            for (int i = 0; i < sectionHeader.length() + readAdditionally; i++) {
                scanner.unread();
            }
            return true;
        }
        return false;
    }

    private static int readBeforeSection(final ICharacterScanner scanner) {
        if (lookAhead(scanner) == ' ') {
            scanner.read();
            return 1;
        } else if (lookAhead(scanner, 2).equals("| ") || lookAhead(scanner, 2).equals("|\t")) {
            return eatPipedLineStart(scanner);
        } else {
            return 0;
        }
    }

    static int lookAhead(final ICharacterScanner scanner) {
        final int ch = scanner.read();
        scanner.unread();
        return ch;
    }

    static String lookAhead(final ICharacterScanner scanner, final int n) {
        boolean eofOccured = false;
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            final int ch = scanner.read();
            if (ch == -1) {
                eofOccured = true;
                break;
            } else {
                builder.append((char) ch);
            }
        }
        final int additional = eofOccured ? 1 : 0;
        // maybe we've read less due to EOF
        for (int i = 0; i < builder.length() + additional; i++) {
            scanner.unread();
        }
        return builder.toString();
    }

    private static int eatPipedLineStart(final ICharacterScanner scanner) {
        int readAdditionally = 0;
        int ch = scanner.read();
        if (ch == '|') {
            readAdditionally++;

            ch = scanner.read();
            while (ch == ' ' || ch == '\t') {
                readAdditionally++;
                ch = scanner.read();
            }
        }
        scanner.unread();
        return readAdditionally;
    }

    private static String getSectionHeader(final ICharacterScanner scanner) {
        int next = scanner.read();
        if (next == '*') {
            final StringBuilder header = new StringBuilder();
            header.append((char) next);
            do {
                next = scanner.read();
                header.append((char) next);
            } while (next == '*');

            while (next == '*' || next == ' ' || next == '\t' || Character.isLetter(next)) {
                next = scanner.read();
                header.append((char) next);
            }
            header.deleteCharAt(header.length() - 1);
            scanner.unread();
            return header.toString();
        } else {
            scanner.unread();
            return "";
        }
    }

    static enum Section {
        TEST_CASES(TestCasesTableHeaderRecognizer.EXPECTED),
        KEYWORDS(KeywordsTableHeaderRecognizer.EXPECTED),
        SETTINGS(SettingsTableHeaderRecognizer.EXPECTED),
        VARIABLES(VariablesTableHeaderRecognizer.EXPECTED);

        private Pattern pattern;

        private Section(final Pattern pattern) {
            this.pattern = pattern;
        }

        boolean matches(final String header) {
            return pattern.matcher(header).find();
        }
    }
}
