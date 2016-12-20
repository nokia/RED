/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords.names;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;

import org.assertj.core.api.Condition;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public class EmbeddedKeywordNamesSupportTest {

    @Test
    public void prefixMatchesTest() {
        assertThat(EmbeddedKeywordNamesSupport.startsWithIgnoreCase("abc${x}xyz", "")).isEqualTo(0);
        assertThat(EmbeddedKeywordNamesSupport.startsWithIgnoreCase("abc${x}xyz", "a")).isEqualTo(1);
        assertThat(EmbeddedKeywordNamesSupport.startsWithIgnoreCase("abc${x}xyz", "ab")).isEqualTo(2);
        assertThat(EmbeddedKeywordNamesSupport.startsWithIgnoreCase("abc${x}xyz", "abc")).isEqualTo(3);
        assertThat(EmbeddedKeywordNamesSupport.startsWithIgnoreCase("abc${x}xyz", "abcd")).isEqualTo(7);
        assertThat(EmbeddedKeywordNamesSupport.startsWithIgnoreCase("abc${x}xyz", "abcde")).isEqualTo(7);
        assertThat(EmbeddedKeywordNamesSupport.startsWithIgnoreCase("abc${x}xyz", "abcdef")).isEqualTo(7);
        assertThat(EmbeddedKeywordNamesSupport.startsWithIgnoreCase("abc${x}xyz", "abcdefg")).isEqualTo(7);
        assertThat(EmbeddedKeywordNamesSupport.startsWithIgnoreCase("abc${x}xyz", "abcdefgh")).isEqualTo(7);

        assertThat(EmbeddedKeywordNamesSupport.startsWithIgnoreCase("abc${x}g${y}hi${z}", "")).isEqualTo(0);
        assertThat(EmbeddedKeywordNamesSupport.startsWithIgnoreCase("abc${x}g${y}hi${z}", "a")).isEqualTo(1);
        assertThat(EmbeddedKeywordNamesSupport.startsWithIgnoreCase("abc${x}g${y}hi${z}", "ab")).isEqualTo(2);
        assertThat(EmbeddedKeywordNamesSupport.startsWithIgnoreCase("abc${x}g${y}hi${z}", "abc")).isEqualTo(3);
        assertThat(EmbeddedKeywordNamesSupport.startsWithIgnoreCase("abc${x}g${y}hi${z}", "abcd")).isEqualTo(7);
        assertThat(EmbeddedKeywordNamesSupport.startsWithIgnoreCase("abc${x}g${y}hi${z}", "abcde")).isEqualTo(7);
        assertThat(EmbeddedKeywordNamesSupport.startsWithIgnoreCase("abc${x}g${y}hi${z}", "abcdef")).isEqualTo(7);
        assertThat(EmbeddedKeywordNamesSupport.startsWithIgnoreCase("abc${x}g${y}hi${z}", "abcdefg")).isEqualTo(8);
        assertThat(EmbeddedKeywordNamesSupport.startsWithIgnoreCase("abc${x}g${y}hi${z}", "abcdefgh")).isEqualTo(12);
        assertThat(EmbeddedKeywordNamesSupport.startsWithIgnoreCase("abc${x}g${y}hi${z}", "abcdefghi")).isEqualTo(12);
    }

    @Test
    public void nameMatchesTest() {
        assertThat(EmbeddedKeywordNamesSupport.matchesIgnoreCase("abc", "abc")).isTrue();
        assertThat(EmbeddedKeywordNamesSupport.matchesIgnoreCase("ABC", "abc")).isTrue();
        assertThat(EmbeddedKeywordNamesSupport.matchesIgnoreCase("abc", "ABC")).isTrue();

        assertThat(EmbeddedKeywordNamesSupport.matchesIgnoreCase("a${x}c", "abc")).isTrue();
        assertThat(EmbeddedKeywordNamesSupport.matchesIgnoreCase("a${x}c", "ABC")).isTrue();
        assertThat(EmbeddedKeywordNamesSupport.matchesIgnoreCase("A${x}C", "abc")).isTrue();

        assertThat(EmbeddedKeywordNamesSupport.matchesIgnoreCase("a${x}c", "abxyzc")).isTrue();
        assertThat(EmbeddedKeywordNamesSupport.matchesIgnoreCase("a${x}c", "ABXYZC")).isTrue();
        assertThat(EmbeddedKeywordNamesSupport.matchesIgnoreCase("A${x}C", "abxyzc")).isTrue();

        assertThat(EmbeddedKeywordNamesSupport.matchesIgnoreCase("a${x:\\d+}c", "ABXYZC")).isFalse();
        assertThat(EmbeddedKeywordNamesSupport.matchesIgnoreCase("a${x:\\d+}c", "A1C")).isTrue();

        assertThat(EmbeddedKeywordNamesSupport.matchesIgnoreCase("a${x:\\D+}c", "ABXYZC")).isTrue();
        assertThat(EmbeddedKeywordNamesSupport.matchesIgnoreCase("a${x:\\D+}c", "A1C")).isFalse();
        
        assertThat(EmbeddedKeywordNamesSupport.matchesIgnoreCase("today is ${date:\\d{4\\}-\\d{2\\}-\\d{2\\}}",
                "today is 2016-12-20")).isTrue();
    }

    @Test
    public void variableRangesAreFoundProperly() {
        assertThat(EmbeddedKeywordNamesSupport.findEmbeddedArgumentsRanges("")).is(empty());
        assertThat(EmbeddedKeywordNamesSupport.findEmbeddedArgumentsRanges("abc")).is(empty());
        assertThat(EmbeddedKeywordNamesSupport.findEmbeddedArgumentsRanges("ab$c")).is(empty());
        assertThat(EmbeddedKeywordNamesSupport.findEmbeddedArgumentsRanges("ab${c")).is(empty());
        assertThat(EmbeddedKeywordNamesSupport.findEmbeddedArgumentsRanges("ab${}c")).is(empty());
        
        assertThat(EmbeddedKeywordNamesSupport.findEmbeddedArgumentsRanges("${x}"))
                .is(containingExactly(Range.closed(0, 3)));
        assertThat(EmbeddedKeywordNamesSupport.findEmbeddedArgumentsRanges("ab${x}cd"))
                .is(containingExactly(Range.closed(2, 5)));

        assertThat(EmbeddedKeywordNamesSupport.findEmbeddedArgumentsRanges("${x}${y}"))
                .is(containingExactly(Range.closed(0, 3), Range.closed(4, 7)));
        assertThat(EmbeddedKeywordNamesSupport.findEmbeddedArgumentsRanges("ab${x}c${y}d"))
                .is(containingExactly(Range.closed(2, 5), Range.closed(7, 10)));
        
        assertThat(EmbeddedKeywordNamesSupport.findEmbeddedArgumentsRanges("a${x:\\d+}b"))
                .is(containingExactly(Range.closed(1, 8)));

        assertThat(EmbeddedKeywordNamesSupport.findEmbeddedArgumentsRanges("a${x:\\d{3\\}}b"))
                .is(containingExactly(Range.closed(1, 11)));
    }

    private static Condition<? super RangeSet<Integer>> empty() {
        return new Condition<RangeSet<Integer>>() {

            @Override
            public boolean matches(final RangeSet<Integer> value) {
                return value.isEmpty();
            }
        };
    }

    @SafeVarargs
    private static Condition<? super RangeSet<Integer>> containingExactly(final Range<Integer>... elements) {
        return new Condition<RangeSet<Integer>>(
                "contain exactly and in order: [" + Joiner.on(", ").join(elements) + "]") {

            @Override
            public boolean matches(final RangeSet<Integer> value) {
                final Iterator<Range<Integer>> ranges = value.asRanges().iterator();
                for (int i = 0; i < elements.length; i++) {
                    if (!ranges.hasNext() || !ranges.next().equals(elements[i])) {
                        return false;
                    }
                }
                return !ranges.hasNext();
            }
        };
    }

}
