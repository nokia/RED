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

    private static final ProposalMatcher SUBSTRING_MATCHER = ProposalMatchers.substringMatcher();

    private static final ProposalMatcher KEYWORDS_MATCHER = ProposalMatchers.keywordsMatcher();

    private static final ProposalMatcher VARIABLES_MATCHER = ProposalMatchers.variablesMatcher();

    @Test
    public void substringMatcherReturnsEmptyMatches_whenDefinitionDoesNotContainUserContent() {
        assertThat(SUBSTRING_MATCHER.matches("abc", "")).isNotPresent();
        assertThat(SUBSTRING_MATCHER.matches("abc", "d")).isNotPresent();
        assertThat(SUBSTRING_MATCHER.matches("abc", "de")).isNotPresent();
        assertThat(SUBSTRING_MATCHER.matches("abc", "def")).isNotPresent();
    }

    @Test
    public void substringMatcherReturnsMatches_whenDefinitionContainsUserContent() {
        assertThat(SUBSTRING_MATCHER.matches("", "abcdef")).hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(SUBSTRING_MATCHER.matches("A", "abcdef")).hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(SUBSTRING_MATCHER.matches("BC", "abcdef")).hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(SUBSTRING_MATCHER.matches("CdE", "abcdef")).hasValue(new ProposalMatch(Range.closedOpen(2, 5)));

        assertThat(SUBSTRING_MATCHER.matches("", "AbcDef")).hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(SUBSTRING_MATCHER.matches("a", "AbcDef")).hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(SUBSTRING_MATCHER.matches("bc", "AbCDeF")).hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(SUBSTRING_MATCHER.matches("cde", "ABCDEF")).hasValue(new ProposalMatch(Range.closedOpen(2, 5)));

        assertThat(SUBSTRING_MATCHER.matches("", "abcdef")).hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(SUBSTRING_MATCHER.matches("a", "abcdef")).hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(SUBSTRING_MATCHER.matches("bc", "abcdef")).hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(SUBSTRING_MATCHER.matches("cde", "abcdef")).hasValue(new ProposalMatch(Range.closedOpen(2, 5)));

        assertThat(SUBSTRING_MATCHER.matches("a", "AbCabcABC")).hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(SUBSTRING_MATCHER.matches("bc", "AbCabcABC")).hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(SUBSTRING_MATCHER.matches("abc", "AbCabcABC")).hasValue(new ProposalMatch(Range.closedOpen(0, 3)));
    }

    @Test
    public void keywordsMatcherReturnsEmptyMatches_whenDefinitionDoesNotMatchCamelCaseUserContentAndDoesNotContainUserContentTakingRegexIntoAccount() {
        assertThat(KEYWORDS_MATCHER.matches("abc", "")).isNotPresent();
        assertThat(KEYWORDS_MATCHER.matches("abc", "d")).isNotPresent();
        assertThat(KEYWORDS_MATCHER.matches("abc", "de")).isNotPresent();
        assertThat(KEYWORDS_MATCHER.matches("abc", "def")).isNotPresent();
        assertThat(KEYWORDS_MATCHER.matches("AB", "Cd Ef")).isNotPresent();
        assertThat(KEYWORDS_MATCHER.matches("Ac", "Ab Cd")).isNotPresent();
    }

    @Test
    public void keywordsMatcherReturnsMatches_whenDefinitionMatchesCamelCaseUserContentOrContainsUserContentTakingRegexIntoAccount() {
        assertThat(KEYWORDS_MATCHER.matches("A", "Abcd")).hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(KEYWORDS_MATCHER.matches("AbE", "Abcd Efg"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 2), Range.closedOpen(5, 6)));
        assertThat(KEYWORDS_MATCHER.matches("AbcEfH", "Abcd Efg Hi"))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 3), Range.closedOpen(5, 7), Range.closedOpen(9, 10)));

        assertThat(KEYWORDS_MATCHER.matches("", "abcdef")).hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(KEYWORDS_MATCHER.matches("A", "abcdef")).hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(KEYWORDS_MATCHER.matches("BC", "abcdef")).hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(KEYWORDS_MATCHER.matches("CdE", "abcdef")).hasValue(new ProposalMatch(Range.closedOpen(2, 5)));

        assertThat(KEYWORDS_MATCHER.matches("", "AbcDef")).hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(KEYWORDS_MATCHER.matches("a", "AbcDef")).hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(KEYWORDS_MATCHER.matches("bc", "AbCDeF")).hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(KEYWORDS_MATCHER.matches("cde", "ABCDEF")).hasValue(new ProposalMatch(Range.closedOpen(2, 5)));

        assertThat(KEYWORDS_MATCHER.matches("", "abcdef")).hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(KEYWORDS_MATCHER.matches("a", "abcdef")).hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(KEYWORDS_MATCHER.matches("bc", "abcdef")).hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(KEYWORDS_MATCHER.matches("cde", "abcdef")).hasValue(new ProposalMatch(Range.closedOpen(2, 5)));

        assertThat(KEYWORDS_MATCHER.matches("a", "AbCabcABC")).hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(KEYWORDS_MATCHER.matches("bc", "AbCabcABC")).hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(KEYWORDS_MATCHER.matches("abc", "AbCabcABC")).hasValue(new ProposalMatch(Range.closedOpen(0, 3)));

        assertThat(KEYWORDS_MATCHER.matches("", "ab${var}c")).hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(KEYWORDS_MATCHER.matches("bx", "ab${var}c")).hasValue(new ProposalMatch(Range.closedOpen(1, 8)));
        assertThat(KEYWORDS_MATCHER.matches("bxy", "ab${var}c")).hasValue(new ProposalMatch(Range.closedOpen(1, 8)));
        assertThat(KEYWORDS_MATCHER.matches("bxyz", "ab${var}c")).hasValue(new ProposalMatch(Range.closedOpen(1, 8)));
        assertThat(KEYWORDS_MATCHER.matches("bxyzc", "ab${var}c")).hasValue(new ProposalMatch(Range.closedOpen(1, 9)));
        assertThat(KEYWORDS_MATCHER.matches("bxyzcd", "ab${var}c")).hasValue(new ProposalMatch(Range.closedOpen(1, 8)));
    }

    @Test
    public void variablesMatcherReturnsEmptyMatches_whenDefinitionDoesNotContainUserContentOrVariableIdentificatorsDoNotMatch() {
        assertThat(VARIABLES_MATCHER.matches("${abc", "")).isNotPresent();
        assertThat(VARIABLES_MATCHER.matches("${abc", "${d}")).isNotPresent();
        assertThat(VARIABLES_MATCHER.matches("${abc", "${de}")).isNotPresent();
        assertThat(VARIABLES_MATCHER.matches("${abc", "${def}")).isNotPresent();

        assertThat(VARIABLES_MATCHER.matches("$", "@{abc}")).isNotPresent();
        assertThat(VARIABLES_MATCHER.matches("${", "@{abc}")).isNotPresent();
        assertThat(VARIABLES_MATCHER.matches("${abc", "@{abc}")).isNotPresent();
    }

    @Test
    public void variablesMatcherReturnsMatches_whenDefinitionContainsUserContentAndVariableIdentificatorsMatch() {
        assertThat(VARIABLES_MATCHER.matches("${A", "${abcde}")).hasValue(new ProposalMatch(Range.closedOpen(2, 3)));
        assertThat(VARIABLES_MATCHER.matches("${BC", "${abcdef}")).hasValue(new ProposalMatch(Range.closedOpen(3, 5)));
        assertThat(VARIABLES_MATCHER.matches("${CdE", "${abcdef}")).hasValue(new ProposalMatch(Range.closedOpen(4, 7)));

        assertThat(VARIABLES_MATCHER.matches("${a", "${AbcDef}")).hasValue(new ProposalMatch(Range.closedOpen(2, 3)));
        assertThat(VARIABLES_MATCHER.matches("${bc", "${AbCDeF}")).hasValue(new ProposalMatch(Range.closedOpen(3, 5)));
        assertThat(VARIABLES_MATCHER.matches("${cde", "${ABCDEF}")).hasValue(new ProposalMatch(Range.closedOpen(4, 7)));

        assertThat(VARIABLES_MATCHER.matches("${a", "${abcdef}")).hasValue(new ProposalMatch(Range.closedOpen(2, 3)));
        assertThat(VARIABLES_MATCHER.matches("${bc", "${abcdef}")).hasValue(new ProposalMatch(Range.closedOpen(3, 5)));
        assertThat(VARIABLES_MATCHER.matches("${cde", "${abcdef}")).hasValue(new ProposalMatch(Range.closedOpen(4, 7)));

        assertThat(VARIABLES_MATCHER.matches("${a", "${AbCabcABC}"))
                .hasValue(new ProposalMatch(Range.closedOpen(2, 3)));
        assertThat(VARIABLES_MATCHER.matches("${bc", "${AbCabcABC}"))
                .hasValue(new ProposalMatch(Range.closedOpen(3, 5)));
        assertThat(VARIABLES_MATCHER.matches("${abc", "${AbCabcABC}"))
                .hasValue(new ProposalMatch(Range.closedOpen(2, 5)));
    }

    @Test
    public void variablesMatcherReturnsMatches_whenDefinitionContainsUserContentAndUserContentDoesNotStartWithVariableIdentificator() {
        assertThat(VARIABLES_MATCHER.matches("", "${abcdef}")).hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(VARIABLES_MATCHER.matches("A", "${abcde}")).hasValue(new ProposalMatch(Range.closedOpen(2, 3)));
        assertThat(VARIABLES_MATCHER.matches("BC", "${abcdef}")).hasValue(new ProposalMatch(Range.closedOpen(3, 5)));
        assertThat(VARIABLES_MATCHER.matches("CdE", "${abcdef}")).hasValue(new ProposalMatch(Range.closedOpen(4, 7)));

        assertThat(VARIABLES_MATCHER.matches("", "${AbcDef}")).hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(VARIABLES_MATCHER.matches("a", "${AbcDef}")).hasValue(new ProposalMatch(Range.closedOpen(2, 3)));
        assertThat(VARIABLES_MATCHER.matches("bc", "${AbCDeF}")).hasValue(new ProposalMatch(Range.closedOpen(3, 5)));
        assertThat(VARIABLES_MATCHER.matches("cde", "${ABCDEF}")).hasValue(new ProposalMatch(Range.closedOpen(4, 7)));

        assertThat(VARIABLES_MATCHER.matches("", "${abcdef}")).hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(VARIABLES_MATCHER.matches("a", "${abcdef}")).hasValue(new ProposalMatch(Range.closedOpen(2, 3)));
        assertThat(VARIABLES_MATCHER.matches("bc", "${abcdef}")).hasValue(new ProposalMatch(Range.closedOpen(3, 5)));
        assertThat(VARIABLES_MATCHER.matches("cde", "${abcdef}")).hasValue(new ProposalMatch(Range.closedOpen(4, 7)));

        assertThat(VARIABLES_MATCHER.matches("a", "${AbCabcABC}")).hasValue(new ProposalMatch(Range.closedOpen(2, 3)));
        assertThat(VARIABLES_MATCHER.matches("bc", "${AbCabcABC}")).hasValue(new ProposalMatch(Range.closedOpen(3, 5)));
        assertThat(VARIABLES_MATCHER.matches("abc", "${AbCabcABC}"))
                .hasValue(new ProposalMatch(Range.closedOpen(2, 5)));
    }
}
