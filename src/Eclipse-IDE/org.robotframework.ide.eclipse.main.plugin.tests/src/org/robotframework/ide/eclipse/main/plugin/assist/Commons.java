/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.Comparator;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Range;

class Commons {

    static ProposalMatcher substringMatcher() {
        return new ProposalMatcher() {

            @Override
            public Optional<ProposalMatch> matches(final String userContent, final String proposalContent) {
                if (proposalContent.toLowerCase().contains(userContent.toLowerCase())) {
                    final int index = proposalContent.toLowerCase().indexOf(userContent.toLowerCase());
                    return Optional.of(new ProposalMatch(Range.closedOpen(index, index + userContent.length())));
                }
                return Optional.absent();
            }
        };
    }

    static <T> Comparator<T> reverseComparator(final Comparator<T> comparator) {
        return new Comparator<T>() {

            @Override
            public int compare(final T o1, final T o2) {
                return comparator.compare(o2, o1);
            }
        };
    }

    static Function<AssistProposal, String> toLabels() {
        return new Function<AssistProposal, String>() {

            @Override
            public String apply(final AssistProposal proposal) {
                return proposal.getLabel();
            }
        };
    }
}
