/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.graphics.Color;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableConfigurationLabels;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.graphics.ColorsManager;

public class KeywordArgumentsStyleConfigurationTest {

    @SuppressWarnings("unchecked")
    @Test
    public void configurationCheck() {
        final Color bodyMissingArgumentBgColorInUse = ColorsManager.getColor(1, 1, 1);
        final Color bodyOptionalArgumentCellBgColorInUse = ColorsManager.getColor(2, 2, 2);
        final Color bodyInactiveCellBgColorInUse = ColorsManager.getColor(3, 3, 3);

        final TableTheme theme = mock(TableTheme.class);
        when(theme.getBodyMissingArgumentCellBackground()).thenReturn(bodyMissingArgumentBgColorInUse);
        when(theme.getBodyOptionalArgumentCellBackground()).thenReturn(bodyOptionalArgumentCellBgColorInUse);
        when(theme.getBodyInactiveCellBackground()).thenReturn(bodyInactiveCellBgColorInUse);

        final IConfigRegistry configRegistry = mock(IConfigRegistry.class);

        final KeywordArgumentsStyleConfiguration configuration = new KeywordArgumentsStyleConfiguration(theme);
        configuration.configureRegistry(configRegistry);

        verify(configRegistry).registerConfigAttribute(isA(ConfigAttribute.class),
                argThat((hasBackground(bodyMissingArgumentBgColorInUse))), eq(DisplayMode.NORMAL),
                eq(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL));
        verify(configRegistry).registerConfigAttribute(isA(ConfigAttribute.class),
                argThat((hasBackground(bodyMissingArgumentBgColorInUse))), eq(DisplayMode.SELECT),
                eq(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL));
        verify(configRegistry).registerConfigAttribute(isA(ConfigAttribute.class),
                argThat((hasBackground(bodyOptionalArgumentCellBgColorInUse))), eq(DisplayMode.NORMAL),
                eq(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL));
        verify(configRegistry).registerConfigAttribute(isA(ConfigAttribute.class),
                argThat((hasBackground(bodyOptionalArgumentCellBgColorInUse))), eq(DisplayMode.SELECT),
                eq(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL));
        verify(configRegistry).registerConfigAttribute(isA(ConfigAttribute.class),
                argThat((hasBackground(bodyInactiveCellBgColorInUse))), eq(DisplayMode.NORMAL),
                eq(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL));
        verify(configRegistry).registerConfigAttribute(isA(ConfigAttribute.class),
                argThat((hasBackground(bodyInactiveCellBgColorInUse))), eq(DisplayMode.SELECT),
                eq(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL));
        verifyNoMoreInteractions(configRegistry);
    }

    private static ArgumentMatcher<IStyle> hasBackground(final Color color) {
        return style -> style.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR).equals(color);
    }

}
