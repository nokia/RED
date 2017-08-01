/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.google.common.collect.Range;

public class ProposalMatchersTest {

    @Test
    public void substringMatcherReturnsMatches_whenDefinitionContainsUserContent() {
        assertThat(ProposalMatchers.substringMatcher().matches("abc", "")).isNotPresent();
        assertThat(ProposalMatchers.substringMatcher().matches("abc", "d")).isNotPresent();
        assertThat(ProposalMatchers.substringMatcher().matches("abc", "de")).isNotPresent();
        assertThat(ProposalMatchers.substringMatcher().matches("abc", "def")).isNotPresent();

        assertThat(ProposalMatchers.substringMatcher().matches("", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(ProposalMatchers.substringMatcher().matches("A", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(ProposalMatchers.substringMatcher().matches("BC", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(ProposalMatchers.substringMatcher().matches("CdE", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(2, 5)));

        assertThat(ProposalMatchers.substringMatcher().matches("", "AbcDef"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(ProposalMatchers.substringMatcher().matches("a", "AbcDef"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(ProposalMatchers.substringMatcher().matches("bc", "AbCDeF"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(ProposalMatchers.substringMatcher().matches("cde", "ABCDEF"))
                .hasValue(new ProposalMatch(Range.closedOpen(2, 5)));

        assertThat(ProposalMatchers.substringMatcher().matches("", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(ProposalMatchers.substringMatcher().matches("a", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(ProposalMatchers.substringMatcher().matches("bc", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(ProposalMatchers.substringMatcher().matches("cde", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(2, 5)));

        assertThat(ProposalMatchers.substringMatcher().matches("a", "AbCabcABC"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(ProposalMatchers.substringMatcher().matches("bc", "AbCabcABC"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(ProposalMatchers.substringMatcher().matches("abc", "AbCabcABC"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 3)));
    }

    @Test
    public void keywordsMatcherReturnsMatches_whenDefinitionMatchesCamelCaseUserContentOrContainsUserContentTakingRegexIntoAccount() {
        assertThat(ProposalMatchers.keywordsMatcher().matches("abc", "")).isNotPresent();
        assertThat(ProposalMatchers.keywordsMatcher().matches("abc", "d")).isNotPresent();
        assertThat(ProposalMatchers.keywordsMatcher().matches("abc", "de")).isNotPresent();
        assertThat(ProposalMatchers.keywordsMatcher().matches("abc", "def")).isNotPresent();
        assertThat(ProposalMatchers.keywordsMatcher().matches("AB", "Cd Ef")).isNotPresent();
        assertThat(ProposalMatchers.keywordsMatcher().matches("Ac", "Ab Cd")).isNotPresent();

        assertThat(ProposalMatchers.keywordsMatcher().matches("A", "Abcd"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(ProposalMatchers.keywordsMatcher().matches("AbE", "Abcd Efg"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 2), Range.closedOpen(5, 6)));
        assertThat(ProposalMatchers.keywordsMatcher().matches("AbcEfH", "Abcd Efg Hi"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 3), Range.closedOpen(5, 7), Range.closedOpen(9, 10)));

        assertThat(ProposalMatchers.keywordsMatcher().matches("", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(ProposalMatchers.keywordsMatcher().matches("A", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(ProposalMatchers.keywordsMatcher().matches("BC", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(ProposalMatchers.keywordsMatcher().matches("CdE", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(2, 5)));

        assertThat(ProposalMatchers.keywordsMatcher().matches("", "AbcDef"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(ProposalMatchers.keywordsMatcher().matches("a", "AbcDef"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(ProposalMatchers.keywordsMatcher().matches("bc", "AbCDeF"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(ProposalMatchers.keywordsMatcher().matches("cde", "ABCDEF"))
                .hasValue(new ProposalMatch(Range.closedOpen(2, 5)));

        assertThat(ProposalMatchers.keywordsMatcher().matches("", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(ProposalMatchers.keywordsMatcher().matches("a", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(ProposalMatchers.keywordsMatcher().matches("bc", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(ProposalMatchers.keywordsMatcher().matches("cde", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(2, 5)));

        assertThat(ProposalMatchers.keywordsMatcher().matches("a", "AbCabcABC"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(ProposalMatchers.keywordsMatcher().matches("bc", "AbCabcABC"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(ProposalMatchers.keywordsMatcher().matches("abc", "AbCabcABC"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 3)));

        assertThat(ProposalMatchers.keywordsMatcher().matches("", "ab${var}c"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(ProposalMatchers.keywordsMatcher().matches("bx", "ab${var}c"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 8)));
        assertThat(ProposalMatchers.keywordsMatcher().matches("bxy", "ab${var}c"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 8)));
        assertThat(ProposalMatchers.keywordsMatcher().matches("bxyz", "ab${var}c"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 8)));
        assertThat(ProposalMatchers.keywordsMatcher().matches("bxyzc", "ab${var}c"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 9)));
        assertThat(ProposalMatchers.keywordsMatcher().matches("bxyzcd", "ab${var}c"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 8)));
    }
}
