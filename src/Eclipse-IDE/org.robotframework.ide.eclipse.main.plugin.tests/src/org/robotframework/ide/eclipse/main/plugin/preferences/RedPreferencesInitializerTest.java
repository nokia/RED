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
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.CellWrappingStrategy;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.ElementOpenMode;
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

    @Test
    public void byDefaultElementsAreOpenedInSourcePageOfEditor() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences);

        verify(preferences).put(RedPreferences.FILE_ELEMENTS_OPEN_MODE, ElementOpenMode.OPEN_IN_SOURCE.name());
    }

    @Test
    public void byDefaultCellContentIsDrawnInSingleLine() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences);

        verify(preferences).put(RedPreferences.CELL_WRAPPING, CellWrappingStrategy.SINGLE_LINE_CUT.name());
    }

    @Test
    public void byDefaultAllLaunchConfigurationPreferencesAreInitialized() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences);

        verify(preferences).putBoolean(RedPreferences.LAUNCH_USE_ARGUMENT_FILE, true);
        verify(preferences).putBoolean(RedPreferences.LAUNCH_USE_SINGLE_COMMAND_LINE_ARGUMENT, false);
        verify(preferences).putBoolean(RedPreferences.LAUNCH_USE_SINGLE_FILE_DATA_SOURCE, false);
        verify(preferences).put(RedPreferences.LAUNCH_AGENT_CONNECTION_HOST, "127.0.0.1");
        verify(preferences).putInt(RedPreferences.LAUNCH_AGENT_CONNECTION_PORT, 43_981);
        verify(preferences).putInt(RedPreferences.LAUNCH_AGENT_CONNECTION_TIMEOUT, 30);
        verify(preferences).put(RedPreferences.DEBUGGER_SUSPEND_ON_ERROR, "prompt");
        verify(preferences).putBoolean(RedPreferences.DEBUGGER_OMIT_LIB_KEYWORDS, false);
    }

    @Test
    public void byDefaultAllContentAssistPreferencesAreInitialized() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences);

        verify(preferences).putBoolean(RedPreferences.ASSISTANT_AUTO_ACTIVATION_ENABLED, true);
        verify(preferences).putInt(RedPreferences.ASSISTANT_AUTO_ACTIVATION_DELAY, 100);
        verify(preferences).put(RedPreferences.ASSISTANT_AUTO_ACTIVATION_CHARS, "");
        verify(preferences).putBoolean(RedPreferences.ASSISTANT_KEYWORD_PREFIX_AUTO_ADDITION_ENABLED, false);
        verify(preferences).putBoolean(RedPreferences.ASSISTANT_KEYWORD_PREFIX_AUTO_ADDITION_ENABLED, false);
    }
}
