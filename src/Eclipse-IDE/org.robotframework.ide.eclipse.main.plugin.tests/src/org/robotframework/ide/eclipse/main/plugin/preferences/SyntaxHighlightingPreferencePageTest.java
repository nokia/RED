/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.ui.IWorkbench;
import org.junit.Test;

import com.google.common.collect.Range;

public class SyntaxHighlightingPreferencePageTest {

    @Test
    public void initDoesNothing() {
        final IWorkbench workbench = mock(IWorkbench.class);

        final SyntaxHighlightingPreferencePage page = new SyntaxHighlightingPreferencePage();
        page.init(workbench);

        verifyZeroInteractions(workbench);
    }

    @Test
    public void styleRangesAreCreated() {
        final SyntaxHighlightingPreferencePage page = new SyntaxHighlightingPreferencePage();

        assertThat(page.createStyleRanges()).hasSize(69);
    }

    @Test
    public void styleRangesAreDisjunctive() {
        final SyntaxHighlightingPreferencePage page = new SyntaxHighlightingPreferencePage();

        final List<Range<Integer>> sortedRanges = page.createStyleRanges()
                .map(range -> Range.closedOpen(range.start, range.start + range.length))
                .sorted((r1, r2) -> Integer.compare(r1.lowerEndpoint(), r2.lowerEndpoint()))
                .collect(Collectors.toList());

        for (int i = 0; i < sortedRanges.size() - 1; i++) {
            final Range<Integer> r1 = sortedRanges.get(i);
            final Range<Integer> r2 = sortedRanges.get(i + 1);
            assertThat(r1.isConnected(r2) && !r1.intersection(r2).isEmpty())
                    .as("Range %s has not empty intersection with range %s", r1, r2)
                    .isFalse();
        }
    }

    @Test
    public void styleRangesAreEnclosedInSourceText() {
        final SyntaxHighlightingPreferencePage page = new SyntaxHighlightingPreferencePage();

        final Optional<Integer> minRangeStart = page.createStyleRanges()
                .map(range -> range.start)
                .min(Integer::compare);
        final Optional<Integer> maxRangeEnd = page.createStyleRanges()
                .map(range -> range.start + range.length)
                .max(Integer::compare);

        assertThat(minRangeStart).hasValueSatisfying(value -> assertThat(value).isNotNegative());
        assertThat(maxRangeEnd).hasValueSatisfying(
                value -> assertThat(value).isLessThanOrEqualTo(SyntaxHighlightingPreferencePageSource.source.length()));
    }

}
