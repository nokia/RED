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
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;

/**
 * @author Michal Anglart
 *
 */
public class HoveredCellStyleConfiguration extends AbstractRegistryConfiguration {

    private final TableTheme theme;

    public HoveredCellStyleConfiguration(final TableTheme theme) {
        this.theme = theme;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, createHoverStyle(), DisplayMode.HOVER);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, createSelectedHoverStyle(),
                DisplayMode.SELECT_HOVER);
    }

    private Style createSelectedHoverStyle() {
        return createStyle(CellStyleAttributes.BACKGROUND_COLOR, theme.getBodyHoveredSelectedCellBackground());
    }

    private Style createHoverStyle() {
        return createStyle(CellStyleAttributes.BACKGROUND_COLOR, theme.getBodyHoveredCellBackground());
    }

    private <T> Style createStyle(final ConfigAttribute<T> attribute, final T value) {
        final Style style = new Style();
        style.setAttributeValue(attribute, value);
        return style;
    }
}