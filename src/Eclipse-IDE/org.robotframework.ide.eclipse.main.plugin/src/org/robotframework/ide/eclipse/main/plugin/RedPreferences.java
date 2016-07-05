/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionBuilder.AcceptanceMode;
import org.robotframework.red.graphics.ColorsManager;

public class RedPreferences {
    
    private final IPreferenceStore store;

    RedPreferences(final IPreferenceStore store) {
        this.store = store;
    }

    public static final String OTHER_RUNTIMES = "otherRuntimes";
    public static final String ACTIVE_RUNTIME = "activeRuntime";

    public static final String SEPARATOR_MODE = "separatorMode";
    public static final String SEPARATOR_TO_USE = "separatorToUse";
    public static final String MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS = "minimalArgsColumns";
    public static final String BEHAVIOR_ON_CELL_COMMIT = "cellCommitBehavior";

    public static final String ASSISTANT_COMPLETION_MODE = "assistantCompletionMode";
    public static final String ASSISTANT_AUTO_ACTIVATION_ENABLED = "assistantAutoActivationEnabled";
    public static final String ASSISTANT_AUTO_ACTIVATION_DELAY = "assistantAutoActivationDelay";
    public static final String ASSISTANT_AUTO_ACTIVATION_CHARS = "assistantAutoActivationChars";
    public static final String ASSISTANT_KEYWORD_PREFIX_AUTO_ADDITION_ENABLED = "assistantKeywordPrefixAutoAdditionEnabled";

    public static final String SYNTAX_COLORING_PREFIX = "syntaxColoring.";

    public String getActiveRuntime() {
        return store.getString(ACTIVE_RUNTIME);
    }
    
    public String getAllRuntimes() {
        return store.getString(OTHER_RUNTIMES);
    }
    
    public SeparatorsMode getSeparatorsMode() {
        return SeparatorsMode.valueOf(store.getString(SEPARATOR_MODE));
    }

    public String getSeparatorToUse(final boolean isTsvFile) {
        final SeparatorsMode mode = getSeparatorsMode();
        switch (mode) {
            case ALWAYS_TABS:
                return "\t";
            case ALWAYS_USER_DEFINED_SEPARATOR:
                return store.getString(SEPARATOR_TO_USE).replaceAll("t", "\t").replaceAll("s", " ");
            case FILETYPE_DEPENDENT:
                if (isTsvFile) {
                    return "\t";
                } else {
                    return store.getString(SEPARATOR_TO_USE).replaceAll("t", "\t").replaceAll("s", " ");
                }
            default:
                throw new IllegalStateException("Unrecognized separators mode: " + mode.toString());
        }
    }

    public int getMimalNumberOfArgumentColumns() {
        return store.getInt(MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS);
    }

    public CellCommitBehavior getCellCommitBehavior() {
        return CellCommitBehavior.valueOf(store.getString(BEHAVIOR_ON_CELL_COMMIT));
    }

    public AcceptanceMode getAssistantAcceptanceMode() {
        return AcceptanceMode.valueOf(store.getString(ASSISTANT_COMPLETION_MODE));
    }

    public boolean isAssistantAutoActivationEnabled() {
        return store.getBoolean(ASSISTANT_AUTO_ACTIVATION_ENABLED);
    }

    public int getAssistantAutoActivationDelay() {
        return store.getInt(ASSISTANT_AUTO_ACTIVATION_DELAY);
    }

    public char[] getAssistantAutoActivationChars() {
        return store.getString(ASSISTANT_AUTO_ACTIVATION_CHARS).toCharArray();
    }
    
    public boolean isAssistantKeywordPrefixAutoAdditionEnabled() {
        return store.getBoolean(ASSISTANT_KEYWORD_PREFIX_AUTO_ADDITION_ENABLED);
    }

    public ColoringPreference getSyntaxColoring(final SyntaxHighlightingCategory category) {
        final int fontStyle = store.getInt(SYNTAX_COLORING_PREFIX + category.getId() + ".fontStyle");
        final int red = store.getInt(SYNTAX_COLORING_PREFIX + category.getId() + ".color.r");
        final int green = store.getInt(SYNTAX_COLORING_PREFIX + category.getId() + ".color.g");
        final int blue = store.getInt(SYNTAX_COLORING_PREFIX + category.getId() + ".color.b");
        return new ColoringPreference(new RGB(red, green, blue), fontStyle);
    }

    public static class ColoringPreference {

        private final RGB color;

        private final int fontStyle;

        public ColoringPreference(final RGB color, final int fontStyle) {
            this.color = color;
            this.fontStyle = fontStyle;
        }

        public Color getColor() {
            return ColorsManager.getColor(color);
        }

        public int getFontStyle() {
            return fontStyle;
        }

        public RGB getRgb() {
            return color;
        }
    }

    public enum SeparatorsMode {
        ALWAYS_TABS,
        ALWAYS_USER_DEFINED_SEPARATOR,
        FILETYPE_DEPENDENT
    }

    public enum CellCommitBehavior {
        STAY_IN_SAME_CELL,
        MOVE_TO_ADJACENT_CELL
    }
}
