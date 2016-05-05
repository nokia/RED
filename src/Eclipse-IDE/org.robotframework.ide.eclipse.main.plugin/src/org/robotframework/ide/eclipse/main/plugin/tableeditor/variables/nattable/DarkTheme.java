/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable.TableThemes.TableTheme;
import org.robotframework.red.graphics.ColorsManager;


/**
 * @author Michal Anglart
 *
 */
public class DarkTheme implements TableTheme {

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
