/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.test.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;


/**
 *
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 *
 * @see CombinationGenerator#convertTo(java.util.List)
 * @see CombinationGenerator#combinations(String)
 */
public class CombinationGeneratorTest {

    private final CombinationGenerator combiner = new CombinationGenerator();


    @Test
    public void test_combinations_checkForWhitespaces_shouldReturn_listEqualsToInput() {
        // prepare
        final String whitespaces = " \t\r\n\f";

        // execute
        final List<String> combinations = combiner.combinations(whitespaces);

        // verify
        assertThat(combinations).isNotNull();
        assertThat(combinations).hasSize(1);
        assertThat(combinations).contains(whitespaces, atIndex(0));
    }


    @Test
    public void test_combinations_checkForSingleNumber1_shouldReturn_listWith_1() {
        // execute
        final List<String> combinations = combiner.combinations("1");

        // verify
        assertThat(combinations).isNotNull();
        assertThat(combinations).hasSize(1);
        assertThat(combinations).contains("1", atIndex(0));
    }


    @Test
    public void test_combinations_checkForLettersAndNumber1ABC1D_shouldReturn_listWith_sixteenCombinations() {
        assertThatCombinationListIsCorrect(Arrays.asList("1ABC1D", "1ABC1d",
                "1ABc1D", "1ABc1d", "1AbC1D", "1AbC1d", "1Abc1D", "1Abc1d",
                "1aBC1D", "1aBC1d", "1aBc1D", "1aBc1d", "1abC1D", "1abC1d",
                "1abc1D", "1abc1d"), combiner.combinations("1abc1d"));
    }


    @Test
    public void test_combinations_checkForLettersAndNumberABC1D_shouldReturn_listWith_sixteenCombinations() {
        assertThatCombinationListIsCorrect(Arrays.asList("aBC1D", "aBC1d",
                "aBc1D", "aBc1d", "abC1D", "abC1d", "abc1D", "abc1d", "ABC1D",
                "ABC1d", "ABc1D", "ABc1d", "AbC1D", "AbC1d", "Abc1D", "Abc1d"),
                combiner.combinations("abc1d"));
    }


    @Test
    public void test_combinations_checkForLettersABC_shouldReturn_listWith_eightCombinations() {
        assertThatCombinationListIsCorrect(Arrays.asList("aBC", "aBc", "abC",
                "abc", "ABC", "ABc", "AbC", "Abc"),
                combiner.combinations("abc"));
    }


    @Test
    public void test_combinations_checkForLettersAB_shouldReturn_listWith_fourCombinations() {
        assertThatCombinationListIsCorrect(
                Arrays.asList("aB", "ab", "AB", "Ab"),
                combiner.combinations("ab"));
    }


    private void assertThatCombinationListIsCorrect(
            final List<String> combinationsExpected, final List<String> combinationsGet) {
        assertThat(combinationsExpected).isNotNull();
        assertThat(combinationsGet).isNotNull();
        assertThat(combinationsGet).hasSize(combinationsExpected.size());
        assertThat(combinationsExpected).containsOnlyElementsOf(combinationsGet);
    }


    @Test
    public void test_combinations_checkForSingleLetterA_shouldReturn_listWith_aA() {
        // execute
        final List<String> combinations = combiner.combinations("a");

        // verify
        assertThat(combinations).isNotNull();
        assertThat(combinations).hasSize(2);
        assertThat(combinations).contains("a", atIndex(0)).contains("A",
                atIndex(1));
    }


    @Test
    public void test_combinations_checkForEmptyString_shouldReturn_emptyList() {
        // execute
        final List<String> combinations = combiner.combinations("");

        // verify
        assertThat(combinations).isNotNull();
        assertThat(combinations).isEmpty();
    }


    @Test
    public void test_combinations_checkForNULL_shouldReturn_emptyList() {
        // execute
        final List<String> combinations = combiner.combinations(null);

        // verify
        assertThat(combinations).isNotNull();
        assertThat(combinations).isEmpty();
    }
}
