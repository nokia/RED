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
import java.util.List;

import org.assertj.core.api.Condition;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.junit.Test;
import org.rf.ide.core.rflint.RfLintRule;
import org.rf.ide.core.rflint.RfLintViolationSeverity;
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
    public void rfLintRulesFilesAreTakenFromStore_1() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getString(RedPreferences.RFLINT_RULES_FILES)).thenReturn("");

        final RedPreferences preferences = new RedPreferences(store);
        assertThat(preferences.getRfLintRulesFiles()).isEmpty();
    }

    @Test
    public void rfLintRulesFilesAreTakenFromStore_2() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getString(RedPreferences.RFLINT_RULES_FILES))
                .thenReturn("/path/to/firstfile.py;/path/to/sndfile.py");

        final RedPreferences preferences = new RedPreferences(store);
        assertThat(preferences.getRfLintRulesFiles()).containsExactly("/path/to/firstfile.py", "/path/to/sndfile.py");
    }

    @Test
    public void rfLintRulesConfigIsTakenFromStore_1() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getString(RedPreferences.RFLINT_RULES_CONFIG_NAMES)).thenReturn("");
        when(store.getString(RedPreferences.RFLINT_RULES_CONFIG_SEVERITIES)).thenReturn("");
        when(store.getString(RedPreferences.RFLINT_RULES_CONFIG_ARGS)).thenReturn("");

        final RedPreferences preferences = new RedPreferences(store);
        assertThat(preferences.getRfLintRules()).isEmpty();
    }

    @Test
    public void rfLintRulesConfigIsTakenFromStore_2() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getString(RedPreferences.RFLINT_RULES_CONFIG_NAMES)).thenReturn("Rule1;Rule2");
        when(store.getString(RedPreferences.RFLINT_RULES_CONFIG_SEVERITIES)).thenReturn("DEFAULT;ERROR");
        when(store.getString(RedPreferences.RFLINT_RULES_CONFIG_ARGS)).thenReturn("80;");

        final RedPreferences preferences = new RedPreferences(store);
        final List<RfLintRule> rules = preferences.getRfLintRules();
        assertThat(rules).hasSize(2);

        assertThat(rules.get(0).getRuleName()).isEqualTo("Rule1");
        assertThat(rules.get(0).getSeverity()).isEqualTo(RfLintViolationSeverity.DEFAULT);
        assertThat(rules.get(0).getConfiguration()).isEqualTo("80");

        assertThat(rules.get(1).getRuleName()).isEqualTo("Rule2");
        assertThat(rules.get(1).getSeverity()).isEqualTo(RfLintViolationSeverity.ERROR);
        assertThat(rules.get(1).getConfiguration()).isEmpty();
    }

    @Test
    public void messageLogOutputLimitIsTakenFromStore_1() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getBoolean(RedPreferences.LIMIT_MSG_LOG_OUTPUT)).thenReturn(false);
        when(store.getInt(RedPreferences.LIMIT_MSG_LOG_LENGTH)).thenReturn(1000);

        final RedPreferences preferences = new RedPreferences(store);
        assertThat(preferences.getMessageLogViewLimit()).isEmpty();
    }

    @Test
    public void messageLogOutputLimitIsTakenFromStore_2() {
        final IPreferenceStore store = mock(IPreferenceStore.class);
        when(store.getBoolean(RedPreferences.LIMIT_MSG_LOG_OUTPUT)).thenReturn(true);
        when(store.getInt(RedPreferences.LIMIT_MSG_LOG_LENGTH)).thenReturn(1000);

        final RedPreferences preferences = new RedPreferences(store);
        assertThat(preferences.getMessageLogViewLimit()).contains(1000);
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
