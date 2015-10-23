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
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

import com.google.common.base.Splitter;

/**
 * @author Michal Anglart
 *
 */
public class Rules {

    private static int EOF = -1;

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

    static IRule createVariableRule(final IToken token) {
        return createCombinedRule(createScalarVariableRule(token), createListVariableRule(token),
                createDictionaryVariableRule(token), createEnvironmentVariableRule(token));
    }

    private static IRule createScalarVariableRule(final IToken token) {
        return new SingleLineRule("${", "}", token);
    }

    private static IRule createListVariableRule(final IToken token) {
        return new SingleLineRule("@{", "}", token);
    }

    private static IRule createDictionaryVariableRule(final IToken token) {
        return new SingleLineRule("&{", "}", token);
    }

    private static IRule createEnvironmentVariableRule(final IToken token) {
        return new SingleLineRule("%{", "}", token);
    }

    static IRule createCommentRule(final IToken token) {
        return createCombinedRule(new EndOfLineRule("  #", token), new EndOfLineRule("\t#", token));
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
        if (scanner.getColumn() > 1) {
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
            if (next == '\n' || next == '\r' || next == '#') {
                return header.toString();
            } else {
                return "";
            }
        } else {
            scanner.unread();
            return "";
        }
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
                if (!n.isEmpty() && Character.isAlphabetic(n.charAt(0))) {
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
                int ch = scanner.read();
                scanner.unread();
                if (ch == EOF || Character.isWhitespace(ch)) {
                    return Token.UNDEFINED;
                }
                final String lineContentBefore = CharacterScannerUtilities.lineContentBeforeCurrentPosition(scanner);

                if (DocumentUtilities.getNumberOfCellSeparators(lineContentBefore) == 1) {
                    ch = scanner.read();
                    if (ch != '[') {
                        scanner.unread();
                        return Token.UNDEFINED;
                    }
                    ch = scanner.read();
                    while (ch != EOF && !Character.isWhitespace(ch)) {
                        ch = scanner.read();
                    }
                    scanner.unread();

                    return token;
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
                final String lineContentBefore = CharacterScannerUtilities.lineContentBeforeCurrentPosition(scanner);

                final int numberOfCellSeparators = DocumentUtilities.getNumberOfCellSeparators(lineContentBefore);
                if (numberOfCellSeparators == 1 && isKeywordBasedSetting(lineContentBefore)) {
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
                final String lineContentBefore = CharacterScannerUtilities.lineContentBeforeCurrentPosition(scanner);
                if (isAssignment(ch, lineContentBefore)) {
                    return Token.UNDEFINED;
                }

                final int numberOfCellSeparators = DocumentUtilities.getNumberOfCellSeparators(lineContentBefore);
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
}
