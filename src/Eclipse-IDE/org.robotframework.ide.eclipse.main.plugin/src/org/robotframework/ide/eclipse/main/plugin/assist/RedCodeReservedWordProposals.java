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
import java.util.stream.Stream;

import org.rf.ide.core.environment.RobotVersion;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

public class RedCodeReservedWordProposals {

    static final String FOR_LOOP_1 = ":FOR";

    static final String FOR_LOOP_2 = ": FOR";

    public static final List<String> GHERKIN_ELEMENTS = ImmutableList.of("Given", "When", "And", "But", "Then");

    static final List<String> NEW_FOR_LOOP_LITERALS = ImmutableList.of("FOR", "END");

    private static final List<String> LOOP_ELEMENTS = ImmutableList.of("IN", "IN ENUMERATE", "IN RANGE", "IN ZIP");

    private final ProposalMatcher matcher;

    private final RobotVersion version;

    private final AssistProposalPredicate<String> predicateWordHasToSatisfy;

    public RedCodeReservedWordProposals(final RobotVersion version,
            final AssistProposalPredicate<String> predicateWordHasToSatisfy) {
        this(ProposalMatchers.substringMatcher(), version, predicateWordHasToSatisfy);
    }

    @VisibleForTesting
    RedCodeReservedWordProposals(final ProposalMatcher matcher,
            final RobotVersion version, final AssistProposalPredicate<String> predicateWordHasToSatisfy) {
        this.matcher = matcher;
        this.version = version;
        this.predicateWordHasToSatisfy = predicateWordHasToSatisfy;
    }

    public List<? extends AssistProposal> getReservedWordProposals(final String userContent) {
        return getReservedWordProposals(userContent, sortedByLabels());
    }

    public List<? extends AssistProposal> getReservedWordProposals(final String userContent,
            final Comparator<AssistProposal> comparator) {

        final List<RedCodeReservedWordProposal> proposals = new ArrayList<>();

        proposals.addAll(generateProposalsFrom(GHERKIN_ELEMENTS, userContent));
        // match against both variants, but add only one proposal
        if (predicateWordHasToSatisfy.test(FOR_LOOP_1)) {
            Stream.of(FOR_LOOP_1, FOR_LOOP_2)
                    .map(variant -> matcher.matches(userContent, variant))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .ifPresent(
                            match -> proposals.add(AssistProposals.createCodeReservedWordProposal(FOR_LOOP_1, match)));
        }
        if (version.isNewerOrEqualTo(new RobotVersion(3, 1))) {
            proposals.addAll(generateProposalsFrom(NEW_FOR_LOOP_LITERALS, userContent));
        }
        proposals.addAll(generateProposalsFrom(LOOP_ELEMENTS, userContent));

        proposals.sort(comparator);
        return proposals;
    }

    private List<RedCodeReservedWordProposal> generateProposalsFrom(final Iterable<String> words,
            final String userContent) {

        final List<RedCodeReservedWordProposal> proposals = new ArrayList<>();
        for (final String word : words) {
            if (predicateWordHasToSatisfy.test(word)) {
                final Optional<ProposalMatch> match = matcher.matches(userContent, word);
                if (match.isPresent()) {
                    proposals.add(AssistProposals.createCodeReservedWordProposal(word, match.get()));
                }
            }
        }
        return proposals;
    }
}
