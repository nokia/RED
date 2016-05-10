/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.red.graphics.ColorsManager;

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

    private static class DarkTheme implements TableTheme {

        @Override
        public Font getFont() {
            return null;
        }

        @Override
        public Color getGridBorderColor() {
            return ColorsManager.getColor(SWT.COLOR_BLACK);
        }

        @Override
        public Color getHeadersBackground() {
            return ColorsManager.getColor(82, 87, 91);
        }

        @Override
        public Color getHeadersForeground() {
            return ColorsManager.getColor(204, 204, 204);
        }

        @Override
        public Color getHeadersUnderlineColor() {
            return ColorsManager.getColor(SWT.COLOR_BLACK);
        }

        @Override
        public Color getHighlightedHeadersBackground() {
            return ColorsManager.getColor(44, 47, 50);
        }

        @Override
        public Color getBodyBackgroundOddRowBackground() {
            return ColorsManager.getColor(47, 47, 47);
        }

        @Override
        public Color getBodyBackgroundEvenRowBackground() {
            return ColorsManager.getColor(61, 65, 68);
        }

        @Override
        public Color getBodyForeground() {
            return ColorsManager.getColor(204, 204, 204);
        }

        @Override
        public Color getBodySelectionBorderColor() {
            return ColorsManager.getColor(SWT.COLOR_BLACK);
        }

        @Override
        public Color getBodyHoveredCellBackground() {
            return ColorsManager.getColor(40, 40, 40);
        }

        @Override
        public Color getBodyHoveredSelectedCellBackground() {
            return ColorsManager.getColor(30, 30, 30);
        }

        @Override
        public Color getBodySelectedCellBackground() {
            return ColorsManager.getColor(50, 50, 50);
        }

        @Override
        public Color getBodyAnchoredCellBackground() {
            return ColorsManager.getColor(40, 40, 40);
        }
    }

    private static class BrightTheme implements TableTheme {

        @Override
        public Font getFont() {
            return null;
        }

        @Override
        public Color getGridBorderColor() {
            return ColorsManager.getColor(240, 240, 240);
        }

        @Override
        public Color getHeadersBackground() {
            return ColorsManager.getColor(250, 250, 250);
        }

        @Override
        public Color getHeadersForeground() {
            return ColorsManager.getColor(SWT.COLOR_BLACK);
        }

        @Override
        public Color getHeadersUnderlineColor() {
            return ColorsManager.getColor(220, 220, 220);
        }

        @Override
        public Color getHighlightedHeadersBackground() {
            return ColorsManager.getColor(240, 240, 240);
        }

        @Override
        public Color getBodyBackgroundOddRowBackground() {
            return ColorsManager.getColor(SWT.COLOR_WHITE);
        }

        @Override
        public Color getBodyBackgroundEvenRowBackground() {
            return ColorsManager.getColor(250, 250, 250);
        }

        @Override
        public Color getBodyForeground() {
            return ColorsManager.getColor(SWT.COLOR_BLACK);
        }

        @Override
        public Color getBodySelectionBorderColor() {
            return ColorsManager.getColor(SWT.COLOR_BLACK);
        }

        @Override
        public Color getBodyHoveredCellBackground() {
            return RedTheme.getHighlightedCellColor();
        }

        @Override
        public Color getBodyHoveredSelectedCellBackground() {
            return ColorsManager.getColor(255, 240, 170);
        }

        @Override
        public Color getBodySelectedCellBackground() {
            return RedTheme.getHiglihtedRowColor();
        }

        @Override
        public Color getBodyAnchoredCellBackground() {
            return RedTheme.getHighlightedCellColor();
        }
    }
}
