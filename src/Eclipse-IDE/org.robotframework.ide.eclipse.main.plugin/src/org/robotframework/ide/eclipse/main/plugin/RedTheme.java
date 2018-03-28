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
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;

public class RedTheme {

    private static final String TEXT_EDITOR_FONT = "org.eclipse.jface.textfont";

    public static final String RED_SOURCE_EDITOR_FONT = "org.robotframework.red.textfont";
    private static final String RED_TABLE_EDITOR_FONT = "org.robotframework.red.tablefont";

    private static final String ECLIPSE_INFO_BACKGROUND_COLOR = "org.eclipse.ui.workbench.HOVER_BACKGROUND";
    private static final String ECLIPSE_DECORATION_COLOR = "DECORATIONS_COLOR";
    private static final String ECLIPSE_SEARCH_MATCH_COLOR = "org.eclipse.search.ui.match.highlight";

    private static final String ROBOT_CONSOLE_RED_MESSAGES = "org.robotframework.red.robotConsoleRedStream";

    private static final String TABLE_HEADER_GRID_COLOR = "org.robotframework.red.table.header.grid";
    private static final String TABLE_HEADER_BACKGROUND_COLOR = "org.robotframework.red.table.header.background";
    private static final String TABLE_HEADER_FOREGROUND_COLOR = "org.robotframework.red.table.header.foreground";
    private static final String TABLE_HEADER_BACKGROUND_HIGHLIGHTED_COLOR = "org.robotframework.red.table.header.highlighted.background";
    private static final String TABLE_HEADER_UNDERLINE_COLOR = "org.robotframework.red.table.header.underline";

    private static final String TABLE_BODY_GRID_COLOR = "org.robotframework.red.table.body.grid";
    private static final String TABLE_BODY_SELECTION_GRID_COLOR = "org.robotframework.red.table.body.grid.selected";
    private static final String TABLE_BODY_SELECTION_BORDER_COLOR = "org.robotframework.red.table.body.selection.border";
    private static final String TABLE_BODY_FOREGROUND_COLOR = "org.robotframework.red.table.body.foreground";
    private static final String TABLE_BODY_ODD_ROW_BACKGROUND_COLOR = "org.robotframework.red.table.body.row.odd.background";
    private static final String TABLE_BODY_EVEN_ROW_BACKGROUND_COLOR = "org.robotframework.red.table.body.row.even.background";
    private static final String TABLE_HIGHLIGHTED_ROW_COLOR = "org.robotframework.red.table.body.row.highlight";
    private static final String TABLE_HIGHLIGHTED_CELL_COLOR = "org.robotframework.red.table.body.cell.highlight";
    private static final String TABLE_INACTIVE_CELL_BACKGROUND_COLOR = "org.robotframework.red.table.body.cell.inactive.background";
    private static final String TABLE_INACTIVE_CELL_FOREGROUND_COLOR = "org.robotframework.red.table.body.cell.inactive.foreground";


    private static ColorRegistry getColorRegistry() {
        return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
    }

    private static FontRegistry getFontRegistry() {
        return PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry();
    }

    public static class Fonts {

        public static Font getTextEditorFont() {
            return getFontRegistry().get(TEXT_EDITOR_FONT);
        }

        public static Font getRedSourceEditorFont() {
            return getFontRegistry().get(RED_SOURCE_EDITOR_FONT);
        }

        public static Font getTablesEditorFont() {
            return getFontRegistry().get(RED_TABLE_EDITOR_FONT);
        }
    }

    public static class Colors {

        public static RGB getEclipseInfoBackgroundColor() {
            return getColorRegistry().getRGB(ECLIPSE_INFO_BACKGROUND_COLOR);
        }

        public static Color getEclipseDecorationColor() {
            return getColorRegistry().get(ECLIPSE_DECORATION_COLOR);
        }

        public static Color getEclipseSearchMatchColor() {
            return getColorRegistry().get(ECLIPSE_SEARCH_MATCH_COLOR);
        }

        public static Color getRobotConsoleRedStreamColor() {
            return getColorRegistry().get(ROBOT_CONSOLE_RED_MESSAGES);
        }

        public static Color getTableHeaderGridColor() {
            return getColorRegistry().get(TABLE_HEADER_GRID_COLOR);
        }

        public static Color getTableHeaderBackgroundColor() {
            return getColorRegistry().get(TABLE_HEADER_BACKGROUND_COLOR);
        }

        public static Color getTableHeaderForegroundColor() {
            return getColorRegistry().get(TABLE_HEADER_FOREGROUND_COLOR);
        }

        public static Color getTableHeaderHihglightedBackgroundColor() {
            return getColorRegistry().get(TABLE_HEADER_BACKGROUND_HIGHLIGHTED_COLOR);
        }

        public static Color getTableHeaderUnderlineColor() {
            return getColorRegistry().get(TABLE_HEADER_UNDERLINE_COLOR);
        }

        public static Color getTableBodyGridColor() {
            return getColorRegistry().get(TABLE_BODY_GRID_COLOR);
        }

        public static Color getTableBodySelectionGridColor() {
            return getColorRegistry().get(TABLE_BODY_SELECTION_GRID_COLOR);
        }

        public static Color getTableBodySelectionBorderColor() {
            return getColorRegistry().get(TABLE_BODY_SELECTION_BORDER_COLOR);
        }

        public static Color getTableBodyForegroundColor() {
            return getColorRegistry().get(TABLE_BODY_FOREGROUND_COLOR);
        }

        public static Color getTableBodyOddRowBackgroundColor() {
            return getColorRegistry().get(TABLE_BODY_ODD_ROW_BACKGROUND_COLOR);
        }

        public static Color getTableBodyEvenRowBackgroundColor() {
            return getColorRegistry().get(TABLE_BODY_EVEN_ROW_BACKGROUND_COLOR);
        }

        public static Color getTableHighlightedCellColor() {
            return getColorRegistry().get(TABLE_HIGHLIGHTED_CELL_COLOR);
        }

        public static Color getTableHiglihtedRowColor() {
            return getColorRegistry().get(TABLE_HIGHLIGHTED_ROW_COLOR);
        }

        public static Color getTableInactiveCellBackgroundColor() {
            return getColorRegistry().get(TABLE_INACTIVE_CELL_BACKGROUND_COLOR);
        }

        public static Color getTableInactiveCellForegroundColor() {
            return getColorRegistry().get(TABLE_INACTIVE_CELL_FOREGROUND_COLOR);
        }
    }
}
