/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.google.common.collect.Range;

public class ProposalMatchTest {

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
        final int hashcode2 = newArrayList(Range.closedOpen(2, 5), Range.closedOpen(7, 10)).hashCode();

        assertThat(hashcode1).isEqualTo(hashcode2);
    }

    @Test
    public void emptyMatchMappingAlwaysResultsInEmptyMatch() {
        final ProposalMatch match = new ProposalMatch();

        assertThat(match.mapAndShiftToFragment(0, 10)).isNotPresent();
        assertThat(match.mapAndShiftToFragment(100, 30)).isNotPresent();
        assertThat(match.mapAndShiftToFragment(1, 5)).isNotPresent();
    }

    @Test
    public void singleRangeMatchMappingProperly() {
        final ProposalMatch match = new ProposalMatch(Range.closedOpen(6, 9));

        assertThat(match.mapAndShiftToFragment(0, 4)).isNotPresent();
        assertThat(match.mapAndShiftToFragment(0, 5)).isNotPresent();
        assertThat(match.mapAndShiftToFragment(0, 6)).hasValue(new ProposalMatch(Range.closedOpen(6, 6)));
        assertThat(match.mapAndShiftToFragment(2, 6)).hasValue(new ProposalMatch(Range.closedOpen(4, 6)));
        assertThat(match.mapAndShiftToFragment(5, 8)).hasValue(new ProposalMatch(Range.closedOpen(1, 4)));
        assertThat(match.mapAndShiftToFragment(5, 3)).hasValue(new ProposalMatch(Range.closedOpen(1, 3)));
        assertThat(match.mapAndShiftToFragment(7, 1)).hasValue(new ProposalMatch(Range.closedOpen(0, 1)));
        assertThat(match.mapAndShiftToFragment(7, 8)).hasValue(new ProposalMatch(Range.closedOpen(0, 2)));
        assertThat(match.mapAndShiftToFragment(9, 5)).hasValue(new ProposalMatch(Range.closedOpen(0, 0)));
        assertThat(match.mapAndShiftToFragment(15, 5)).isNotPresent();
    }

    @Test
    public void multipleRangeMatchMappingProperly() {
        final ProposalMatch match = new ProposalMatch(Range.closedOpen(2, 3), Range.closedOpen(5, 7),
                Range.closedOpen(10, 15));

        assertThat(match.mapAndShiftToFragment(0, 1)).isNotPresent();
        assertThat(match.mapAndShiftToFragment(0, 4)).isNotPresent();
        assertThat(match.mapAndShiftToFragment(0, 9)).isNotPresent();
        assertThat(match.mapAndShiftToFragment(0, 10))
                .hasValue(new ProposalMatch(Range.closedOpen(2, 3), Range.closedOpen(5, 7), Range.closedOpen(10, 10)));
        assertThat(match.mapAndShiftToFragment(1, 10))
                .hasValue(new ProposalMatch(Range.closedOpen(1, 2), Range.closedOpen(4, 6), Range.closedOpen(9, 10)));
        assertThat(match.mapAndShiftToFragment(2, 14))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 1), Range.closedOpen(3, 5), Range.closedOpen(8, 13)));
        assertThat(match.mapAndShiftToFragment(3, 10))
                .hasValue(new ProposalMatch(Range.closedOpen(0, 0), Range.closedOpen(2, 4), Range.closedOpen(7, 10)));
        assertThat(match.mapAndShiftToFragment(4, 8)).isNotPresent();
        assertThat(match.mapAndShiftToFragment(4, 12)).isNotPresent();
        assertThat(match.mapAndShiftToFragment(8, 8)).isNotPresent();
        assertThat(match.mapAndShiftToFragment(20, 5)).isNotPresent();
    }
}
