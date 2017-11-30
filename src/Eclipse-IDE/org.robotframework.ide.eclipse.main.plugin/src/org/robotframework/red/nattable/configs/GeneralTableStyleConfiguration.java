/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.VerticalAlignmentEnum;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;


/**
 * @author Michal Anglart
 *
 */
public class GeneralTableStyleConfiguration extends DefaultNatTableStyleConfiguration {

    public GeneralTableStyleConfiguration(final TableTheme theme, final TextPainter textPainter) {
        this.font = theme.getFont();
        this.bgColor = theme.getBodyOddRowBackground();
        this.fgColor = theme.getBodyForeground();
        this.hAlign = HorizontalAlignmentEnum.LEFT;
        this.vAlign = VerticalAlignmentEnum.TOP;
        this.cellPainter = new PaddingDecorator(textPainter, 2, 2, 2, 5);
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        super.configureRegistry(configRegistry);

        configRegistry.registerConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, new DefaultDisplayConverter() {
            @Override
            public Object displayToCanonicalValue(final Object destinationValue) {
                return destinationValue == null ? null : destinationValue.toString();
            }
        });
    }
}
