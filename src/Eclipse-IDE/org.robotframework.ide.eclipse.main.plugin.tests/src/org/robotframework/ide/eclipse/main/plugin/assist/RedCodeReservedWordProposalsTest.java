/*
 * Copyright 2016 Nokia Solutions and Networks
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
import org.rf.ide.core.environment.RobotVersion;

public class RedCodeReservedWordProposalsTest {

    @Test
    public void noProposalsAreProvided_whenPredicateIsAlwaysFalse() {
        final AssistProposalPredicate<String> predicateWordHasToSatisfy = AssistProposalPredicates.alwaysFalse();
        final RedCodeReservedWordProposals proposalsProvider = new RedCodeReservedWordProposals(new RobotVersion(3, 0),
                predicateWordHasToSatisfy);

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("");
        assertThat(proposals).isEmpty();
    }

    @Test
    public void allProposalsAreProvided_whenPredicateIsAlwaysTrue() {
        final AssistProposalPredicate<String> predicateWordHasToSatisfy = AssistProposalPredicates.alwaysTrue();
        final RedCodeReservedWordProposals proposalsProvider = new RedCodeReservedWordProposals(new RobotVersion(3, 0),
                predicateWordHasToSatisfy);

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("");
        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly(":FOR", "And", "But", "Given", "IN", "IN ENUMERATE", "IN RANGE", "IN ZIP", "Then",
                        "When");
    }

    @Test
    public void onlyProposalsMatchingPredicateAreProvided_whenPredicateSelectsThem() {
        final AssistProposalPredicate<String> predicateWordHasToSatisfy = word -> word.length() < 4;
        final RedCodeReservedWordProposals proposalsProvider = new RedCodeReservedWordProposals(new RobotVersion(3, 0),
                predicateWordHasToSatisfy);

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("");
        assertThat(proposals).extracting(AssistProposal::getLabel).containsExactly("And", "But", "IN");
    }

    @Test
    public void onlyProposalsContainingInputAreProvided_whenDefaultMatcherIsUsed() {
        final RedCodeReservedWordProposals proposalsProvider = new RedCodeReservedWordProposals(new RobotVersion(3, 0),
                AssistProposalPredicates.alwaysTrue());

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("En");
        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly("Given", "IN ENUMERATE", "Then", "When");
    }

    @Test
    public void onlyProposalsMatchingGivenMatcherAreProvided_whenMatcherIsGiven() {
        final RedCodeReservedWordProposals proposalsProvider = new RedCodeReservedWordProposals(prefixesMatcher(),
                new RobotVersion(3, 0), AssistProposalPredicates.alwaysTrue());

        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("iN");
        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly("IN", "IN ENUMERATE", "IN RANGE", "IN ZIP");
    }

    @Test
    public void proposalsAreProvidedInOrderInducedByGivenComparator_whenCustomComparatorIsProvided() {
        final RedCodeReservedWordProposals proposalsProvider = new RedCodeReservedWordProposals(new RobotVersion(3, 0),
                AssistProposalPredicates.alwaysTrue());

        final Comparator<AssistProposal> comparator = reverseComparator(AssistProposals.sortedByLabels());
        final List<? extends AssistProposal> proposals = proposalsProvider.getReservedWordProposals("", comparator);
        assertThat(proposals).extracting(AssistProposal::getLabel)
                .containsExactly("When", "Then", "IN ZIP", "IN RANGE", "IN ENUMERATE", "IN", "Given", "But", "And",
                        ":FOR");
    }
}
