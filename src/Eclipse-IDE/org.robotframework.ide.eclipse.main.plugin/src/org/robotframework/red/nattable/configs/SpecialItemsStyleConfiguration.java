/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.stream.Stream;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;

/**
 * @author lwlodarc
 *
 */
public class SpecialItemsStyleConfiguration extends RobotElementsStyleConfiguration {

    public SpecialItemsStyleConfiguration(final TableTheme theme) {
        super(theme, RedPlugin.getDefault().getPreferences());
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        final Style specialStyle = createStyle(SyntaxHighlightingCategory.SPECIAL);

        Stream.of(DisplayMode.NORMAL, DisplayMode.HOVER, DisplayMode.SELECT, DisplayMode.SELECT_HOVER).forEach(mode -> {
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, specialStyle, mode,
                    SpecialItemsLabelAccumulator.SPECIAL_ITEM_CONFIG_LABEL);
        });
    }
}