/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;

/**
 * @author Michal Anglart
 *
 */
public enum SyntaxHighlightingCategory {
    COMMENT("comment", new ColoringPreference(new RGB(192, 192, 192), SWT.NORMAL), new ColoringPreference(new RGB(128, 128, 128), SWT.NORMAL), "Comments"),
    GHERKIN("gherkin", new ColoringPreference(new RGB(128, 128, 64), SWT.BOLD), new ColoringPreference(new RGB(128, 255, 255), SWT.BOLD), "Given/When/And/Then"),
    KEYWORD_CALL("call", new ColoringPreference(new RGB(0, 128, 192), SWT.BOLD), new ColoringPreference(new RGB(148, 100, 1), SWT.BOLD), "Keyword calls"),
    DEFINITION("definition", new ColoringPreference(new RGB(0, 0, 0), SWT.BOLD), new ColoringPreference(new RGB(64, 128, 191), SWT.BOLD), "Keyword/Test Cases definition headers"),
    SECTION_HEADER("section", new ColoringPreference(new RGB(255, 0, 0), SWT.NORMAL), new ColoringPreference(new RGB(233, 97, 148), SWT.NORMAL), "Section headers"),
    SETTING("setting", new ColoringPreference(new RGB(149, 0, 85), SWT.NORMAL), new ColoringPreference(new RGB(255, 255, 128), SWT.NORMAL), "Settings"),
    SPECIAL("special", new ColoringPreference(new RGB(128, 128, 64), SWT.BOLD), new ColoringPreference(new RGB(128, 255, 255), SWT.BOLD), "Special items"),
    DEFAULT_SECTION("default", new ColoringPreference(new RGB(255, 200, 100), SWT.ITALIC), new ColoringPreference(new RGB(113, 193, 2), SWT.ITALIC), "Undefined tables"),
    VARIABLE("var", new ColoringPreference(new RGB(0, 128, 0), SWT.NORMAL), new ColoringPreference(new RGB(160, 245, 46), SWT.NORMAL), "Variables"),
    TASKS("tasks", new ColoringPreference(new RGB(182,115,204), SWT.BOLD), new ColoringPreference(new RGB(150,140,124), SWT.BOLD), "Tasks");

    private String id;

    private ColoringPreference defaultPref;
    
    private ColoringPreference darkPref;

    private String desc;

    private SyntaxHighlightingCategory(final String id, final ColoringPreference defaultPref, final ColoringPreference darkPref, final String desc) {
        this.id = id;
        this.defaultPref = defaultPref;
        this.darkPref = darkPref;
        this.desc = desc;
    }

    public static SyntaxHighlightingCategory fromPreferenceId(final String key) {
        if (COMMENT.getPreferenceId().equals(key)) {
            return COMMENT;

        } else if (GHERKIN.getPreferenceId().equals(key)) {
            return GHERKIN;

        } else if (KEYWORD_CALL.getPreferenceId().equals(key)) {
            return KEYWORD_CALL;

        } else if (DEFINITION.getPreferenceId().equals(key)) {
            return DEFINITION;

        } else if (SECTION_HEADER.getPreferenceId().equals(key)) {
            return SECTION_HEADER;

        } else if (SETTING.getPreferenceId().equals(key)) {
            return SETTING;

        } else if (SPECIAL.getPreferenceId().equals(key)) {
            return SPECIAL;

        } else if (DEFAULT_SECTION.getPreferenceId().equals(key)) {
            return DEFAULT_SECTION;

        } else if (VARIABLE.getPreferenceId().equals(key)) {
            return VARIABLE;

        } else if (TASKS.getPreferenceId().equals(key)) {
            return TASKS;
        } else {
            throw new IllegalStateException("Unrecognized preference key: " + key);
        }
    }

    public String getId() {
        return this.id;
    }

    public String getPreferenceId() {
        return RedPreferences.SYNTAX_COLORING + "." + id;
    }

    public String getShortDescription() {
        return this.desc;
    }

    public ColoringPreference getPreference() {
        return RedPlugin.getDefault().getPreferences().getSyntaxColoring(this);
    }

    public ColoringPreference getDefault() {
        return defaultPref;
    }
    
    public ColoringPreference getDark() {
        return darkPref;
    }

}
