/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory;

public class RedPreferencesInitializerTest {

    @Test
    public void byDefaultAllElementsAreFoldable() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences);

        verify(preferences).putInt(RedPreferences.FOLDING_LINE_LIMIT, 2);
        verify(preferences).putBoolean(RedPreferences.FOLDABLE_SECTIONS, true);
        verify(preferences).putBoolean(RedPreferences.FOLDABLE_CASES, true);
        verify(preferences).putBoolean(RedPreferences.FOLDABLE_KEYWORDS, true);
        verify(preferences).putBoolean(RedPreferences.FOLDABLE_DOCUMENTATION, true);
    }

    @Test
    public void byDefaultAllSyntaxHighlightingCategoryPreferencesAreInitialized() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences);

        for (final SyntaxHighlightingCategory category : SyntaxHighlightingCategory.values()) {
            final String prefix = RedPreferences.SYNTAX_COLORING_PREFIX + category.getId();
            final ColoringPreference preference = category.getDefault();
            verify(preferences).putInt(prefix + ".fontStyle", preference.getFontStyle());
            verify(preferences).putInt(prefix + ".color.r", preference.getRgb().red);
            verify(preferences).putInt(prefix + ".color.g", preference.getRgb().green);
            verify(preferences).putInt(prefix + ".color.b", preference.getRgb().blue);
        }
    }

    @Test
    public void byDefaultAllProblemCategoryPreferencesAreInitialized() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences);

        for (final ProblemCategory category : ProblemCategory.values()) {
            verify(preferences).put(category.getId(), category.getDefaultSeverity().name());
        }
    }

}
