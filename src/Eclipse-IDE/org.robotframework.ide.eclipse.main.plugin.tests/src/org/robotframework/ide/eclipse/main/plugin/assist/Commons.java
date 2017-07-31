/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.Comparator;
import java.util.Optional;

import com.google.common.collect.Range;

class Commons {

    static ProposalMatcher prefixesMatcher() {
        return new ProposalMatcher() {

            @Override
            public Optional<ProposalMatch> matches(final String userContent, final String proposalContent) {
                if (proposalContent.toLowerCase().startsWith(userContent.toLowerCase())) {
                    return Optional.of(new ProposalMatch(Range.closedOpen(0, userContent.length())));
                } else {
                    return Optional.empty();
                }
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

    static Comparator<AssistProposal> firstProposalContaining(final String toContain) {
        return new Comparator<AssistProposal>() {

            @Override
            public int compare(final AssistProposal p1, final AssistProposal p2) {
                final boolean contains1 = p1.getLabel().contains(toContain);
                final boolean contains2 = p2.getLabel().contains(toContain);
                final int result = Boolean.compare(contains2, contains1);
                if (result != 0) {
                    return result;
                }
                return Integer.compare(p1.getLabel().indexOf(toContain), p2.getLabel().indexOf(toContain));
            }
        };
    }
}
