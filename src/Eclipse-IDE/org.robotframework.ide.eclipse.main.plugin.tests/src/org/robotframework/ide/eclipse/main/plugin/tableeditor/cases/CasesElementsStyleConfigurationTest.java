/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.widgets.Display;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableConfigurationLabels;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;
import org.robotframework.red.nattable.painter.InactiveCellPainter;

public class CasesElementsStyleConfigurationTest {

    @SuppressWarnings("unchecked")
    @Test
    public void configurationCheck() {
        final TableTheme theme = mock(TableTheme.class);
        when(theme.getFont()).thenReturn(Display.getCurrent().getSystemFont());

        final IConfigRegistry configRegistry = mock(IConfigRegistry.class);

        final CasesElementsStyleConfiguration configuration = new CasesElementsStyleConfiguration(theme, true, false);
        configuration.configureRegistry(configRegistry);

        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class), isA(IStyle.class),
                eq(DisplayMode.NORMAL), eq(CasesElementsLabelAccumulator.CASE_CONFIG_LABEL));
        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class), isA(IStyle.class),
                eq(DisplayMode.SELECT), eq(CasesElementsLabelAccumulator.CASE_CONFIG_LABEL));
        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class), isA(CellPainterDecorator.class),
                eq(DisplayMode.NORMAL), eq(CasesElementsLabelAccumulator.CASE_CONFIG_LABEL));

        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class), isA(IStyle.class),
                eq(DisplayMode.NORMAL), eq(CasesElementsLabelAccumulator.CASE_WITH_TEMPLATE_CONFIG_LABEL));
        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class), isA(IStyle.class),
                eq(DisplayMode.SELECT), eq(CasesElementsLabelAccumulator.CASE_WITH_TEMPLATE_CONFIG_LABEL));
        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class),
                isA(CellPainterDecorator.class), eq(DisplayMode.NORMAL),
                eq(CasesElementsLabelAccumulator.CASE_WITH_TEMPLATE_CONFIG_LABEL));

        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class), isA(IStyle.class),
                eq(DisplayMode.NORMAL), eq(CasesElementsLabelAccumulator.CASE_SETTING_CONFIG_LABEL));
        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class), isA(IStyle.class),
                eq(DisplayMode.SELECT), eq(CasesElementsLabelAccumulator.CASE_SETTING_CONFIG_LABEL));

        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class),
                isA(InactiveCellPainter.class), eq(DisplayMode.NORMAL),
                eq(TableConfigurationLabels.CELL_NOT_EDITABLE_LABEL));
    }
}
