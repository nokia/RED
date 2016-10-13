/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;

/**
 * @author Michal Anglart
 *
 */
public enum SyntaxHighlightingCategory {
    DEFAULT_SECTION("default", new ColoringPreference(new RGB(255, 200, 100), SWT.ITALIC), "Undefined tables"),
    SECTION_HEADER("section", new ColoringPreference(new RGB(255, 0, 0), SWT.NORMAL), "Section headers"),
    COMMENT("comment", new ColoringPreference(new RGB(192, 192, 192), SWT.NORMAL), "Comments"),
    SETTING("setting", new ColoringPreference(new RGB(149, 0, 85), SWT.NORMAL), "Settings"),
    DEFINITION("definition", new ColoringPreference(new RGB(0, 0, 0), SWT.BOLD), "Keyword/Test Cases definition headers"),
    KEYWORD_CALL("call", new ColoringPreference(new RGB(0, 128, 192), SWT.BOLD), "Keyword calls"),
    VARIABLE("var", new ColoringPreference(new RGB(0, 128, 0), SWT.NORMAL), "Variables"),
    GHERKIN("gherkin", new ColoringPreference(new RGB(128, 128, 64), SWT.NORMAL), "Given/When/And/Then");

    private String id;

    private ColoringPreference defaultPref;

    private String desc;

    private SyntaxHighlightingCategory(final String id, final ColoringPreference defaultPref, final String desc) {
        this.id = id;
        this.defaultPref = defaultPref;
        this.desc = desc;
    }

    public String getId() {
        return this.id;
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
}
