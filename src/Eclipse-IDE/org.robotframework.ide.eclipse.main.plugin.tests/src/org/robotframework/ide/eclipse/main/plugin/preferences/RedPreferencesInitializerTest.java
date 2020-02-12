/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.PythonInstallationDirectoryFinder.PythonInstallationDirectory;
import org.rf.ide.core.environment.SuiteExecutor;
import org.rf.ide.core.testdata.formatter.RedFormatter.FormattingSeparatorType;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.CellCommitBehavior;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.CellWrappingStrategy;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.FormatterType;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.LibraryPrefixStrategy;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.LinkedModeStrategy;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.MatchingKeywordStrategy;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.SeparatorsMode;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.ElementOpenMode;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory;

public class RedPreferencesInitializerTest {

    @Test
    public void byDefaultEmptyFrameworkPreferencesAreInitialized() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences, ArrayList::new);

        verify(preferences).put(RedPreferences.ACTIVE_INSTALLATION, "{\"interpreter\":null,\"path\":null}");
        verify(preferences).put(RedPreferences.ALL_INSTALLATIONS, "[]");
    }

    @Test
    public void byDefaultNotEmptyFrameworkPreferencesAreInitialized() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);
        final PythonInstallationDirectory dir1 = mock(PythonInstallationDirectory.class);
        when(dir1.getAbsolutePath()).thenReturn("path_to_jython");
        when(dir1.getInterpreter()).thenReturn(SuiteExecutor.Jython);
        final PythonInstallationDirectory dir2 = mock(PythonInstallationDirectory.class);
        when(dir2.getAbsolutePath()).thenReturn("path_to_iron_python");
        when(dir2.getInterpreter()).thenReturn(SuiteExecutor.IronPython);
        final PythonInstallationDirectory dir3 = mock(PythonInstallationDirectory.class);
        when(dir3.getAbsolutePath()).thenReturn("path_to_no_python_dir");
        when(dir3.getInterpreter()).thenReturn(null);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences, () -> newArrayList(dir1, dir2, dir3));

        verify(preferences).put(RedPreferences.ACTIVE_INSTALLATION,
                "{\"interpreter\":\"Jython\",\"path\":\"path_to_jython\"}");
        verify(preferences).put(RedPreferences.ALL_INSTALLATIONS,
                "[{\"interpreter\":\"Jython\",\"path\":\"path_to_jython\"},{\"interpreter\":\"IronPython\",\"path\":\"path_to_iron_python\"},{\"interpreter\":null,\"path\":\"path_to_no_python_dir\"}]");
    }

    @Test
    public void byDefaultAllElementsAreFoldable() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences, ArrayList::new);

        verify(preferences).putInt(RedPreferences.FOLDING_LINE_LIMIT, 2);
        verify(preferences).putBoolean(RedPreferences.FOLDABLE_SECTIONS, true);
        verify(preferences).putBoolean(RedPreferences.FOLDABLE_CASES, true);
        verify(preferences).putBoolean(RedPreferences.FOLDABLE_TASKS, true);
        verify(preferences).putBoolean(RedPreferences.FOLDABLE_KEYWORDS, true);
        verify(preferences).putBoolean(RedPreferences.FOLDABLE_DOCUMENTATION, true);
    }

    @Test
    public void byDefaultAllSyntaxHighlightingCategoryPreferencesAreInitialized() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences, ArrayList::new);

        for (final SyntaxHighlightingCategory category : SyntaxHighlightingCategory.values()) {
            final ColoringPreference preference = category.getDefault();
            verify(preferences).put(category.getPreferenceId(), preference.toPreferenceString());
        }
    }

    @Test
    public void byDefaultAllProblemCategoryPreferencesAreInitialized() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences, ArrayList::new);

        for (final ProblemCategory category : ProblemCategory.values()) {
            verify(preferences).put(category.getId(), category.getDefaultSeverity().name());
        }
    }

    @Test
    public void byDefaultEditorPreferencesAreInitialized() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences, ArrayList::new);

        verify(preferences).putBoolean(RedPreferences.PARENT_DIRECTORY_NAME_IN_TAB, false);
        verify(preferences).putBoolean(RedPreferences.LIBRARY_KEYWORD_HYPERLINKS, true);
        verify(preferences).put(RedPreferences.FILE_ELEMENTS_OPEN_MODE, ElementOpenMode.OPEN_IN_SOURCE.name());
        verify(preferences).putBoolean(RedPreferences.KEYWORD_ARGUMENTS_CELL_COLORING, false);
        verify(preferences).putInt(RedPreferences.MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS, 5);
        verify(preferences).put(RedPreferences.BEHAVIOR_ON_CELL_COMMIT,
                CellCommitBehavior.MOVE_TO_ADJACENT_CELL.name());
        verify(preferences).put(RedPreferences.CELL_WRAPPING, CellWrappingStrategy.SINGLE_LINE_CUT.name());
        verify(preferences).put(RedPreferences.SEPARATOR_MODE, SeparatorsMode.FILE_TYPE_DEPENDENT.name());
        verify(preferences).put(RedPreferences.SEPARATOR_TO_USE, "ssss");
        verify(preferences).putBoolean(RedPreferences.SEPARATOR_JUMP_MODE_ENABLED, false);
        verify(preferences).putBoolean(RedPreferences.VARIABLES_BRACKETS_INSERTION_ENABLED, false);
        verify(preferences).putBoolean(RedPreferences.VARIABLES_BRACKETS_INSERTION_WRAPPING_ENABLED, false);
        verify(preferences).put(RedPreferences.VARIABLES_BRACKETS_INSERTION_WRAPPING_PATTERN, "\\w+");
    }

    @Test
    public void byDefaultAllLaunchConfigurationPreferencesAreInitialized() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences, ArrayList::new);

        verify(preferences).putBoolean(RedPreferences.LAUNCH_USE_ARGUMENT_FILE, true);
        verify(preferences).putBoolean(RedPreferences.LAUNCH_USE_SINGLE_COMMAND_LINE_ARGUMENT, false);
        verify(preferences).putBoolean(RedPreferences.LAUNCH_USE_SINGLE_FILE_DATA_SOURCE, false);
        verify(preferences).putBoolean(RedPreferences.LIMIT_MSG_LOG_OUTPUT, false);
        verify(preferences).putInt(RedPreferences.LIMIT_MSG_LOG_LENGTH, 80_000);
        verify(preferences).put(RedPreferences.LAUNCH_AGENT_CONNECTION_HOST, "127.0.0.1");
        verify(preferences).putInt(RedPreferences.LAUNCH_AGENT_CONNECTION_PORT, 43_981);
        verify(preferences).putInt(RedPreferences.LAUNCH_AGENT_CONNECTION_TIMEOUT, 30);
        verify(preferences).put(RedPreferences.LAUNCH_ENVIRONMENT_VARIABLES, "{\"PYTHONIOENCODING\":\"utf8\"}");
        verify(preferences).put(RedPreferences.DEBUGGER_SUSPEND_ON_ERROR, "prompt");
        verify(preferences).putBoolean(RedPreferences.DEBUGGER_OMIT_LIB_KEYWORDS, false);
    }

    @Test
    public void byDefaultFormatterPreferencesAreInitialized() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences, ArrayList::new);

        verify(preferences).put(RedPreferences.FORMATTER_TYPE, FormatterType.CUSTOM.name());
        verify(preferences).putBoolean(RedPreferences.FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED, false);
        verify(preferences).put(RedPreferences.FORMATTER_SEPARATOR_TYPE, FormattingSeparatorType.CONSTANT.name());
        verify(preferences).putInt(RedPreferences.FORMATTER_SEPARATOR_LENGTH, 4);
        verify(preferences).putBoolean(RedPreferences.FORMATTER_IGNORE_LONG_CELLS_ENABLED, true);
        verify(preferences).putInt(RedPreferences.FORMATTER_LONG_CELL_LENGTH_LIMIT, 40);
        verify(preferences).putBoolean(RedPreferences.FORMATTER_RIGHT_TRIM_ENABLED, false);
    }

    @Test
    public void byDefaultSaveActionsPreferencesAreInitialized() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences, ArrayList::new);

        verify(preferences).putBoolean(RedPreferences.SAVE_ACTIONS_CODE_FORMATTING_ENABLED, false);
        verify(preferences).putBoolean(RedPreferences.SAVE_ACTIONS_CHANGED_LINES_ONLY_ENABLED, false);
        verify(preferences).putBoolean(RedPreferences.SAVE_ACTIONS_AUTO_DISCOVERING_ENABLED, true);
        verify(preferences).putBoolean(RedPreferences.SAVE_ACTIONS_AUTO_DISCOVERING_SUMMARY_WINDOW_ENABLED, false);
    }

    @Test
    public void byDefaultLibrariesPreferencesAreInitialized() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences, ArrayList::new);

        verify(preferences).putBoolean(RedPreferences.PROJECT_MODULES_RECURSIVE_ADDITION_ON_VIRTUALENV_ENABLED, false);
        verify(preferences).putBoolean(RedPreferences.AUTODISCOVERY_GEVENT_SUPPORT, false);
        verify(preferences).putBoolean(RedPreferences.PYTHON_LIBRARIES_LIBDOCS_GENERATION_IN_SEPARATE_PROCESS_ENABLED,
                true);
        verify(preferences).putBoolean(RedPreferences.LIBDOCS_AUTO_RELOAD_ENABLED, true);
    }

    @Test
    public void byDefaultAllContentAssistPreferencesAreInitialized() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences, ArrayList::new);

        verify(preferences).putBoolean(RedPreferences.ASSISTANT_AUTO_INSERT_ENABLED, false);
        verify(preferences).putBoolean(RedPreferences.ASSISTANT_AUTO_ACTIVATION_ENABLED, true);
        verify(preferences).putInt(RedPreferences.ASSISTANT_AUTO_ACTIVATION_DELAY, 100);
        verify(preferences).put(RedPreferences.ASSISTANT_AUTO_ACTIVATION_CHARS, "");
        verify(preferences).putBoolean(RedPreferences.ASSISTANT_KEYWORD_FROM_NOT_IMPORTED_LIBRARY_ENABLED, false);
        verify(preferences).put(RedPreferences.ASSISTANT_KEYWORD_PREFIX_AUTO_ADDITION,
                LibraryPrefixStrategy.AUTOMATIC.name());
        verify(preferences).put(RedPreferences.ASSISTANT_LINKED_ARGUMENTS_MODE, LinkedModeStrategy.EXIT_ON_LAST.name());
        verify(preferences).put(RedPreferences.ASSISTANT_MATCHING_KEYWORD, MatchingKeywordStrategy.FIRST_FOUND.name());
    }

    @Test
    public void byDefaultThereAreNoActiveVariablesSetsNorTheSetsThemselves() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);
        new RedPreferencesInitializer().initializeDefaultPreferences(preferences, ArrayList::new);

        verify(preferences).put(RedPreferences.STRING_VARIABLES_SETS, "");
        verify(preferences).put(RedPreferences.STRING_VARIABLES_ACTIVE_SET, "");
    }

    @Test
    public void byDefaultThereAreNoAdditionalRfRulesFiles() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);
        new RedPreferencesInitializer().initializeDefaultPreferences(preferences, ArrayList::new);

        verify(preferences).put(RedPreferences.RFLINT_RULES_FILES, "");
    }

    @Test
    public void byDefaultNoRfLintRuleIsConfigured() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);
        new RedPreferencesInitializer().initializeDefaultPreferences(preferences, ArrayList::new);

        verify(preferences).put(RedPreferences.RFLINT_RULES_CONFIG_NAMES, "");
        verify(preferences).put(RedPreferences.RFLINT_RULES_CONFIG_SEVERITIES, "");
        verify(preferences).put(RedPreferences.RFLINT_RULES_CONFIG_ARGS, "");
    }

    @Test
    public void byDefaultValidationIsTurnedOn() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences, ArrayList::new);

        verify(preferences).putBoolean(RedPreferences.TURN_OFF_VALIDATION, false);
    }

    @Test
    public void byDefaultTasksAreTurnedOn() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences, ArrayList::new);

        verify(preferences).putBoolean(RedPreferences.TASKS_DETECTION_ENABLED, true);
    }

    @Test
    public void byDefaultTasksAreTaggedWithTodoAndFixmeOfProperPriorities() {
        final IEclipsePreferences preferences = mock(IEclipsePreferences.class);

        new RedPreferencesInitializer().initializeDefaultPreferences(preferences, ArrayList::new);

        verify(preferences).put(RedPreferences.TASKS_TAGS, "FIXME;TODO");
        verify(preferences).put(RedPreferences.TASKS_PRIORITIES, "HIGH;NORMAL");
    }
}
