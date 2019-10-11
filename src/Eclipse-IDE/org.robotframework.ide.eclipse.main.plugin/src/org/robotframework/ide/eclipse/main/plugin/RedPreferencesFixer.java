/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.RGB;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.LibraryPrefixStrategy;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

// TODO: remove this class somewhere in future, see RED-1238
class RedPreferencesFixer {

    // there are modified preferences changed in 0.9.0
    static void updateModifiedPreferencesIfNeeded(final IPreferenceStore store) {

        boolean shouldSave = false;

        for (final String oldBooleanKeywordPrefixPreferenceName : newArrayList(
                "assistantKeywordPrefixAutoAdditionEnabled", "red.editor.assistant.keywordPrefixAutoAdditionEnabled")) {
            if (store.contains(oldBooleanKeywordPrefixPreferenceName)) {
                final boolean oldBooleanKeywordPrefixPreferenceValue = store
                        .getBoolean(oldBooleanKeywordPrefixPreferenceName);
                store.setToDefault(oldBooleanKeywordPrefixPreferenceName);
                if (oldBooleanKeywordPrefixPreferenceValue) {
                    store.setValue(RedPreferences.ASSISTANT_KEYWORD_PREFIX_AUTO_ADDITION,
                            LibraryPrefixStrategy.ALWAYS.name());
                }
                shouldSave = true;
            }
        }

        if (shouldSave && store instanceof IPersistentPreferenceStore) {
            try {
                ((IPersistentPreferenceStore) store).save();
            } catch (final IOException e) {
                // ok, it will not be saved
            }
        }
    }

    // there are preferences written without "red." prefix
    // red prefix was added to all preferences in 0.8.12 to fix preference exporting feature
    static void updatePreferencesWithoutPrefixesIfNeeded(final IPreferenceStore store) {

        boolean shouldSave = false;

        for (final Entry<String, String> entry : createIntegerPreferencesMapping().entrySet()) {
            if (store.contains(entry.getKey())) {
                final int value = store.getInt(entry.getKey());
                store.setToDefault(entry.getKey());
                store.setValue(entry.getValue(), value);
                shouldSave = true;
            }
        }

        for (final Entry<String, String> entry : createBooleanPreferencesMapping().entrySet()) {
            if (store.contains(entry.getKey())) {
                final boolean value = store.getBoolean(entry.getKey());
                store.setToDefault(entry.getKey());
                store.setValue(entry.getValue(), value);
                shouldSave = true;
            }
        }

        for (final Entry<String, String> entry : createStringPreferencesMapping().entrySet()) {
            if (store.contains(entry.getKey())) {
                final String value = store.getString(entry.getKey());
                store.setToDefault(entry.getKey());
                store.setValue(entry.getValue(), value);
                shouldSave = true;
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
                shouldSave = true;
            }
        }

        if (shouldSave && store instanceof IPersistentPreferenceStore) {
            try {
                ((IPersistentPreferenceStore) store).save();
            } catch (final IOException e) {
                // ok, it will not be saved
            }
        }
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
        builder.put("otherRuntimes", RedPreferences.OTHER_RUNTIMES);
        builder.put("activeRuntime", RedPreferences.ACTIVE_RUNTIME);
        builder.put("cellCommitBehavior", RedPreferences.BEHAVIOR_ON_CELL_COMMIT);
        builder.put("separatorMode", RedPreferences.SEPARATOR_MODE);
        builder.put("separatorToUse", RedPreferences.SEPARATOR_TO_USE);
        builder.put("assistantAutoActivationChars", RedPreferences.ASSISTANT_AUTO_ACTIVATION_CHARS);
        return builder.build();
    }
}
