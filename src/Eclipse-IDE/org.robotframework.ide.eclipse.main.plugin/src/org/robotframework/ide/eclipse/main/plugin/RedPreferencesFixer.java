/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.RGB;
import org.rf.ide.core.environment.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.LibraryPrefixStrategy;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotEnvironments;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotEnvironments.InterpreterWithPath;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Streams;

// TODO: remove this class somewhere in future, see RED-1238
class RedPreferencesFixer {

    private static final List<PreferenceFixer> FIXERS = new ArrayList<>();
    static {
        // red prefix was added to all preferences in 0.8.12 to fix preference exporting feature
        FIXERS.add(createOldNamesFixer());

        // assistant preferences were changed in 0.9.0
        FIXERS.add(createAssistantAutoAdditionFixer());

        // interpreter preferences were changed in 0.9.4
        FIXERS.add(createInstalledFrameworkFixer());
    }

    @FunctionalInterface
    private static interface PreferenceFixer {

        boolean fixPreferences(IPreferenceStore store);
    }

    static void updateModifiedPreferencesIfNeeded(final IPreferenceStore store) {
        boolean shouldSave = false;
        for (final PreferenceFixer fixer : FIXERS) {
            final boolean preferenceWasUpdated = fixer.fixPreferences(store);
            shouldSave = shouldSave || preferenceWasUpdated;
        }

        if (shouldSave && store instanceof IPersistentPreferenceStore) {
            try {
                ((IPersistentPreferenceStore) store).save();
            } catch (final IOException e) {
                // ok, it will not be saved
            }
        }
    }

    private static PreferenceFixer createInstalledFrameworkFixer() {
        return store -> {
            boolean wasUpdated = false;

            if (store.contains("red.activeRuntime")) {
                final InterpreterWithPath installation;
                if (store.contains("red.activeRuntimeExec")) {
                    final String path = store.getString("red.activeRuntime");
                    final String exec = store.getString("red.activeRuntimeExec");
                    installation = new InterpreterWithPath(exec.isEmpty() ? null : SuiteExecutor.valueOf(exec),
                            path.isEmpty() ? null : path);
                } else {
                    installation = new InterpreterWithPath();
                }

                store.setToDefault("red.activeRuntime");
                store.setToDefault("red.activeRuntimeExec");
                store.putValue(RedPreferences.ACTIVE_INSTALLATION,
                        InstalledRobotEnvironments.writeInstallation(installation));

                wasUpdated = true;
            }

            if (store.contains("red.otherRuntimes")) {
                final List<InterpreterWithPath> installations;
                if (store.contains("red.otherRuntimesExecs")) {
                    final List<String> paths = Splitter.on(';').splitToList(store.getString("red.otherRuntimes"));
                    final List<String> execs = Splitter.on(';').splitToList(store.getString("red.otherRuntimesExecs"));
                    installations = Streams.zip(paths.stream(), execs.stream(),
                            (path, exec) -> new InterpreterWithPath(exec.isEmpty() ? null : SuiteExecutor.valueOf(exec),
                                    path.isEmpty() ? null : path))
                            .collect(Collectors.toList());
                } else {
                    installations = new ArrayList<>();
                }

                store.setToDefault("red.otherRuntimes");
                store.setToDefault("red.otherRuntimesExecs");
                store.putValue(RedPreferences.ALL_INSTALLATIONS,
                        InstalledRobotEnvironments.writeInstallations(installations));

                wasUpdated = true;
            }

            return wasUpdated;
        };
    }

    private static PreferenceFixer createAssistantAutoAdditionFixer() {
        return store -> {
            boolean wasUpdated = false;

            if (store.contains("red.editor.assistant.keywordPrefixAutoAdditionEnabled")) {
                final boolean oldBooleanKeywordPrefixPreferenceValue = store
                        .getBoolean("red.editor.assistant.keywordPrefixAutoAdditionEnabled");
                store.setToDefault("red.editor.assistant.keywordPrefixAutoAdditionEnabled");
                if (oldBooleanKeywordPrefixPreferenceValue) {
                    store.setValue(RedPreferences.ASSISTANT_KEYWORD_PREFIX_AUTO_ADDITION,
                            LibraryPrefixStrategy.ALWAYS.name());
                }
                wasUpdated = true;
            }

            return wasUpdated;
        };
    }

