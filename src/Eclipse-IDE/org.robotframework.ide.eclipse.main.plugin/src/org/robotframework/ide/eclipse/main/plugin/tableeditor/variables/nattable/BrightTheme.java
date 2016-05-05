/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable.TableThemes.TableTheme;
import org.robotframework.red.graphics.ColorsManager;


/**
 * @author Michal Anglart
 *
 */
public class BrightTheme implements TableTheme {

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
