/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;

import com.google.common.base.Optional;
import com.google.common.collect.Range;

public class ProposalMatchers {

    public static ProposalMatcher prefixesMatcher() {
        return new ProposalMatcher() {

            @Override
            public Optional<ProposalMatch> matches(final String userContent, final String proposalContent) {
                if (proposalContent.toLowerCase().startsWith(userContent.toLowerCase())) {
                    return Optional.of(new ProposalMatch(Range.closedOpen(0, userContent.length())));
                } else {
                    return Optional.absent();
                }
            }
        };
    }

    public static ProposalMatcher caseSensitivePrefixesMatcher() {
        return new ProposalMatcher() {

            @Override
            public Optional<ProposalMatch> matches(final String userContent, final String proposalContent) {
                if (proposalContent.startsWith(userContent)) {
                    return Optional.of(new ProposalMatch(Range.closedOpen(0, userContent.length())));
                } else {
                    return Optional.absent();
                }
            }
        };
    }

    public static ProposalMatcher embeddedKeywordsMatcher() {
        return new ProposalMatcher() {

            @Override
            public Optional<ProposalMatch> matches(final String userContent, final String proposalContent) {
                if (EmbeddedKeywordNamesSupport.startsWith(proposalContent, userContent)) {
                    return Optional.of(new ProposalMatch(Range.closedOpen(0, userContent.length())));
                } else {
                    return Optional.absent();
                }
            }
        };
    }
}
