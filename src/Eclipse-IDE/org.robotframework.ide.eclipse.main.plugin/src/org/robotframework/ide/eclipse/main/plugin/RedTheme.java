/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.PlatformUI;

public class RedTheme {

    private static final String SECTION_HEADER = "org.robotframework.ide.eclipse.section.header";
    private static final String ECLIPSE_DECORATION_COLOR = "DECORATIONS_COLOR";
    private static final String HIGHLIGHTED_CELL_COLOR = "org.robotframework.ide.eclipse.cell.highlight";
    private static final String HIGHLIGHTED_ROW_COLOR = "org.robotframework.ide.eclipse.row.selected";
    private static final String COMMENTS_IN_TABLES = "org.robotframework.ide.eclipse.comment";
    private static final String VARIABLES_IN_TABLES = "org.robotframework.ide.eclipse.variable";
    private static final String SETTING = "org.robotframework.ide.eclipse.setting";

    private static ColorRegistry getColorRegistry() {
        return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
    }

    public static Color getEclipseDecorationColor() {
        return getColorRegistry().get(ECLIPSE_DECORATION_COLOR);
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

}
