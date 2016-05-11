/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;


/**
 * @author Michal Anglart
 *
 */
public class GeneralTableConfiguration extends DefaultNatTableStyleConfiguration {

    public GeneralTableConfiguration(final TableTheme theme, final TextPainter textPainter) {
        this.bgColor = theme.getBodyBackgroundOddRowBackground();
        this.fgColor = theme.getBodyForeground();
        this.hAlign = HorizontalAlignmentEnum.LEFT;
        this.vAlign = VerticalAlignmentEnum.TOP;
        this.cellPainter = new PaddingDecorator(textPainter, 2, 2, 2, 5);
    }
}
