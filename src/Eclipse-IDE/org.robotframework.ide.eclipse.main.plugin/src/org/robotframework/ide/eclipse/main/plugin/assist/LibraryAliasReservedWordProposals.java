/*
* Copyright 2017 Nokia Solutions and Networks
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

public class LibraryAliasReservedWordProposals {

    public static final String WITH_NAME = "WITH NAME";

    private final ProposalMatcher matcher;

    private final AssistProposalPredicate<String> predicateWordHasToSatisfy;

    public LibraryAliasReservedWordProposals(final AssistProposalPredicate<String> predicateWordHasToSatisfy) {
        this(ProposalMatchers.substringMatcher(), predicateWordHasToSatisfy);
    }

    @VisibleForTesting
    LibraryAliasReservedWordProposals(final ProposalMatcher matcher,
            final AssistProposalPredicate<String> predicateWordHasToSatisfy) {
        this.matcher = matcher;
        this.predicateWordHasToSatisfy = predicateWordHasToSatisfy;
    }

    public List<? extends AssistProposal> getReservedWordProposals(final String userContent) {
        return getReservedWordProposals(userContent, sortedByLabels());
    }

    public List<? extends AssistProposal> getReservedWordProposals(final String userContent,
            final Comparator<AssistProposal> comparator) {

        final List<LibraryAliasReservedWordProposal> proposals = new ArrayList<>();

        if (predicateWordHasToSatisfy.test(WITH_NAME)) {
            final Optional<ProposalMatch> match = matcher.matches(userContent, WITH_NAME);
            if (match.isPresent()) {
                proposals.add(AssistProposals.createLibraryAliasReservedWordProposal(match.get()));
            }
        }

        return proposals;
    }
}
