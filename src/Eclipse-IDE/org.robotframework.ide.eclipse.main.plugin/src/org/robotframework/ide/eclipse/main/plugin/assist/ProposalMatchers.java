/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.List;
import java.util.Optional;

import org.rf.ide.core.testdata.model.table.keywords.names.CamelCaseKeywordNamesSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;

import com.google.common.collect.Range;

public class ProposalMatchers {

    public static ProposalMatcher substringMatcher() {
        return new ProposalMatcher() {

            @Override
            public Optional<ProposalMatch> matches(final String userContent, final String proposalContent) {
                if (proposalContent.toLowerCase().contains(userContent.toLowerCase())) {
                    final int index = proposalContent.toLowerCase().indexOf(userContent.toLowerCase());
                    return Optional.of(new ProposalMatch(Range.closedOpen(index, index + userContent.length())));
                }
                return Optional.empty();
            }
        };
    }

    public static ProposalMatcher keywordsMatcher() {
        return new ProposalMatcher() {

            @Override
            public Optional<ProposalMatch> matches(final String userContent, final String proposalContent) {
                final List<Range<Integer>> ranges = CamelCaseKeywordNamesSupport.matches(proposalContent, userContent);
                if (!ranges.isEmpty()) {
                    return Optional.of(new ProposalMatch(ranges));
                }
                return EmbeddedKeywordNamesSupport.containsIgnoreCase(proposalContent, userContent)
                        .map(ProposalMatch::new);
            }
        };
    }
}
