/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.prefixesMatcher;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.reverseComparator;

import java.util.Comparator;
import java.util.List;

import org.junit.Test;

public class ForLoopReservedWordsProposalsTest {

    @Test
    public void noProposalsAreProvided_whenPredicateIsAlwaysFalse() {
        final AssistProposalPredicate<String> predicateWordHasToSatisfy = AssistProposalPredicates.alwaysFalse();
        final ForLoopReservedWordsProposals proposalsProvider = new ForLoopReservedWordsProposals(
                predicateWordHasToSatisfy);

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("");
        assertThat(proposals).isEmpty();
    }

    @Test
    public void allProposalsAreProvided_whenPredicateIsAlwaysTrue() {
        final AssistProposalPredicate<String> predicateWordHasToSatisfy = AssistProposalPredicates.alwaysTrue();
        final ForLoopReservedWordsProposals proposalsProvider = new ForLoopReservedWordsProposals(
                predicateWordHasToSatisfy);

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("");
        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly(":FOR", "END", "FOR", "IN", "IN ENUMERATE", "IN RANGE", "IN ZIP");
    }

    @Test
    public void onlyProposalsMatchingPredicateAreProvided_whenPredicateSelectsThem() {
        final AssistProposalPredicate<String> predicateWordHasToSatisfy = word -> word.length() < 4;
        final ForLoopReservedWordsProposals proposalsProvider = new ForLoopReservedWordsProposals(
                predicateWordHasToSatisfy);

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("");
        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("END", "FOR", "IN");
    }

    @Test
    public void onlyProposalsContainingInputAreProvided_whenDefaultMatcherIsUsed() {
        final ForLoopReservedWordsProposals proposalsProvider = new ForLoopReservedWordsProposals(
                AssistProposalPredicates.alwaysTrue());

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("En");
        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("END", "IN ENUMERATE");
    }

    @Test
    public void onlyProposalsMatchingGivenMatcherAreProvided_whenMatcherIsGiven() {
        final ForLoopReservedWordsProposals proposalsProvider = new ForLoopReservedWordsProposals(prefixesMatcher(),
                AssistProposalPredicates.alwaysTrue());

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("iN");
        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly("IN", "IN ENUMERATE", "IN RANGE", "IN ZIP");
    }

    @Test
    public void proposalsAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided() {
        final ForLoopReservedWordsProposals proposalsProvider = new ForLoopReservedWordsProposals(
                AssistProposalPredicates.alwaysTrue());

        final Comparator<AssistProposal> comparator = reverseComparator(AssistProposals.sortedByLabels());
        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("", comparator);
        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly("IN ZIP", "IN RANGE", "IN ENUMERATE", "IN", "FOR", "END", ":FOR");
    }
}
