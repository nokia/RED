/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.graphics.FontsManager;

/**
 * @author lwlodarc
 *
 */
public class SettingsItemsStyleConfigurationTest {

    private final RedPreferences preferences = mock(RedPreferences.class);

    @Before
    public void before() {
        when(preferences.getSyntaxColoring(SyntaxHighlightingCategory.SETTING))
                .thenReturn(new ColoringPreference(new RGB(1, 2, 3), SWT.BOLD));
    }

    @Test
    public void sameStyleIsRegisteredForEachDisplayMode() throws Exception {
        final IConfigRegistry configRegistry = new ConfigRegistry();

        final SettingsItemsStyleConfiguration config = new SettingsItemsStyleConfiguration(mock(TableTheme.class),
                preferences);
        config.configureRegistry(configRegistry);

        final IStyle style1 = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                SettingsItemsLabelAccumulator.SETTING_CONFIG_LABEL);
        final IStyle style2 = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.HOVER,
                SettingsItemsLabelAccumulator.SETTING_CONFIG_LABEL);
        final IStyle style3 = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.SELECT,
                SettingsItemsLabelAccumulator.SETTING_CONFIG_LABEL);
        final IStyle style4 = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE,
                DisplayMode.SELECT_HOVER, SettingsItemsLabelAccumulator.SETTING_CONFIG_LABEL);

        assertThat(style1).isSameAs(style2);
        assertThat(style2).isSameAs(style3);
        assertThat(style3).isSameAs(style4);
    }

    @Test
    public void fontDefinedInStyleUsesFontTakenFromThemeWithStyleDefinedInPreferences() {
        final TableTheme theme = mock(TableTheme.class);
        when(theme.getFont()).thenReturn(JFaceResources.getTextFont());

        final IConfigRegistry configRegistry = new ConfigRegistry();

        final SettingsItemsStyleConfiguration config = new SettingsItemsStyleConfiguration(theme, preferences);
        config.configureRegistry(configRegistry);

        final IStyle style = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                SettingsItemsLabelAccumulator.SETTING_CONFIG_LABEL);

        assertThat(style.getAttributeValue(CellStyleAttributes.FONT))
                .isSameAs(FontsManager.transformFontWithStyle(JFaceResources.getTextFont(), SWT.BOLD));
    }

    @Test
    public void foregroundColorDefinedInStyleUsesColorTakenFromPreferences() {
        final IConfigRegistry configRegistry = new ConfigRegistry();

        final SettingsItemsStyleConfiguration config = new SettingsItemsStyleConfiguration(mock(TableTheme.class),
                preferences);
        config.configureRegistry(configRegistry);

        final IStyle style = configRegistry.getConfigAttribute(CellConfigAttributes.CELL_STYLE, DisplayMode.NORMAL,
                SettingsItemsLabelAccumulator.SETTING_CONFIG_LABEL);

        assertThat(style.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR).getRGB()).isEqualTo(new RGB(1, 2, 3));
    }
}
