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
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;

/**
 * @author lwlodarc
 *
 */
public class SettingsItemsStyleConfiguration extends RobotElementsStyleConfiguration {

    public SettingsItemsStyleConfiguration(final TableTheme theme) {
        super(theme);
    }

    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {
        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();

        final Style variableStyle = createStyle(preferences, SyntaxHighlightingCategory.SETTING);

        Stream.of(DisplayMode.NORMAL, DisplayMode.HOVER, DisplayMode.SELECT, DisplayMode.SELECT_HOVER).forEach(mode -> {
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, variableStyle, mode,
                    SettingsItemsLabelAccumulator.SETTING_CONFIG_LABEL);
        });
    }
}