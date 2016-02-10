/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import com.google.common.base.Splitter;

/**
 * @author Michal Anglart
 *
 */
public class Rules {

    private static final int EOF = -1;

    private static IRule createCombinedRule(final IRule... rules) {
        return new IRule() {

            @Override
            public IToken evaluate(final ICharacterScanner scanner) {
                for (final IRule rule : rules) {
                    final IToken token = rule.evaluate(scanner);
                    if (!token.isUndefined()) {
                        return token;
                    }
                }
                return Token.UNDEFINED;
            }
        };
    }

    static IRule createReadAllRule(final IToken token) {
        return new IRule() {
            @Override
            public IToken evaluate(final ICharacterScanner scanner) {
                int next = scanner.read();
                scanner.unread();
                if (next == EOF || Character.isWhitespace(next)) {
                    return Token.UNDEFINED;
                }
                while (true) {
                    next = scanner.read();

                    if (next == EOF || Character.isWhitespace(next)) {
                        scanner.unread();
                        return token;
                    }
                }
            }
        };
    }

    static IRule createVariableRule(final IToken token) {
        return new VariableRule(token);
    }

    static IRule createCommentRule(final IToken token) {
        return createCombinedRule(new CommentOnLineBeginRule(token), new EndOfLineRule("  #", token),
                new EndOfLineRule("\t#", token), new EndOfLineRule("| #", token));
    }

    static IRule createSectionHeaderRule(final IToken token) {
        return new IRule() {
            @Override
            public IToken evaluate(final ICharacterScanner scanner) {
                return getSectionHeader(scanner).isEmpty() ? Token.UNDEFINED : token;
            }
        };
    }

