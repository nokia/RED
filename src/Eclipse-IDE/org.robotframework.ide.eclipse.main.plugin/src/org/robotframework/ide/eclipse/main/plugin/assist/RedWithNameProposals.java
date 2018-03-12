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

public class RedWithNameProposals {

    static final String LIBRARY = "library";

    static final String WITH_NAME = "WITH NAME";

    private final ProposalMatcher matcher;

    private final AssistProposalPredicate<String> predicateWordHasToSatisfy;

    public RedWithNameProposals(final AssistProposalPredicate<String> predicateWordHasToSatisfy) {
        this(ProposalMatchers.substringMatcher(), predicateWordHasToSatisfy);
    }

    @VisibleForTesting
    RedWithNameProposals(final ProposalMatcher matcher,
            final AssistProposalPredicate<String> predicateWordHasToSatisfy) {
        this.matcher = matcher;
        this.predicateWordHasToSatisfy = predicateWordHasToSatisfy;
    }

    public List<? extends AssistProposal> getWithNameProposals(final String userContent) {
        return getWithNameProposals(userContent, sortedByLabels());
    }

    public List<? extends AssistProposal> getWithNameProposals(final String userContent,
            final Comparator<AssistProposal> comparator) {

        final List<RedWithNameProposal> proposals = new ArrayList<>();

        if (predicateWordHasToSatisfy.test(WITH_NAME)) {
            final Optional<ProposalMatch> withNameMatch = matcher.matches(userContent, WITH_NAME);
            if (withNameMatch.isPresent()) {
                proposals.add(AssistProposals.createWithNameProposal(WITH_NAME, withNameMatch.get()));
            }
        }

        return proposals;
    }
}
