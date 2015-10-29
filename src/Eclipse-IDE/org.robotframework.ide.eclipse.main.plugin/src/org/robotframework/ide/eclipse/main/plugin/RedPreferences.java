/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.RGB;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.RedCompletionBuilder.AcceptanceMode;

public class RedPreferences {
    
    private final IPreferenceStore store;

    RedPreferences(final IPreferenceStore store) {
        this.store = store;
    }

    public static final String OTHER_RUNTIMES = "otherRuntimes";
    public static final String ACTIVE_RUNTIME = "activeRuntime";

    public static final String MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS = "minimalArgsColumns";

    public static final String ASSISTANT_COMPLETION_MODE = "assistantCompletionMode";

    public static final String SYNTAX_COLORING_PREFIX = "syntaxColoring.";

    public String getActiveRuntime() {
        return store.getString(ACTIVE_RUNTIME);
    }
    
    public String getAllRuntimes() {
        return store.getString(OTHER_RUNTIMES);
    }
    
    public int getMimalNumberOfArgumentColumns() {
        return store.getInt(MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS);
    }

    public AcceptanceMode getAcceptanceMode() {
        return AcceptanceMode.valueOf(store.getString(ASSISTANT_COMPLETION_MODE));
    }

    public ColoringPreference getSyntaxColoring(final SyntaxHighlightingCategory category) {
        final int fontStyle = store.getInt(SYNTAX_COLORING_PREFIX + category.getId() + ".fontStyle");
        final int red = store.getInt(SYNTAX_COLORING_PREFIX + category.getId() + ".color.r");
        final int green = store.getInt(SYNTAX_COLORING_PREFIX + category.getId() + ".color.g");
        final int blue = store.getInt(SYNTAX_COLORING_PREFIX + category.getId() + ".color.b");
        return new ColoringPreference(new RGB(red, green, blue), fontStyle);
    }
}