    private static PreferenceFixer createOldNamesFixer() {
        return store -> {
            boolean wasUpdated = false;

            for (final Entry<String, String> entry : createIntegerPreferencesMapping().entrySet()) {
                if (store.contains(entry.getKey())) {
                    final int value = store.getInt(entry.getKey());
                    store.setToDefault(entry.getKey());
                    store.setValue(entry.getValue(), value);
                    wasUpdated = true;
                }
            }

            for (final Entry<String, String> entry : createBooleanPreferencesMapping().entrySet()) {
                if (store.contains(entry.getKey())) {
                    final boolean value = store.getBoolean(entry.getKey());
                    store.setToDefault(entry.getKey());
                    store.setValue(entry.getValue(), value);
                    wasUpdated = true;
                }
            }

            for (final Entry<String, String> entry : createStringPreferencesMapping().entrySet()) {
                if (store.contains(entry.getKey())) {
                    final String value = store.getString(entry.getKey());
                    store.setToDefault(entry.getKey());
                    store.setValue(entry.getValue(), value);
                    wasUpdated = true;
                }
            }

            for (final SyntaxHighlightingCategory category : SyntaxHighlightingCategory.values()) {
                if (store.contains("syntaxColoring." + category.getId() + ".fontStyle")
                        || store.contains("syntaxColoring." + category.getId() + ".color.r")
                        || store.contains("syntaxColoring." + category.getId() + ".color.g")
                        || store.contains("syntaxColoring." + category.getId() + ".color.b")) {

                    final int fontStyle = store.contains("syntaxColoring." + category.getId() + ".fontStyle")
                            ? store.getInt("syntaxColoring." + category.getId() + ".fontStyle")
                            : category.getDefault().getFontStyle();
                    final int red = store.contains("syntaxColoring." + category.getId() + ".color.r")
                            ? store.getInt("syntaxColoring." + category.getId() + ".color.r")
                            : category.getDefault().getColor().getRed();
                    final int green = store.contains("syntaxColoring." + category.getId() + ".color.g")
                            ? store.getInt("syntaxColoring." + category.getId() + ".color.g")
                            : category.getDefault().getColor().getGreen();
                    final int blue = store.contains("syntaxColoring." + category.getId() + ".color.b")
                            ? store.getInt("syntaxColoring." + category.getId() + ".color.b")
                            : category.getDefault().getColor().getBlue();

                    final ColoringPreference preference = new ColoringPreference(new RGB(red, green, blue), fontStyle);

                    store.setToDefault("syntaxColoring." + category.getId() + ".fontStyle");
                    store.setToDefault("syntaxColoring." + category.getId() + ".color.r");
                    store.setToDefault("syntaxColoring." + category.getId() + ".color.g");
                    store.setToDefault("syntaxColoring." + category.getId() + ".color.b");
                    store.setValue(category.getPreferenceId(), preference.toPreferenceString());
                    wasUpdated = true;
                }
            }

            return wasUpdated;
        };
    }

    private static Map<String, String> createIntegerPreferencesMapping() {
        final Builder<String, String> builder = ImmutableMap.builder();
        builder.put("minimalArgsColumns", RedPreferences.MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS);
        builder.put("assistantAutoActivationDelay", RedPreferences.ASSISTANT_AUTO_ACTIVATION_DELAY);
        builder.put("foldingLineLimit", RedPreferences.FOLDING_LINE_LIMIT);
        return builder.build();
    }

    private static Map<String, String> createBooleanPreferencesMapping() {
        final Builder<String, String> builder = ImmutableMap.builder();
        builder.put("projectModulesRecursiveAdditionOnVirtualenvEnabled",
                RedPreferences.PROJECT_MODULES_RECURSIVE_ADDITION_ON_VIRTUALENV_ENABLED);
        builder.put("pythonLibrariesLibdocsGenarationInSeperateProcessEnabled",
                RedPreferences.PYTHON_LIBRARIES_LIBDOCS_GENERATION_IN_SEPARATE_PROCESS_ENABLED);
        builder.put("foldableSections", RedPreferences.FOLDABLE_SECTIONS);
        builder.put("foldableCases", RedPreferences.FOLDABLE_CASES);
        builder.put("foldableTasks", RedPreferences.FOLDABLE_TASKS);
        builder.put("foldableKeywords", RedPreferences.FOLDABLE_KEYWORDS);
        builder.put("foldableDocumentation", RedPreferences.FOLDABLE_DOCUMENTATION);
        builder.put("assistantAutoActivationEnabled", RedPreferences.ASSISTANT_AUTO_ACTIVATION_ENABLED);
        return builder.build();
    }

    private static Map<String, String> createStringPreferencesMapping() {
        final Builder<String, String> builder = ImmutableMap.builder();
        builder.put("otherRuntimes", "red.otherRuntimes");
        builder.put("activeRuntime", "red.activeRuntime");
        builder.put("cellCommitBehavior", RedPreferences.BEHAVIOR_ON_CELL_COMMIT);
        builder.put("separatorMode", RedPreferences.SEPARATOR_MODE);
        builder.put("separatorToUse", RedPreferences.SEPARATOR_TO_USE);
        builder.put("assistantAutoActivationChars", RedPreferences.ASSISTANT_AUTO_ACTIVATION_CHARS);
        builder.put("assistantKeywordPrefixAutoAdditionEnabled",
                "red.editor.assistant.keywordPrefixAutoAdditionEnabled");
        return builder.build();
    }
}
