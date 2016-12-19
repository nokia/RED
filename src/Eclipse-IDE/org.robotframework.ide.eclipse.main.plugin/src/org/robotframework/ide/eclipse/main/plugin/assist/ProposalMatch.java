/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.Range;

public final class ProposalMatch implements Iterable<Range<Integer>> {

    public static final ProposalMatch EMPTY = new ProposalMatch();

    private final List<Range<Integer>> matches;

    @SafeVarargs
    public ProposalMatch(final Range<Integer>... matchingRanges) {
        this.matches = newArrayList(matchingRanges);
    }

    public ProposalMatch(final List<Range<Integer>> matches) {
        this.matches = matches;
    }

    @Override
    public Iterator<Range<Integer>> iterator() {
        return matches.iterator();
    }

    // limits current matches only to given range and transforms them into given domain
    Optional<ProposalMatch> mapAndShiftToFragment(final int startIndex, final int length) {
        final List<Range<Integer>> rangesInDomain = new ArrayList<>();

        final Range<Integer> targetDomain = Range.closedOpen(startIndex, startIndex + length);
        for (final Range<Integer> match : this) {
            if (match.encloses(targetDomain)) {
                rangesInDomain.add(targetDomain);
            } else if (targetDomain.encloses(match)) {
                rangesInDomain.add(match);
            } else if (match.lowerEndpoint().intValue() <= startIndex
                    && startIndex <= match.upperEndpoint().intValue()) {
                rangesInDomain.add(Range.closedOpen(startIndex, match.upperEndpoint()));
            } else if (match.lowerEndpoint().intValue() <= startIndex + length
                    && startIndex + startIndex <= match.upperEndpoint().intValue()) {
                rangesInDomain.add(Range.closedOpen(match.lowerEndpoint(), startIndex + length));
            }
        }

        final List<Range<Integer>> newMatches = new ArrayList<>();
        for (final Range<Integer> match : rangesInDomain) {
            newMatches.add(Range.closedOpen(match.lowerEndpoint() - startIndex, match.upperEndpoint() - startIndex));
        }
        return newMatches.isEmpty() ? Optional.<ProposalMatch> absent() : Optional.of(new ProposalMatch(newMatches));
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == ProposalMatch.class) {
            final ProposalMatch that = (ProposalMatch) obj;
            return this.matches.equals(that.matches);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return matches.hashCode();
    };

    @Override
    public String toString() {
        return matches.toString();
    }
}
