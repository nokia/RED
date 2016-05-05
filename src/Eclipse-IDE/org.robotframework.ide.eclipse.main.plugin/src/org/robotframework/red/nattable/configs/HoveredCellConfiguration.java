/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable.TableThemes.TableTheme;

/**
 * @author Michal Anglart
 *
 */
public class HoveredCellConfiguration extends AbstractRegistryConfiguration {

    private final TableTheme theme;

    public HoveredCellConfiguration(final TableTheme theme) {
        this.theme = theme;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        final ConfigAttribute<IStyle> attribute = CellConfigAttributes.CELL_STYLE;
        configRegistry.registerConfigAttribute(attribute, createHoverStyle(), DisplayMode.HOVER);
        configRegistry.registerConfigAttribute(attribute, createSelectedHoverStyle(), DisplayMode.SELECT_HOVER);
    }

    private Style createSelectedHoverStyle() {
        final Style style = new Style();
        style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, theme.getBodyHoveredSelectedCellBackground());
        return style;
    }

    private Style createHoverStyle() {
        final Style style = new Style();
        style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, theme.getBodyHoveredCellBackground());
        return style;
    }
}