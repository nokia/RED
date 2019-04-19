/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.stream.Stream;

import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.jface.viewers.Stylers;

/**
 * @author lwlodarc
 *
 */
public abstract class RobotElementsStyleConfiguration extends AbstractRegistryConfiguration {

    private final TableTheme theme;

    final RedPreferences preferences;

    RobotElementsStyleConfiguration(final TableTheme theme, final RedPreferences preferences) {
        this.theme = theme;
        this.preferences = preferences;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        final Style style = createElementStyle();
        final String configLabel = getConfigLabel();

        Stream.of(DisplayMode.NORMAL, DisplayMode.HOVER, DisplayMode.SELECT, DisplayMode.SELECT_HOVER).forEach(mode -> {
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, mode, configLabel);
        });
    }

    abstract String getConfigLabel();

    abstract Style createElementStyle();

    Style createStyle(final SyntaxHighlightingCategory category) {
        final ColoringPreference syntaxColoring = preferences.getSyntaxColoring(category);

        final Style style = new Style();
        style.setAttributeValue(CellStyleAttributes.FONT,
                FontsManager.transformFontWithStyle(theme.getFont(), syntaxColoring.getFontStyle()));
        style.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, syntaxColoring.getColor());
        return style;
    }

    Styler createStyler(final SyntaxHighlightingCategory category) {
        final ColoringPreference syntaxColoring = preferences.getSyntaxColoring(category);

        return Stylers.mixingStyler(Stylers.withForeground(syntaxColoring.getRgb()),
                Stylers.withFontStyle(syntaxColoring.getFontStyle()));
    }
}
