/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.layer.config.DefaultColumnHeaderStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.nattable.painter.UnderlinedCellBackgroundPainter;
import org.robotframework.red.nattable.painter.UnderlinedCellBackgroundPainter.Side;

/**
 * @author Michal Anglart
 *
 */
public class ColumnHeaderConfiguration extends DefaultColumnHeaderStyleConfiguration {

    public ColumnHeaderConfiguration(final TableTheme theme) {
        this.font = theme.getFont();
        this.bgColor = theme.getHeadersBackground();
        this.fgColor = theme.getHeadersForeground();
        this.cellPainter = new UnderlinedCellBackgroundPainter(new TextPainter(false, false), Side.BOTTOM,
                theme.getHeadersUnderlineColor().getRGB());
    }
}
