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
    public void prefixesMatcherReturnsMatches_whenProposalContentStartsFromUserContent() {
        assertThat(ProposalMatchers.prefixesMatcher().matches("abc", "")).isNotPresent();
        assertThat(ProposalMatchers.prefixesMatcher().matches("abc", "d")).isNotPresent();
        assertThat(ProposalMatchers.prefixesMatcher().matches("abc", "de")).isNotPresent();
        assertThat(ProposalMatchers.prefixesMatcher().matches("abc", "def")).isNotPresent();

        assertThat(ProposalMatchers.prefixesMatcher().matches("", "abcdef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(ProposalMatchers.prefixesMatcher().matches("A", "abcdef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(ProposalMatchers.prefixesMatcher().matches("AB", "abcdef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 2)));
        assertThat(ProposalMatchers.prefixesMatcher().matches("AbC", "abcdef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 3)));

        assertThat(ProposalMatchers.prefixesMatcher().matches("", "AbcDef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(ProposalMatchers.prefixesMatcher().matches("a", "AbcDef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(ProposalMatchers.prefixesMatcher().matches("ab", "AbCDeF")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 2)));
        assertThat(ProposalMatchers.prefixesMatcher().matches("abc", "ABCDEF")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 3)));

        assertThat(ProposalMatchers.prefixesMatcher().matches("", "abcdef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(ProposalMatchers.prefixesMatcher().matches("a", "abcdef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(ProposalMatchers.prefixesMatcher().matches("ab", "abcdef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 2)));
        assertThat(ProposalMatchers.prefixesMatcher().matches("abc", "abcdef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 3)));
    }

    @Test
    public void caseSensitivePrefixesMatcherReturnsMatches_whenProposalContentStartsFromUserContentWithCaseTakenIntoAccount() {
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("abc", "")).isNotPresent();
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("abc", "d")).isNotPresent();
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("abc", "de")).isNotPresent();
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("abc", "def")).isNotPresent();

        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("A", "abcdef")).isNotPresent();
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("AB", "abcdef")).isNotPresent();
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("AbC", "abcdef")).isNotPresent();

        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("a", "AbcDef")).isNotPresent();
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("ab", "AbCDeF")).isNotPresent();
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("abc", "ABCDEF")).isNotPresent();

        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("", "abcdef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("a", "abcdef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("ab", "abcdef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 2)));
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("abc", "abcdef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 3)));
    }

    @Test
    public void embeddedNamesAwareMatcherReturnsMatches_whenDefinitionStartsTakingRegexIntoAccount() {
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("abc", "")).isNotPresent();
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("abc", "d")).isNotPresent();
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("abc", "de")).isNotPresent();
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("abc", "def")).isNotPresent();

        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("", "abcdef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("A", "abcdef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("AB", "abcdef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 2)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("AbC", "abcdef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 3)));

        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("", "AbcDef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("a", "AbcDef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("ab", "AbCDeF")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 2)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("abc", "ABCDEF")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 3)));

        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("", "abcdef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("a", "abcdef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("ab", "abcdef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 2)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("abc", "abcdef")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 3)));

        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("", "a${b}c")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("ax", "a${b}c")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 5)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("axy", "a${b}c")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 5)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("axyz", "a${b}c")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 5)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("axyzc", "a${b}c")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 6)));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("axyzcd", "a${b}c")).isPresent()
                .hasValue(new ProposalMatch(Range.closedOpen(0, 5)));
    }
}
