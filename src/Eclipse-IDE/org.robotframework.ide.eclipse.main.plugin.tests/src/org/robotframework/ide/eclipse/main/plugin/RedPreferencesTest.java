/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.EnumSet;

import org.assertj.core.api.Condition;
import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.FoldableElements;

import com.google.common.collect.Iterables;

public class RedPreferencesTest {

    @Test
    public void verifyFoldableElements_allCombinations() {
        // just encode all combinations as 4 bit number
        for (int i = 0; i < 16; i++) {
            final IPreferenceStore store = mock(IPreferenceStore.class);
            when(store.getBoolean(RedPreferences.FOLDABLE_SECTIONS)).thenReturn((i & 8) != 0);
            when(store.getBoolean(RedPreferences.FOLDABLE_CASES)).thenReturn((i & 4) != 0);
            when(store.getBoolean(RedPreferences.FOLDABLE_KEYWORDS)).thenReturn((i & 2) != 0);
            when(store.getBoolean(RedPreferences.FOLDABLE_DOCUMENTATION)).thenReturn((i & 1) != 0);

            final RedPreferences preferences = new RedPreferences(store);

            final EnumSet<FoldableElements> elements = preferences.getFoldableElements();

            assertThat(elements).is(contatingElementOnlyWhen(FoldableElements.SECTIONS, (i & 8) != 0));
            assertThat(elements).is(contatingElementOnlyWhen(FoldableElements.CASES, (i & 4) != 0));
            assertThat(elements).is(contatingElementOnlyWhen(FoldableElements.KEYWORDS, (i & 2) != 0));
            assertThat(elements).is(contatingElementOnlyWhen(FoldableElements.DOCUMENTATION, (i & 1) != 0));
        }
    }

    private static <T extends Iterable<?>> Condition<T> contatingElementOnlyWhen(final Object element,
            final boolean condition) {
        return new Condition<T>() {
            @Override
            public boolean matches(final T iterable) {
                if (condition) {
                    return Iterables.contains(iterable, element);
                } else {
                    return !Iterables.contains(iterable, element);
                }
            }
        };
    }
}
