/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.robotframework.ide.eclipse.main.plugin.assist.AssistProposals.sortedByLabels;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

public class ForLoopReservedWordsProposals {

    static final Map<String, String> DESCRIPTIONS = new LinkedHashMap<>();
    static {
        DESCRIPTIONS.put(":FOR",
                "Defines loop used to repeat keywords. The actions to be repeated has to be written in next lines "
                        + "and has to bo indented.\n\n*Important:* This is an old syntax which is going to be deprecated"
                        + " in Robot Framework 3.2 and eventually removed. Use FOR-END syntax instead.");
        DESCRIPTIONS.put("FOR",
                "Defines loop used to repeat keywords. The actions to be repeated has to be written in next lines "
                        + "and matched by following END line after last action in loop body.\n\nAvailable from Robot "
                        + "Framework 3.1");
        DESCRIPTIONS.put("END", "Marks end of FOR loop construct.\n\nAvailable from Robot Framework 3.1");
    }

    static final String FOR_LOOP_1 = ":FOR";

    static final String FOR_LOOP_2 = ": FOR";

    static final List<String> NEW_FOR_LOOP_LITERALS = ImmutableList.of("FOR", "END");

    private static final List<String> LOOP_ELEMENTS = ImmutableList.of("IN", "IN ENUMERATE", "IN RANGE", "IN ZIP");

    private final ProposalMatcher matcher;

    private final AssistProposalPredicate<String> predicateWordHasToSatisfy;

    public ForLoopReservedWordsProposals(final AssistProposalPredicate<String> predicateWordHasToSatisfy) {
        this(ProposalMatchers.substringMatcher(), predicateWordHasToSatisfy);
    }

    @VisibleForTesting
    ForLoopReservedWordsProposals(final ProposalMatcher matcher,
            final AssistProposalPredicate<String> predicateWordHasToSatisfy) {
        this.matcher = matcher;
        this.predicateWordHasToSatisfy = predicateWordHasToSatisfy;
    }

    public List<? extends AssistProposal> getReservedWordProposals(final String userContent) {
        return getReservedWordProposals(userContent, sortedByLabels());
    }

    public List<? extends AssistProposal> getReservedWordProposals(final String userContent,
            final Comparator<AssistProposal> comparator) {

        final List<ForLoopReservedWordProposal> proposals = new ArrayList<>();

        // match against both variants, but add only one proposal
        if (predicateWordHasToSatisfy.test(FOR_LOOP_1)) {
            Stream.of(FOR_LOOP_1, FOR_LOOP_2)
                    .map(variant -> matcher.matches(userContent, variant))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .ifPresent(match -> proposals
                            .add(AssistProposals.createForLoopReservedWordProposal(FOR_LOOP_1, match)));
        }
        proposals.addAll(generateProposalsFrom(NEW_FOR_LOOP_LITERALS, userContent));
        proposals.addAll(generateProposalsFrom(LOOP_ELEMENTS, userContent));

        proposals.sort(comparator);
        return proposals;
    }

    private List<ForLoopReservedWordProposal> generateProposalsFrom(final Iterable<String> words,
            final String userContent) {

        final List<ForLoopReservedWordProposal> proposals = new ArrayList<>();
        for (final String word : words) {
            if (predicateWordHasToSatisfy.test(word)) {
                final Optional<ProposalMatch> match = matcher.matches(userContent, word);
                if (match.isPresent()) {
                    proposals.add(AssistProposals.createForLoopReservedWordProposal(word, match.get()));
                }
            }
        }
        return proposals;
    }
}
