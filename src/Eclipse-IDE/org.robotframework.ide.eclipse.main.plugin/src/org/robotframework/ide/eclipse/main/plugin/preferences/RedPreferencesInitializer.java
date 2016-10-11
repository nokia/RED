/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.PythonInstallationDirectory;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.CellCommitBehavior;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.SeparatorsMode;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionBuilder.AcceptanceMode;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

public class RedPreferencesInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        initializeDefaultPreferences(DefaultScope.INSTANCE.getNode(RedPlugin.PLUGIN_ID));
    }

    @VisibleForTesting
    void initializeDefaultPreferences(final IEclipsePreferences preferences) {
        initializeFrameworkPreferences(preferences);
        initializeEditorPreferences(preferences);
        initializeSourceFoldingPreferences(preferences);
        initializeSourceEditorAssistantPreferences(preferences);
        initializeSyntaxColoringPreferences(preferences);
        initializeAutodiscoveringPreferences(preferences);
    }

    private void initializeFrameworkPreferences(final IEclipsePreferences preferences) {
        final List<PythonInstallationDirectory> pybotPaths = RobotRuntimeEnvironment.whereArePythonInterpreters();
        if (!pybotPaths.isEmpty()) {
            final String activePath = pybotPaths.get(0).getAbsolutePath();
            final String allPaths = Joiner.on(';').join(
                    Iterables.transform(pybotPaths, new Function<PythonInstallationDirectory, String>() {
                        @Override
                        public String apply(final PythonInstallationDirectory dir) {
                            return dir.getAbsolutePath();
                        }
                    }));
            
            preferences.put(RedPreferences.ACTIVE_RUNTIME, activePath);
            preferences.put(RedPreferences.OTHER_RUNTIMES, allPaths);
        }
    }

    private void initializeEditorPreferences(final IEclipsePreferences preferences) {
        preferences.put(RedPreferences.SEPARATOR_MODE, SeparatorsMode.FILETYPE_DEPENDENT.name());
        preferences.put(RedPreferences.SEPARATOR_TO_USE, "ssss");
        preferences.putInt(RedPreferences.MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS, 5);
        preferences.put(RedPreferences.BEHAVIOR_ON_CELL_COMMIT, CellCommitBehavior.MOVE_TO_ADJACENT_CELL.name());
    }

    private void initializeSourceFoldingPreferences(final IEclipsePreferences preferences) {
        preferences.putInt(RedPreferences.FOLDING_LINE_LIMIT, 2);
        preferences.putBoolean(RedPreferences.FOLDABLE_SECTIONS, true);
        preferences.putBoolean(RedPreferences.FOLDABLE_CASES, true);
        preferences.putBoolean(RedPreferences.FOLDABLE_KEYWORDS, true);
        preferences.putBoolean(RedPreferences.FOLDABLE_DOCUMENTATION, true);
    }

    private void initializeSourceEditorAssistantPreferences(final IEclipsePreferences preferences) {
        preferences.put(RedPreferences.ASSISTANT_COMPLETION_MODE, AcceptanceMode.INSERT.name());
        preferences.putBoolean(RedPreferences.ASSISTANT_AUTO_ACTIVATION_ENABLED, true);
        preferences.putInt(RedPreferences.ASSISTANT_AUTO_ACTIVATION_DELAY, 100);
        preferences.put(RedPreferences.ASSISTANT_AUTO_ACTIVATION_CHARS, "");
        preferences.putBoolean(RedPreferences.ASSISTANT_KEYWORD_PREFIX_AUTO_ADDITION_ENABLED, false);
    }

    private void initializeSyntaxColoringPreferences(final IEclipsePreferences preferences) {
        for (final SyntaxHighlightingCategory category : EnumSet.allOf(SyntaxHighlightingCategory.class)) {
            final ColoringPreference defaultPref = category.getDefault();

            preferences.putInt(getFontStyleIdentifierFor(category), defaultPref.getFontStyle());
            preferences.putInt(getRedFactorIdentifierFor(category), defaultPref.getRgb().red);
            preferences.putInt(getGreenFactorIdentifierFor(category), defaultPref.getRgb().green);
            preferences.putInt(getBlueFactorIdentifierFor(category), defaultPref.getRgb().blue);
        }
    }
    
    private void initializeAutodiscoveringPreferences(final IEclipsePreferences preferences) {
        preferences.putBoolean(RedPreferences.PROJECT_MODULES_RECURSIVE_ADDITION_ON_VIRTUALENV_ENABLED, false);
    }

    static String getFontStyleIdentifierFor(final SyntaxHighlightingCategory category) {
        return RedPreferences.SYNTAX_COLORING_PREFIX + category.getId() + ".fontStyle";
    }

    static String getRedFactorIdentifierFor(final SyntaxHighlightingCategory category) {
        return RedPreferences.SYNTAX_COLORING_PREFIX + category.getId() + ".color.r";
    }

    static String getGreenFactorIdentifierFor(final SyntaxHighlightingCategory category) {
        return RedPreferences.SYNTAX_COLORING_PREFIX + category.getId() + ".color.g";
    }

    static String getBlueFactorIdentifierFor(final SyntaxHighlightingCategory category) {
        return RedPreferences.SYNTAX_COLORING_PREFIX + category.getId() + ".color.b";
    }
}
