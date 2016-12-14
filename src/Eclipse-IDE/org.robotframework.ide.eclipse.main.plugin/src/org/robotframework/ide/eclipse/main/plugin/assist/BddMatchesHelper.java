/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.concurrent.atomic.AtomicReference;

import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport.NameOperation;

import com.google.common.base.Optional;

class BddMatchesHelper {

    private final ProposalMatcher matcher;

    BddMatchesHelper(final ProposalMatcher matcher) {
        this.matcher = matcher;
    }

    BddAwareProposalMatch findBddAwareMatch(final String userContent, final String proposalContent) {
        final StringBuilder gherkinPrefix = new StringBuilder();
        final AtomicReference<Optional<ProposalMatch>> match = new AtomicReference<>(Optional.<ProposalMatch> absent());
        GherkinStyleSupport.forEachPossibleGherkinName(userContent, new NameOperation() {

            @Override
            public void perform(final String gherkinNameVariant) {
                if (match.get().isPresent()) {
                    return;
                }
                final Optional<ProposalMatch> keywordMatch = matcher.matches(gherkinNameVariant, proposalContent);
                if (keywordMatch.isPresent()) {
                    match.set(keywordMatch);
                    gherkinPrefix.append(userContent.substring(0, userContent.length() - gherkinNameVariant.length()));
                }
            }
        });
        return new BddAwareProposalMatch(match.get(), gherkinPrefix.toString());
    }

    static class BddAwareProposalMatch {

        private final Optional<ProposalMatch> match;

        private final String bddPrefix;

        public BddAwareProposalMatch(final Optional<ProposalMatch> match, final String bddPrefix) {
            this.match = match;
            this.bddPrefix = bddPrefix;
        }

        public Optional<ProposalMatch> getMatch() {
            return match;
        }

        public String getBddPrefix() {
            return bddPrefix;
        }
    }
}