    static String getSectionHeader(final ICharacterScanner scanner) {
        if (scanner.getColumn() > 1
                || scanner.getColumn() == 1 && CharacterScannerUtilities.lookBack(scanner, 1).equals("#")) {
            return "";
        }
        final StringBuilder header = new StringBuilder();
        int next = scanner.read();
        if (next == '*') {
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

    static IRule createKeywordDefinitionRule(final IToken token) {
        return new IRule() {

            @Override
            public IToken evaluate(final ICharacterScanner scanner) {
                if (scanner.getColumn() > 1 || scanner.getColumn() == 1 && CharacterScannerUtilities.lookBack(scanner, 1).equals("\t")) {
                    if (((SuiteSourceTokenScanner) scanner).numberOfCellSeparatorsInLineBeforeOffset() != 0) {
                        return Token.UNDEFINED;
                    }
                }

                final String n = CharacterScannerUtilities.lookAhead(scanner, 1);
                if (!n.isEmpty() && Character.isAlphabetic(n.charAt(0))) {
                    while (true) {
                        final int ch = scanner.read();

                        final String next = CharacterScannerUtilities.lookAhead(scanner, 3);
                        if (CharacterScannerUtilities.isCellSeparator(next) || next.startsWith("$")) {
                            return token;
                        }
                        if (ch == EOF || ch == '\r' || ch == '\n') {
                            scanner.unread();
                            return token;
                        }
                    }
                } else {
                    return Token.UNDEFINED;
                }
            }
        };
    }

    static IRule createDefinitionRule(final IToken token) {
        return new IRule() {

            @Override
            public IToken evaluate(final ICharacterScanner scanner) {
                if (scanner.getColumn() > 1) {
                    return Token.UNDEFINED;
                }
                if (scanner.getColumn() == 1 && CharacterScannerUtilities.lookBack(scanner, 1).equals("\t")) {
                    return Token.UNDEFINED;
                }

                final String n = CharacterScannerUtilities.lookAhead(scanner, 1);
                if (!n.isEmpty() && (Character.isAlphabetic(n.charAt(0)) || Character.isDigit(n.charAt(0)))) {
                    while (true) {
                        final int ch = scanner.read();

                        if (CharacterScannerUtilities
                                .isCellSeparator(CharacterScannerUtilities.lookAhead(scanner, 3))) {
                            return token;
                        }
                        if (ch == EOF || ch == '\r' || ch == '\n') {
                            scanner.unread();
                            return token;
                        }
                    }
                } else {
                    return Token.UNDEFINED;
                }
            }
        };
    }

    static IRule createLocalSettingRule(final IToken token) {
        return new IRule() {

            @Override
            public IToken evaluate(final ICharacterScanner scanner) {
                if (CharacterScannerUtilities.lookAhead(scanner, 1).equals("[")
                        && ((SuiteSourceTokenScanner) scanner).numberOfCellSeparatorsInLineBeforeOffset() == 1) {
                    int charactersRead = 0;
                    while (true) {
                        final int ch = scanner.read();
                        charactersRead++;

                        final String lookAhead = CharacterScannerUtilities.lookAhead(scanner, 3);
                        if (ch == ']' && (CharacterScannerUtilities.isCellSeparator(lookAhead) || lookAhead.isEmpty()
                                || lookAhead.startsWith("\n") || lookAhead.startsWith("\r")
                                || lookAhead.startsWith(" \n") || lookAhead.startsWith(" \r")
                                || lookAhead.equals(" "))) {
                            return token;
                        } else if (ch == EOF || ch == '\r' || ch == '\n' || CharacterScannerUtilities
                                .isCellSeparator(lookAhead)) {
                            for (int i = 0; i < charactersRead; i++) {
                                scanner.unread();
                            }
                            return Token.UNDEFINED;
                        }
                    }
                }
                return Token.UNDEFINED;
            }
        };
    }

    static IRule createKeywordUsageInSettings(final IToken token) {
        return new IRule() {

            @Override
            public IToken evaluate(final ICharacterScanner scanner) {
                final int ch = scanner.read();
                scanner.unread();
                if (ch == EOF || Character.isWhitespace(ch)) {
                    return Token.UNDEFINED;
                }
                final String lineContentBefore = ((SuiteSourceTokenScanner) scanner).lineContentBeforeCurrentPosition();

                if (isKeywordBasedSetting(lineContentBefore)
                        && ((SuiteSourceTokenScanner) scanner).numberOfCellSeparatorsInLineBeforeOffset() == 1) {
                    consumeWholeToken(scanner);
                    return token;
                } else {
                    return Token.UNDEFINED;
                }
            }

            private boolean isKeywordBasedSetting(final String lineContentBefore) {
                final String lowerCasedLine = lineContentBefore.trim().toLowerCase();
                if (lowerCasedLine.contains("suite")) {
                    return lowerCasedLine.contains("suite setup") || lowerCasedLine.contains("suite precondition") ||
                            lowerCasedLine.contains("suite teardown") || lowerCasedLine.contains("suite postcondition");
                } else if (lowerCasedLine.contains("test")) {
                    return lowerCasedLine.contains("test setup") || lowerCasedLine.contains("test precondition") ||
                            lowerCasedLine.contains("test teardown") || lowerCasedLine.contains("test postcondition") ||
                            lowerCasedLine.contains("test template");
                }
                return false;
            }
        };
    }

    static IRule createKeywordCallRule(final IToken token) {
        return new IRule() {

            @Override
            public IToken evaluate(final ICharacterScanner scanner) {
                final int ch = scanner.read();
                scanner.unread();
                if (ch == EOF || Character.isWhitespace(ch) || ch == '|') {
                    return Token.UNDEFINED;
                }
                final String lineContentBefore = ((SuiteSourceTokenScanner) scanner).lineContentBeforeCurrentPosition();
                if (isAssignment(ch, lineContentBefore)) {
                    return Token.UNDEFINED;
                }
                final int numberOfCellSeparators = ((SuiteSourceTokenScanner) scanner)
                        .numberOfCellSeparatorsInLineBeforeOffset();
                if (numberOfCellSeparators == 1) {
                    consumeWholeToken(scanner);
                    return token;
                } else if (numberOfCellSeparators > 1) {

                    // remove FOR-loop body indicator
                    String lineBeginWithoutForBodyIndicator = lineContentBefore;
                    final Matcher matcher = Pattern.compile(" ( )+\\\\ ( )+")
                            .matcher(lineBeginWithoutForBodyIndicator.replace("\t", "  "));
                    int additional = 0;
                    if (matcher.find()) {
                        lineBeginWithoutForBodyIndicator = lineBeginWithoutForBodyIndicator.replaceFirst(" ( )+\\\\",
                                "");
                        additional = 1;
                    }

                    if (hasVariablesToAssign(lineBeginWithoutForBodyIndicator,
                            numberOfCellSeparators - 1 - additional)) {
                        consumeWholeToken(scanner);
                        return token;
                    }

                }
                return Token.UNDEFINED;
            }

            private boolean isAssignment(final int ch, final String lineContentBefore) {
                return (ch == '=' || ch == ' ') && lineContentBefore.matches(".*\\} ?( ?=)* ?$");
            }
        };
    }

    private static void consumeWholeToken(final ICharacterScanner scanner) {
        int ch = scanner.read();
        while (ch != EOF && ch != '\t' && ch != '\n' && ch != '\r') {
            if (ch == ' ') {
                ch = scanner.read();
                scanner.unread();
                if (ch == ' ' || ch == '\t' || ch == '|') {
                    break;
                }
            }
            ch = scanner.read();
        }
        scanner.unread();
    }

    private static boolean hasVariablesToAssign(final String lineBegin, final int expectedNumberOfVariables) {
        String withoutTabs = lineBegin.replace("\t", "  ").replaceAll(" \\| ", "   ").replaceFirst("^\\| ", "  ");
        if (withoutTabs.startsWith("  ")) {
            withoutTabs = withoutTabs.trim();
            if (withoutTabs.isEmpty()) {
                return expectedNumberOfVariables == 0;
            }
            if (withoutTabs.charAt(0) != '@' && withoutTabs.charAt(0) != '$' && withoutTabs.charAt(0) != '&') {
                return expectedNumberOfVariables == 0;
            }

            final List<String> splitted = newArrayList(Splitter.on(Pattern.compile(" ( )+")).splitToList(withoutTabs));
            if (!lineBegin.endsWith("  ") && !lineBegin.endsWith("\t") && !lineBegin.endsWith(" | ")
                    && !lineBegin.endsWith("\t| ")) {
                splitted.remove(splitted.size() - 1);
            }
            if (splitted.size() == 0 && expectedNumberOfVariables == 0) {
                return true;
            }

            if (splitted.get(splitted.size() - 1).trim().endsWith("=")) {
                final String removed = splitted.remove(splitted.size() - 1);
                splitted.add(removed.trim().replaceAll("=", "").trim());
            }
            if (splitted.size() != expectedNumberOfVariables) {
                return false;
            }
            for (final String var : splitted) {
                if (!Pattern.matches("[$@&]\\{.+\\}", var)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static class CommentOnLineBeginRule implements IRule {

        private final IToken token;

        public CommentOnLineBeginRule(final IToken token) {
            this.token = token;
        }

        @Override
        public IToken evaluate(final ICharacterScanner scanner) {
            if (scanner.getColumn() > 1) {
                return Token.UNDEFINED;
            }

            final String commentStart = CharacterScannerUtilities.lookAhead(scanner, 1);
            if (!commentStart.isEmpty() && "#".equals(commentStart)) {
                while (true) {
                    final int ch = scanner.read();
                    if (ch == EOF || ch == '\r' || ch == '\n') {
                        scanner.unread();
                        return token;
                    }
                }
            } else {
                return Token.UNDEFINED;
            }
        }
    }
}
