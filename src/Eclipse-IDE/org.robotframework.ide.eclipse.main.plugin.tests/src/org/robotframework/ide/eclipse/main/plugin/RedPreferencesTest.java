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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.FoldableElements;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.ElementOpenMode;

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

            assertThat(elements).is(containingElementOnlyWhen(FoldableElements.SECTIONS, (i & 8) != 0));
            assertThat(elements).is(containingElementOnlyWhen(FoldableElements.CASES, (i & 4) != 0));
            assertThat(elements).is(containingElementOnlyWhen(FoldableElements.KEYWORDS, (i & 2) != 0));
            assertThat(elements).is(containingElementOnlyWhen(FoldableElements.DOCUMENTATION, (i & 1) != 0));
        }
    }

    @Test
    public void elementsOpenModeIsTakenFromStore_1() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getString(RedPreferences.FILE_ELEMENTS_OPEN_MODE)).thenReturn(ElementOpenMode.OPEN_IN_SOURCE.name());

        final RedPreferences preferences = new RedPreferences(store);

        assertThat(preferences.getElementOpenMode()).isEqualTo(ElementOpenMode.OPEN_IN_SOURCE);
    }

    @Test
    public void elementsOpenModeIsTakenFromStore_2() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getString(RedPreferences.FILE_ELEMENTS_OPEN_MODE)).thenReturn(ElementOpenMode.OPEN_IN_TABLES.name());

        final RedPreferences preferences = new RedPreferences(store);

        assertThat(preferences.getElementOpenMode()).isEqualTo(ElementOpenMode.OPEN_IN_TABLES);
    }

    @Test
    public void equalsColoringPreferences_areEquals() {
        final ColoringPreference pref1 = new ColoringPreference(new RGB(1, 2, 3), SWT.NONE);
        final ColoringPreference pref2 = new ColoringPreference(new RGB(1, 2, 3), SWT.NONE);

        assertThat(pref1).isEqualTo(pref2);
    }

    private static <T extends Iterable<?>> Condition<T> containingElementOnlyWhen(final Object element,
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
