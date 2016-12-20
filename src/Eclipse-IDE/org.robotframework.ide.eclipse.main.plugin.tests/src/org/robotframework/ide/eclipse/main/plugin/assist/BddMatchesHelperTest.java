/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.Conditions.absent;
import static org.robotframework.red.junit.Conditions.containing;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.assist.BddMatchesHelper.BddAwareProposalMatch;

import com.google.common.collect.Range;

public class BddMatchesHelperTest {

    private final BddMatchesHelper bddMatcher = new BddMatchesHelper(ProposalMatchers.prefixesMatcher());

    @Test
    public void thereIsNoMatch_whenNoBddPrefixIsUsedAndProposalDoesNotMatchPrefix() {
        final BddAwareProposalMatch match = bddMatcher.findBddAwareMatch("prefix", "keyword");
        assertThat(match.getMatch()).is(absent());
        assertThat(match.getBddPrefix()).isEmpty();
    }

    @Test
    public void thereIsAMatch_whenNoBddPrefixIsUsedAndProposalDoesMatchPrefix() {
        final BddAwareProposalMatch match = bddMatcher.findBddAwareMatch("keyw", "keyword");
        assertThat(match.getMatch()).is(containing(new ProposalMatch(Range.closedOpen(0, 4))));
        assertThat(match.getBddPrefix()).isEmpty();
    }

    @Test
    public void thereIsNoMatch_whenBddPrefixIsUsedButProposalDoesNotMatchContent() {
        final BddAwareProposalMatch match = bddMatcher.findBddAwareMatch("Given prefix", "keyword");
        assertThat(match.getMatch()).is(absent());
        assertThat(match.getBddPrefix()).isEmpty();
    }

    @Test
    public void thereIsAMatch_whenBddPrefixIsUsedAndProposalMatchesContent() {
        final BddAwareProposalMatch match = bddMatcher.findBddAwareMatch("Given keyw", "keyword");
        assertThat(match.getMatch()).is(containing(new ProposalMatch(Range.closedOpen(0, 4))));
        assertThat(match.getBddPrefix()).isEqualTo("Given ");
    }

    @Test
    public void thereIsAMatch_whenBddPrefixIsDuplicatedUsedAndProposalMatchesContent() {
        final BddAwareProposalMatch match = bddMatcher.findBddAwareMatch("Given And keyw", "keyword");
        assertThat(match.getMatch()).is(containing(new ProposalMatch(Range.closedOpen(0, 4))));
        assertThat(match.getBddPrefix()).isEqualTo("Given And ");
    }

    @Test
    public void thereIsAMatch_whenGivenKeywordHasANameWhichIsAlsoBddWord_1() {
        final BddAwareProposalMatch match = bddMatcher.findBddAwareMatch("Given key", "Given keyword");
        assertThat(match.getMatch()).is(containing(new ProposalMatch(Range.closedOpen(0, 9))));
        assertThat(match.getBddPrefix()).isEmpty();
    }

    @Test
    public void thereIsAMatch_whenGivenKeywordHasANameWhichIsAlsoBddWord_2() {
        final BddAwareProposalMatch match = bddMatcher.findBddAwareMatch("Given Given key", "Given keyword");
        assertThat(match.getMatch()).is(containing(new ProposalMatch(Range.closedOpen(0, 9))));
        assertThat(match.getBddPrefix()).isEqualTo("Given ");
    }
}
