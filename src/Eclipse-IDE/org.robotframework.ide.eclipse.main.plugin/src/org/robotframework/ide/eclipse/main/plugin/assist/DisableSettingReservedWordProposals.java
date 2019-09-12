/*
 * Copyright 2019 Nokia Solutions and Networks
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

public class DisableSettingReservedWordProposals {

    public static final String NONE = "NONE";

    private final ProposalMatcher matcher;

    private final AssistProposalPredicate<String> predicateWordHasToSatisfy;

    public DisableSettingReservedWordProposals(final AssistProposalPredicate<String> predicateWordHasToSatisfy) {
        this(ProposalMatchers.substringMatcher(), predicateWordHasToSatisfy);
    }

    @VisibleForTesting
    DisableSettingReservedWordProposals(final ProposalMatcher matcher,
            final AssistProposalPredicate<String> predicateWordHasToSatisfy) {
        this.matcher = matcher;
        this.predicateWordHasToSatisfy = predicateWordHasToSatisfy;
    }

    public List<? extends AssistProposal> getReservedWordProposals(final String userContent) {
        return getReservedWordProposals(userContent, sortedByLabels());
    }

    public List<? extends AssistProposal> getReservedWordProposals(final String userContent,
            final Comparator<AssistProposal> comparator) {

        final List<DisableSettingReservedWordProposal> proposals = new ArrayList<>();

        if (predicateWordHasToSatisfy.test(NONE)) {
            final Optional<ProposalMatch> match = matcher.matches(userContent, NONE);
            if (match.isPresent()) {
                proposals.add(AssistProposals.createDisableSettingReservedWordProposal(match.get()));
            }
        }

        return proposals;
    }
}
