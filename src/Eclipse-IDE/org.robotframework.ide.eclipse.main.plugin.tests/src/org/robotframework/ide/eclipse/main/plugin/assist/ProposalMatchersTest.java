/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.Conditions.absent;
import static org.robotframework.red.junit.Conditions.containing;

import org.junit.Test;

import com.google.common.collect.Range;

public class ProposalMatchersTest {

    @Test
    public void prefixesMatcherReturnsMatches_whenProposalContentStartsFromUserContent() {
        assertThat(ProposalMatchers.prefixesMatcher().matches("abc", "")).is(absent());
        assertThat(ProposalMatchers.prefixesMatcher().matches("abc", "d")).is(absent());
        assertThat(ProposalMatchers.prefixesMatcher().matches("abc", "de")).is(absent());
        assertThat(ProposalMatchers.prefixesMatcher().matches("abc", "def")).is(absent());

        assertThat(ProposalMatchers.prefixesMatcher().matches("", "abcdef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 0))));
        assertThat(ProposalMatchers.prefixesMatcher().matches("A", "abcdef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 1))));
        assertThat(ProposalMatchers.prefixesMatcher().matches("AB", "abcdef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 2))));
        assertThat(ProposalMatchers.prefixesMatcher().matches("AbC", "abcdef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 3))));

        assertThat(ProposalMatchers.prefixesMatcher().matches("", "AbcDef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 0))));
        assertThat(ProposalMatchers.prefixesMatcher().matches("a", "AbcDef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 1))));
        assertThat(ProposalMatchers.prefixesMatcher().matches("ab", "AbCDeF"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 2))));
        assertThat(ProposalMatchers.prefixesMatcher().matches("abc", "ABCDEF"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 3))));

        assertThat(ProposalMatchers.prefixesMatcher().matches("", "abcdef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 0))));
        assertThat(ProposalMatchers.prefixesMatcher().matches("a", "abcdef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 1))));
        assertThat(ProposalMatchers.prefixesMatcher().matches("ab", "abcdef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 2))));
        assertThat(ProposalMatchers.prefixesMatcher().matches("abc", "abcdef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 3))));
    }

    @Test
    public void caseSensitivePrefixesMatcherReturnsMatches_whenProposalContentStartsFromUserContentWithCaseTakenIntoAccount() {
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("abc", "")).is(absent());
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("abc", "d")).is(absent());
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("abc", "de")).is(absent());
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("abc", "def")).is(absent());

        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("A", "abcdef"))
                .is(absent());
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("AB", "abcdef"))
                .is(absent());
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("AbC", "abcdef"))
                .is(absent());

        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("a", "AbcDef"))
                .is(absent());
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("ab", "AbCDeF"))
                .is(absent());
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("abc", "ABCDEF"))
                .is(absent());

        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("", "abcdef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 0))));
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("a", "abcdef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 1))));
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("ab", "abcdef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 2))));
        assertThat(ProposalMatchers.caseSensitivePrefixesMatcher().matches("abc", "abcdef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 3))));
    }

    @Test
    public void embeddedNamesAwareMatcherReturnsMatches_whenDefitionStartsTakingRegexIntoAccount() {
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("abc", "")).is(absent());
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("abc", "d")).is(absent());
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("abc", "de")).is(absent());
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("abc", "def")).is(absent());

        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("", "abcdef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 0))));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("A", "abcdef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 1))));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("AB", "abcdef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 2))));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("AbC", "abcdef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 3))));

        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("", "AbcDef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 0))));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("a", "AbcDef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 1))));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("ab", "AbCDeF"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 2))));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("abc", "ABCDEF"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 3))));

        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("", "abcdef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 0))));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("a", "abcdef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 1))));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("ab", "abcdef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 2))));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("abc", "abcdef"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 3))));
        
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("", "a${b}c"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 0))));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("ax", "a${b}c"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 5))));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("axy","a${b}c"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 5))));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("axyz", "a${b}c"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 5))));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("axyzc", "a${b}c"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 6))));
        assertThat(ProposalMatchers.embeddedKeywordsMatcher().matches("axyzcd", "a${b}c"))
                .is(containing(new ProposalMatch(Range.closedOpen(0, 5))));
    }
}
