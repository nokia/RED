/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.robotframework.ide.eclipse.main.plugin.assist.AssistProposals.sortedByLabels;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

public class GherkinReservedWordProposals {

    public static final List<String> GHERKIN_ELEMENTS = ImmutableList.of("Given", "When", "And", "But", "Then");

    private final ProposalMatcher matcher;

    private final AssistProposalPredicate<String> predicateWordHasToSatisfy;

    public GherkinReservedWordProposals(final AssistProposalPredicate<String> predicateWordHasToSatisfy) {
        this(ProposalMatchers.substringMatcher(), predicateWordHasToSatisfy);
    }

    @VisibleForTesting
    GherkinReservedWordProposals(final ProposalMatcher matcher,
            final AssistProposalPredicate<String> predicateWordHasToSatisfy) {
        this.matcher = matcher;
        this.predicateWordHasToSatisfy = predicateWordHasToSatisfy;
    }

    public List<? extends AssistProposal> getReservedWordProposals(final String userContent) {
        return getReservedWordProposals(userContent, sortedByLabels());
    }

    public List<? extends AssistProposal> getReservedWordProposals(final String userContent,
            final Comparator<AssistProposal> comparator) {

        final List<GherkinReservedWordProposal> proposals = new ArrayList<>();
        proposals.addAll(generateProposalsFrom(GHERKIN_ELEMENTS, userContent));

        proposals.sort(comparator);
        return proposals;
    }

    private List<GherkinReservedWordProposal> generateProposalsFrom(final Iterable<String> words,
            final String userContent) {

        final List<GherkinReservedWordProposal> proposals = new ArrayList<>();
        for (final String word : words) {
            if (predicateWordHasToSatisfy.test(word)) {
                final Optional<ProposalMatch> match = matcher.matches(userContent, word);
                if (match.isPresent()) {
                    proposals.add(AssistProposals.createGherkinReservedWordProposal(word, match.get()));
                }
            }
        }
        return proposals;
    }
}
