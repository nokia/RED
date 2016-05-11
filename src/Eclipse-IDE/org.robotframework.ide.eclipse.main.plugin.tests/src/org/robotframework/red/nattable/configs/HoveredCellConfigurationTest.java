/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.graphics.Color;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.graphics.ColorsManager;

public class HoveredCellConfigurationTest {

    @SuppressWarnings("unchecked")
    @Test
    public void configurationCheck() {
        final Color hoverSelectedBgColorInUse = ColorsManager.getColor(200, 200, 200);
        final Color hoverBgColorInUse = ColorsManager.getColor(100, 100, 100);

        final TableTheme theme = mock(TableTheme.class);
        when(theme.getBodyHoveredSelectedCellBackground()).thenReturn(hoverSelectedBgColorInUse);
        when(theme.getBodyHoveredCellBackground()).thenReturn(hoverBgColorInUse);

        final IConfigRegistry configRegistry = mock(IConfigRegistry.class);

        final HoveredCellConfiguration configuration = new HoveredCellConfiguration(theme);
        configuration.configureRegistry(configRegistry);

        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class), isA(IStyle.class),
                eq(DisplayMode.HOVER));
        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class), isA(IStyle.class),
                eq(DisplayMode.SELECT_HOVER));
    }
}
