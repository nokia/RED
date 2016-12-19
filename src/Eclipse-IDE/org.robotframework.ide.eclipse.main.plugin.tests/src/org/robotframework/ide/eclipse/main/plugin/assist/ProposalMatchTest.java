/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.Conditions.absent;
import static org.robotframework.red.junit.Conditions.containing;

import org.junit.Test;

import com.google.common.collect.Range;

public class ProposalMatchTest {

    @SuppressWarnings("unchecked")
    @Test
    public void matchesEqualityTest() {
        assertThat(new ProposalMatch().equals(ProposalMatch.EMPTY)).isTrue();
        assertThat(new ProposalMatch().equals(new ProposalMatch())).isTrue();
        assertThat(new ProposalMatch(newArrayList(Range.openClosed(0, 5)))
                .equals(new ProposalMatch(Range.openClosed(0, 5)))).isTrue();

        assertThat(new ProposalMatch().equals(null)).isFalse();
        assertThat(new ProposalMatch().equals(new Object())).isFalse();
        assertThat(new ProposalMatch(Range.closed(0, 5)).equals(new ProposalMatch(Range.openClosed(0, 5)))).isFalse();
        assertThat(new ProposalMatch(Range.closed(1, 2), Range.closed(3, 4))
                .equals(new ProposalMatch(Range.closed(3, 4), Range.closed(1, 2)))).isFalse();
    }

    @Test
    public void matchesHashCodeTest() {
        assertThat(new ProposalMatch().hashCode()).isEqualTo(ProposalMatch.EMPTY.hashCode());
        assertThat(new ProposalMatch().hashCode()).isEqualTo(new ProposalMatch().hashCode());
        assertThat(new ProposalMatch(Range.openClosed(0, 5)).hashCode())
                .isEqualTo(new ProposalMatch(Range.openClosed(0, 5)).hashCode());
    }

    @Test
    public void matchesHashCodeIsBuildFromRanges() {
        final int hashcode1 = new ProposalMatch(Range.closedOpen(2, 5), Range.closedOpen(7, 10)).hashCode();
        @SuppressWarnings("unchecked")
        final int hashcode2 = newArrayList(Range.closedOpen(2, 5), Range.closedOpen(7, 10)).hashCode();
        
        assertThat(hashcode1).isEqualTo(hashcode2);
    }

    @Test
    public void emptyMatchMappingAlwaysResultsInEmptyMatch() {
        final ProposalMatch match = new ProposalMatch();

        assertThat(match.mapAndShiftToFragment(0, 10)).is(absent());
        assertThat(match.mapAndShiftToFragment(100, 30)).is(absent());
        assertThat(match.mapAndShiftToFragment(1, 5)).is(absent());
    }

    @Test
    public void singleRangeMatchMappingProperly() {
        final ProposalMatch match = new ProposalMatch(Range.closedOpen(5, 10));
        
        assertThat(match.mapAndShiftToFragment(0, 3)).is(absent());
        assertThat(match.mapAndShiftToFragment(0, 4)).is(absent());
        assertThat(match.mapAndShiftToFragment(0, 5)).is(containing(new ProposalMatch(Range.closedOpen(5, 5))));
        assertThat(match.mapAndShiftToFragment(2, 5)).is(containing(new ProposalMatch(Range.closedOpen(3, 5))));
        assertThat(match.mapAndShiftToFragment(4, 8)).is(containing(new ProposalMatch(Range.closedOpen(1, 6))));
        assertThat(match.mapAndShiftToFragment(6, 3)).is(containing(new ProposalMatch(Range.closedOpen(0, 3))));
        assertThat(match.mapAndShiftToFragment(6, 8)).is(containing(new ProposalMatch(Range.closedOpen(0, 4))));
        assertThat(match.mapAndShiftToFragment(10, 5)).is(containing(new ProposalMatch(Range.closedOpen(0, 0))));
        assertThat(match.mapAndShiftToFragment(15, 5)).is(absent());
    }
}
