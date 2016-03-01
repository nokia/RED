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
 *
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
        final int readAdditionally = eatPipedLineStart(scanner);

        final String sectionHeader = Rules.getSectionHeader(scanner);
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
        final int readAdditionally = eatPipedLineStart(scanner);

        final String sectionHeader = Rules.getSectionHeader(scanner);
        if (!sectionHeader.isEmpty()) {
            for (int i = 0; i < sectionHeader.length() + readAdditionally; i++) {
                scanner.unread();
            }
            return true;
        }
        return false;
    }

    private int eatPipedLineStart(final ICharacterScanner scanner) {
        int readAdditionally = 0;
        if (CharacterScannerUtilities.lookAhead(scanner, 1).equals("|")) {
            scanner.read();
            readAdditionally++;
            String next = CharacterScannerUtilities.lookAhead(scanner, 1);
            while (next.equals(" ") || next == "\t") {
                scanner.read();
                readAdditionally++;
                next = CharacterScannerUtilities.lookAhead(scanner, 1);
            }
        }
        return readAdditionally;
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
