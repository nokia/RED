/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.PlatformUI;

public class RedTheme {

    private static final String TEXT_EDITOR_FONT = "org.eclipse.jface.textfont";
    public static final String RED_SOURCE_EDITOR_FONT = "org.robotframework.ide.textfont";

    private static final String SECTION_HEADER = "org.robotframework.ide.eclipse.section.header";
    private static final String ECLIPSE_DECORATION_COLOR = "DECORATIONS_COLOR";
    private static final String ECLIPSE_SEARCH_MATCH_COLOR = "org.eclipse.search.ui.match.highlight";
    private static final String HIGHLIGHTED_CELL_COLOR = "org.robotframework.ide.eclipse.cell.highlight";
    private static final String HIGHLIGHTED_ROW_COLOR = "org.robotframework.ide.eclipse.row.selected";
    private static final String COMMENTS_IN_TABLES = "org.robotframework.ide.eclipse.comment";
    private static final String VARIABLES_IN_TABLES = "org.robotframework.ide.eclipse.variable";
    private static final String SETTING = "org.robotframework.ide.eclipse.setting";
    private static final String ROBOT_CONSOLE_RED_MESSAGES = "org.robotframework.ide.eclipse.robotConsoleRedStream";


    private static ColorRegistry getColorRegistry() {
        return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
    }

    private static FontRegistry getFontRegistry() {
        return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry();
    }

    public static Color getRobotConsoleRedStreamColor() {
        return getColorRegistry().get(ROBOT_CONSOLE_RED_MESSAGES);
    }

    public static Color getEclipseDecorationColor() {
        return getColorRegistry().get(ECLIPSE_DECORATION_COLOR);
    }

    public static Color getEclipseSearchMatchColor() {
        return getColorRegistry().get(ECLIPSE_SEARCH_MATCH_COLOR);
    }

    public static Color getHighlightedCellColor() {
        return getColorRegistry().get(HIGHLIGHTED_CELL_COLOR);
    }

    public static Color getHiglihtedRowColor() {
        return getColorRegistry().get(HIGHLIGHTED_ROW_COLOR);
    }

    public static Color getCommentsColor() {
        return getColorRegistry().get(COMMENTS_IN_TABLES);
    }

    public static Color getVariableColor() {
        return getColorRegistry().get(VARIABLES_IN_TABLES);
    }

    public static Color getSectionHeaderColor() {
        return getColorRegistry().get(SECTION_HEADER);
    }

    public static Color getSettingColor() {
        return getColorRegistry().get(SETTING);
    }

    public static Font getTextEditorFont() {
        return getFontRegistry().get(TEXT_EDITOR_FONT);
    }
    
    public static Font getRedSourceEditorFont() {
        return getFontRegistry().get(RED_SOURCE_EDITOR_FONT);
    }
}
