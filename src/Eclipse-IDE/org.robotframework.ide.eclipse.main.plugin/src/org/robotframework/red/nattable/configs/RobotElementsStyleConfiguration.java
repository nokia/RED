/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.graphics.FontsManager;

/**
 * @author lwlodarc
 *
 */
public abstract class RobotElementsStyleConfiguration extends AbstractRegistryConfiguration {

    private final TableTheme theme;

    protected final RedPreferences preferences;

    RobotElementsStyleConfiguration(final TableTheme theme, final RedPreferences preferences) {
        this.theme = theme;
        this.preferences = preferences;
    }

    protected final Style createStyle(final SyntaxHighlightingCategory category) {
        final ColoringPreference syntaxColoring = preferences.getSyntaxColoring(category);

        final Style style = new Style();
        style.setAttributeValue(CellStyleAttributes.FONT,
                FontsManager.transformFontWithStyle(theme.getFont(), syntaxColoring.getFontStyle()));
        style.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, syntaxColoring.getColor());
        return style;
    }
}
