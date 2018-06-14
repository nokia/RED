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
    public void hasEmbeddedArgumentsTest() {
        assertThat(EmbeddedKeywordNamesSupport.hasEmbeddedArguments("abc")).isFalse();

        assertThat(EmbeddedKeywordNamesSupport.hasEmbeddedArguments("abc${x}")).isTrue();
    }

    @Test
    public void containsMatchesTest() {
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abcAbC", "")).hasValue(Range.closedOpen(0, 0));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abcabc", "A")).hasValue(Range.closedOpen(0, 1));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abcabc", "ab")).hasValue(Range.closedOpen(0, 2));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("ABCabc", "Bc")).hasValue(Range.closedOpen(1, 3));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("AbCaBc", "bC")).hasValue(Range.closedOpen(1, 3));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abcAbC", "x")).isNotPresent();

        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}xyz", "")).hasValue(Range.closedOpen(0, 0));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}xyz", "b")).hasValue(Range.closedOpen(1, 2));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}xyz", "bc")).hasValue(Range.closedOpen(1, 3));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}xyz", "bcd"))
                .hasValue(Range.closedOpen(1, 7));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}xyz", "bcde"))
                .hasValue(Range.closedOpen(1, 7));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}xyz", "bcdef"))
                .hasValue(Range.closedOpen(1, 7));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}xyz", "bcdefg"))
                .hasValue(Range.closedOpen(1, 7));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}xyz", "bcdefgh"))
                .hasValue(Range.closedOpen(1, 7));

        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}g${y}hi${z}", ""))
                .hasValue(Range.closedOpen(0, 0));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}g${y}hi${z}", "b"))
                .hasValue(Range.closedOpen(1, 2));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}g${y}hi${z}", "bc"))
                .hasValue(Range.closedOpen(1, 3));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}g${y}hi${z}", "bcd"))
                .hasValue(Range.closedOpen(1, 7));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}g${y}hi${z}", "bcde"))
                .hasValue(Range.closedOpen(1, 7));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}g${y}hi${z}", "bcdef"))
                .hasValue(Range.closedOpen(1, 7));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}g${y}hi${z}", "bcdefg"))
                .hasValue(Range.closedOpen(1, 8));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}g${y}hi${z}", "bcdefgh"))
                .hasValue(Range.closedOpen(1, 12));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}g${y}hi${z}", "bcdefghi"))
                .hasValue(Range.closedOpen(1, 12));

        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}xyz", "vwxyz"))
                .hasValue(Range.closedOpen(3, 10));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}xyz", "wxyz"))
                .hasValue(Range.closedOpen(3, 10));
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}xyz", "xyz"))
                .hasValue(Range.closedOpen(7, 10));

        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("${x}abc", "def")).isNotPresent();
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}xyz", "def")).isNotPresent();
        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x}", "def")).isNotPresent();

        assertThat(EmbeddedKeywordNamesSupport.containsIgnoreCase("abc${x:[}xyz", "def")).isNotPresent();
    }

    @Test
    public void nameMatchesTest() {
        assertThat(EmbeddedKeywordNamesSupport.matchesIgnoreCase("abc", "")).isFalse();
        assertThat(EmbeddedKeywordNamesSupport.matchesIgnoreCase("abc", "x")).isFalse();

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

        assertThat(EmbeddedKeywordNamesSupport.matchesIgnoreCase("incorrect regex ${date:[}", "word")).isFalse();
    }

    @Test
    public void nameMatchesTest_whenUnicodeIsUsed() {
        assertThat(EmbeddedKeywordNamesSupport.matchesIgnoreCase("Łączę ${x}", "łączę z serwerem")).isTrue();
        assertThat(EmbeddedKeywordNamesSupport.matchesIgnoreCase("Łączę ${x}", "ŁĄCZĘ Z SERWEREM")).isTrue();
        assertThat(EmbeddedKeywordNamesSupport.matchesIgnoreCase("Łączę ${x}", "ŁąCzĘ z SeRwErEm")).isTrue();
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

    @Test
    public void regexRemoveTest() {
        assertThat(EmbeddedKeywordNamesSupport.removeRegex("${x}")).isEqualTo("${x}");

        assertThat(EmbeddedKeywordNamesSupport.removeRegex("${x:\\\\d+}")).isEqualTo("${x}");
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
