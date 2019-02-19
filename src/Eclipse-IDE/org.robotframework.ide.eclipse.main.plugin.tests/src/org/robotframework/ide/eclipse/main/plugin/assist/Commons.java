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
        return (userContent, proposalContent) -> {
            if (proposalContent.toLowerCase().startsWith(userContent.toLowerCase())) {
                return Optional.of(new ProposalMatch(Range.closedOpen(0, userContent.length())));
            } else {
                return Optional.empty();
            }
        };
    }

    static Comparator<AssistProposal> firstProposalContaining(final String toContain) {
        return (p1, p2) -> {
            final boolean contains1 = p1.getLabel().contains(toContain);
            final boolean contains2 = p2.getLabel().contains(toContain);
            final int result = Boolean.compare(contains2, contains1);
            if (result != 0) {
                return result;
            }
            return Integer.compare(p1.getLabel().indexOf(toContain), p2.getLabel().indexOf(toContain));
        };
    }
}
