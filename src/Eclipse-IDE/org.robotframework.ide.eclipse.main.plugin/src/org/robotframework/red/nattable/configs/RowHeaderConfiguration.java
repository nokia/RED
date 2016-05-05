/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.layer.config.DefaultRowHeaderStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable.TableThemes.TableTheme;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.nattable.configs.UnderlinedCellBackgroundPainter.Side;


/**
 * @author Michal Anglart
 *
 */
public class RowHeaderConfiguration extends DefaultRowHeaderStyleConfiguration {

    public RowHeaderConfiguration(final TableTheme theme) {
        this.font = theme.getFont();
        this.borderStyle = new BorderStyle(0, ColorsManager.getColor(255, 0, 0), LineStyleEnum.SOLID);
        this.bgColor = theme.getHeadersBackground();
        this.fgColor = theme.getHeadersForeground();
        this.cellPainter = new UnderlinedCellBackgroundPainter(new TextPainter(false, false), Side.RIGHT,
                theme.getHeadersUnderlineColor().getRGB());
    }
}
