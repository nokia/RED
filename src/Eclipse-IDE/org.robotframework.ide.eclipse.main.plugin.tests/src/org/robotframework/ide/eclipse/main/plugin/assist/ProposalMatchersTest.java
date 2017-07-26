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
    public void embeddedNamesAwareMatcherReturnsMatches_whenDefinitionContainsUserContentTakingRegexIntoAccount() {
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("abc", "")).isNotPresent();
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("abc", "d")).isNotPresent();
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("abc", "de")).isNotPresent();
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("abc", "def")).isNotPresent();

        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("A", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("BC", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("CdE", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(2, 5)));

        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("", "AbcDef"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("a", "AbcDef"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("bc", "AbCDeF"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("cde", "ABCDEF"))
                .hasValue(new ProposalMatch(Range.closedOpen(2, 5)));

        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("a", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("bc", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("cde", "abcdef"))
                .hasValue(new ProposalMatch(Range.closedOpen(2, 5)));

        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("a", "AbCabcABC"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("bc", "AbCabcABC"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("abc", "AbCabcABC"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 3)));

        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("", "ab${var}c"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("bx", "ab${var}c"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 8)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("bxy", "ab${var}c"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 8)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("bxyz", "ab${var}c"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 8)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("bxyzc", "ab${var}c"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 9)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("bxyzcd", "ab${var}c"))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 8)));
    }
}
