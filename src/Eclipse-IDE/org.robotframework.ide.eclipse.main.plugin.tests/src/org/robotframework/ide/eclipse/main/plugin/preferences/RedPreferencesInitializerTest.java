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
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.CellCommitBehavior;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.CellWrappingStrategy;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.SeparatorsMode;
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
            final ColoringPreference preference = category.getDefault();
            verify(preferences).put(category.getPreferenceId(), preference.toPreferenceString());
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
    public void byDefaultEditorPreferencesAreInitialized() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences);

        verify(preferences).putBoolean(RedPreferences.PARENT_DIRECTORY_NAME_IN_TAB, false);
        verify(preferences).put(RedPreferences.FILE_ELEMENTS_OPEN_MODE, ElementOpenMode.OPEN_IN_SOURCE.name());
        verify(preferences).put(RedPreferences.SEPARATOR_MODE, SeparatorsMode.FILE_TYPE_DEPENDENT.name());
        verify(preferences).put(RedPreferences.SEPARATOR_TO_USE, "ssss");
        verify(preferences).putInt(RedPreferences.MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS, 5);
        verify(preferences).put(RedPreferences.BEHAVIOR_ON_CELL_COMMIT,
                CellCommitBehavior.MOVE_TO_ADJACENT_CELL.name());
        verify(preferences).put(RedPreferences.CELL_WRAPPING, CellWrappingStrategy.SINGLE_LINE_CUT.name());
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
        verify(preferences).putBoolean(RedPreferences.LIMIT_MSG_LOG_OUTPUT, false);
        verify(preferences).putInt(RedPreferences.LIMIT_MSG_LOG_LENGTH, 80_000);
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

    @Test
    public void byDefaultThereAreNoAdditionalRfRulesFiles() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);
        new RedPreferencesInitializer().initializeDefaultPreferences(preferences);

        verify(preferences).put(RedPreferences.RFLINT_RULES_FILES, "");
    }

    @Test
    public void byDefaultNoRfLintRuleIsConfigured() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);
        new RedPreferencesInitializer().initializeDefaultPreferences(preferences);

        verify(preferences).put(RedPreferences.RFLINT_RULES_CONFIG_NAMES, "");
        verify(preferences).put(RedPreferences.RFLINT_RULES_CONFIG_SEVERITIES, "");
        verify(preferences).put(RedPreferences.RFLINT_RULES_CONFIG_ARGS, "");
    }
}
