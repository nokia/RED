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
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;

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
        if (sectionHeader.isEmpty()) {
            return false;
        }
        return sectionIsOfExpectedType(scanner, sectionHeader, readAdditionally);
    }

    private boolean endDetected(final ICharacterScanner scanner) {
        if (scanner.getColumn() != 0) {
            return false;
        }
        final int readAdditionally = readBeforeSection(scanner);
        final String sectionHeader = getSectionHeader(scanner);

        if (sectionHeader.isEmpty()) {
            return false;
        }
        return !sectionIsOfExpectedType(scanner, sectionHeader, readAdditionally);
        // because we want to have a single partition for sequence of same-type tables
    }

    private boolean sectionIsOfExpectedType(final ICharacterScanner scanner, final String sectionHeader,
            final int readAdditionally) {
        final boolean startedWithPipedSeparator = readAdditionally > 1;
        if (sectionType.matches(sectionHeader, startedWithPipedSeparator)) {
            return true;
        } else {
            for (int i = 0; i < sectionHeader.length() + readAdditionally; i++) {
                scanner.unread();
            }
            return false;
        }
    }

    private static int readBeforeSection(final ICharacterScanner scanner) {
        int readAdditionally = 0;
        if (lookAhead(scanner) == ' ') {
            scanner.read();
            readAdditionally++;
        }
        if (lookAhead(scanner, 2).equals("| ") || lookAhead(scanner, 2).equals("|\t")) {
            readAdditionally += eatPipedLineStart(scanner);
        }
        // this is > 1 when pipe were read
        return readAdditionally;
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

    private static final String PIPED_SECTION_START = "^\\*(\\s*\\*)*\\s?";
    private static final String PIPED_SECTION_END = "(\\s*\\*)*((( \\|).*)?|\\s*)$";

    private static final String NORMAL_SECTION_START = "^\\*(\\s?\\*)*\\s?";
    private static final String NORMAL_SECTION_END = "\\s?(\\*\\s?)*(((  )|\\t).*)?$";


    private static final Pattern SETTINGS_NORMAL = Pattern.compile(
            NORMAL_SECTION_START + "(" + insensitiveWithSpace("Settings") + "|" + insensitiveWithSpace("Setting") + "|"
                    + insensitiveWithSpace("Metadata") + ")" + NORMAL_SECTION_END);
    private static final Pattern SETTINGS_PIPES = Pattern.compile(
            PIPED_SECTION_START + "(" + insensitiveWithSpaces("Settings") + "|" + insensitiveWithSpaces("Setting") + "|"
                    + insensitiveWithSpaces("Metadata") + ")" + PIPED_SECTION_END);

    private static final Pattern VARIABLES_NORMAL = Pattern.compile(NORMAL_SECTION_START + "("
            + insensitiveWithSpace("Variables") + "|" + insensitiveWithSpace("Variable") + ")" + NORMAL_SECTION_END);
    private static final Pattern VARIABLES_PIPES = Pattern.compile(PIPED_SECTION_START + "("
            + insensitiveWithSpace("Variables") + "|" + insensitiveWithSpace("Variable") + ")" + PIPED_SECTION_END);

    private static final Pattern KEYWORDS_NORMAL = Pattern.compile(NORMAL_SECTION_START + "("
            + insensitiveWithSpace("User") + "[\\s]?)?" + "(" + insensitiveWithSpace("Keywords") + "|"
            + insensitiveWithSpace("Keyword") + ")" + NORMAL_SECTION_END);
    private static final Pattern KEYWORDS_PIPES = Pattern.compile(PIPED_SECTION_START + "("
            + insensitiveWithSpaces("User") + "[\\s]*)?" + "(" + insensitiveWithSpaces("Keywords") + "|"
            + insensitiveWithSpaces("Keyword") + ")" + PIPED_SECTION_END);

    private static final Pattern TEST_CASE_NORMAL = Pattern
            .compile(NORMAL_SECTION_START + insensitiveWithSpace("Test") + "[\\s]?" + "("
                    + insensitiveWithSpace("Cases") + "|" + insensitiveWithSpace("Case") + ")" + NORMAL_SECTION_END);
    private static final Pattern TEST_CASE_PIPES = Pattern
            .compile(PIPED_SECTION_START + insensitiveWithSpaces("Test") + "[\\s]*" + "("
                    + insensitiveWithSpaces("Cases") + "|" + insensitiveWithSpaces("Case") + ")" + PIPED_SECTION_END);

    private static String insensitiveWithSpaces(final String text) {
        return ATokenRecognizer.createUpperLowerCaseWordWithSpacesInside(text);
    }

    private static String insensitiveWithSpace(final String text) {
        return ATokenRecognizer.createUpperLowerCaseWordWithOptionalSpaceInside(text);
    }

    static enum Section {
        TEST_CASES(TEST_CASE_NORMAL, TEST_CASE_PIPES),
        KEYWORDS(KEYWORDS_NORMAL, KEYWORDS_PIPES),
        SETTINGS(SETTINGS_NORMAL, SETTINGS_PIPES),
        VARIABLES(VARIABLES_NORMAL, VARIABLES_PIPES);

        private final Pattern normalPattern;

        private final Pattern pipedStartPattern;

        private Section(final Pattern normalPattern, final Pattern pipedStartPattenr) {
            this.normalPattern = normalPattern;
            this.pipedStartPattern = pipedStartPattenr;
        }

        boolean matches(final String header, final boolean startedWithPipedSeparator) {
            return (startedWithPipedSeparator ? pipedStartPattern : normalPattern).matcher(header).find();
        }
    }
}
