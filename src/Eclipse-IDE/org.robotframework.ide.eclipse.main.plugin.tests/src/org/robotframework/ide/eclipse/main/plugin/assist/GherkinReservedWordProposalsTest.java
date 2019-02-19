/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.prefixesMatcher;

import java.util.Comparator;
import java.util.List;

import org.junit.Test;

public class GherkinReservedWordProposalsTest {

    @Test
    public void noProposalsAreProvided_whenPredicateIsAlwaysFalse() {
        final AssistProposalPredicate<String> predicateWordHasToSatisfy = AssistProposalPredicates.alwaysFalse();
        final GherkinReservedWordProposals proposalsProvider = new GherkinReservedWordProposals(
                predicateWordHasToSatisfy);

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("");
        assertThat(proposals).isEmpty();
    }

    @Test
    public void allProposalsAreProvided_whenPredicateIsAlwaysTrue() {
        final AssistProposalPredicate<String> predicateWordHasToSatisfy = AssistProposalPredicates.alwaysTrue();
        final GherkinReservedWordProposals proposalsProvider = new GherkinReservedWordProposals(
                predicateWordHasToSatisfy);

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("");
        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly("And", "But", "Given", "Then", "When");
    }

    @Test
    public void onlyProposalsMatchingPredicateAreProvided_whenPredicateSelectsThem() {
        final AssistProposalPredicate<String> predicateWordHasToSatisfy = word -> word.length() < 4;
        final GherkinReservedWordProposals proposalsProvider = new GherkinReservedWordProposals(
                predicateWordHasToSatisfy);

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("");
        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("And", "But");
    }

    @Test
    public void onlyProposalsContainingInputAreProvided_whenDefaultMatcherIsUsed() {
        final GherkinReservedWordProposals proposalsProvider = new GherkinReservedWordProposals(
                AssistProposalPredicates.alwaysTrue());

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("En");
        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly("Given", "Then", "When");
    }

    @Test
    public void onlyProposalsMatchingGivenMatcherAreProvided_whenMatcherIsGiven() {
        final GherkinReservedWordProposals proposalsProvider = new GherkinReservedWordProposals(prefixesMatcher(),
                AssistProposalPredicates.alwaysTrue());

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("w");
        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("When");
    }

    @Test
    public void proposalsAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided() {
        final GherkinReservedWordProposals proposalsProvider = new GherkinReservedWordProposals(
                AssistProposalPredicates.alwaysTrue());

        final Comparator<AssistProposal> comparator = AssistProposals.sortedByLabels().reversed();
        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("", comparator);
        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly("When", "Then", "Given", "But", "And");
    }
}
