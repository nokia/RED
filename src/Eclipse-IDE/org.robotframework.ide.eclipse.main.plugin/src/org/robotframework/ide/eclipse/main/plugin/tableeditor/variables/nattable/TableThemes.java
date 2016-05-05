/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;

/**
 * @author Michal Anglart
 *
 */
public class TableThemes {

    private static final int LIMIT = 130;

    static TableTheme getTheme(final RGB backgroundInUse) {
        return isDarkColor(backgroundInUse) ? new DarkTheme() : new BrightTheme();
    }

    private static boolean isDarkColor(final RGB color) {
        return calculatePerceivedBrightness(color) < LIMIT;
    }

    // the formula is referenced in the internet in topics regarding perceived brightness
    private static int calculatePerceivedBrightness(final RGB color) {
        final int r = color.red;
        final int g = color.green;
        final int b = color.blue;
        return (int) Math.sqrt(r * r * .299 + g * g * .587 + b * b * .114);
    }

    public interface TableTheme {

        public Font getFont();
        public Color getGridBorderColor();

        public Color getHeadersBackground();
        public Color getHeadersForeground();
        public Color getHeadersUnderlineColor();
        public Color getHighlightedHeadersBackground();

        public Color getBodyBackgroundOddRowBackground();
        public Color getBodyBackgroundEvenRowBackground();
        public Color getBodyForeground();
        
        public Color getBodySelectionBorderColor();
        public Color getBodyHoveredCellBackground();
        public Color getBodySelectedCellBackground();
        public Color getBodyHoveredSelectedCellBackground();
        public Color getBodyAnchoredCellBackground();

    }
}
