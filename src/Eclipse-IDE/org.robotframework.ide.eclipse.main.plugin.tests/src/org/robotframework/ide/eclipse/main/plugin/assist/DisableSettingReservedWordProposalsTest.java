/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.prefixesMatcher;

import java.util.List;

import org.junit.jupiter.api.Test;

public class DisableSettingReservedWordProposalsTest {

    @Test
    public void noProposalsAreProvided_whenPredicateIsAlwaysFalse() {
        final AssistProposalPredicate<String> predicateWordHasToSatisfy = AssistProposalPredicates.alwaysFalse();
        final DisableSettingReservedWordProposals proposalsProvider = new DisableSettingReservedWordProposals(
                predicateWordHasToSatisfy);

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("");
        assertThat(proposals).isEmpty();
    }

    @Test
    public void allProposalsAreProvided_whenPredicateIsAlwaysTrue() {
        final AssistProposalPredicate<String> predicateWordHasToSatisfy = AssistProposalPredicates.alwaysTrue();
        final DisableSettingReservedWordProposals proposalsProvider = new DisableSettingReservedWordProposals(
                predicateWordHasToSatisfy);

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("");
        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("NONE");
    }

    @Test
    public void onlyProposalsMatchingPredicateAreProvided_whenPredicateSelectsThem() {
        final AssistProposalPredicate<String> predicateWordHasToSatisfy = word -> word.length() < 3;
        final DisableSettingReservedWordProposals proposalsProvider = new DisableSettingReservedWordProposals(
                predicateWordHasToSatisfy);

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("");
        assertThat(proposals).isEmpty();
    }

    @Test
    public void onlyProposalsMatchingPredicateAreProvided_whenPredicateCannotSelectThem() {
        final AssistProposalPredicate<String> predicateWordHasToSatisfy = word -> word.length() > 3;
        final DisableSettingReservedWordProposals proposalsProvider = new DisableSettingReservedWordProposals(
                predicateWordHasToSatisfy);

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("");
        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("NONE");
    }

    @Test
    public void onlyProposalsContainingInputAreProvided_whenDefaultMatcherIsUsed() {
        final DisableSettingReservedWordProposals proposalsProvider = new DisableSettingReservedWordProposals(
                AssistProposalPredicates.alwaysTrue());

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("on");
        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("NONE");
    }

    @Test
    public void onlyProposalsMatchingGivenMatcherAreProvided_whenMatcherIsGiven() {
        final DisableSettingReservedWordProposals proposalsProvider = new DisableSettingReservedWordProposals(
                prefixesMatcher(), AssistProposalPredicates.alwaysTrue());

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("no");
        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("NONE");
    }
}
