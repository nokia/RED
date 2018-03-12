/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.style.ConfigAttribute;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;

public class ImportTypesStyleConfigurationTest {

    @SuppressWarnings("unchecked")
    @Test
    public void configurationCheck() {
        final IConfigRegistry configRegistry = mock(IConfigRegistry.class);

        final ImportTypesStyleConfiguration configuration = new ImportTypesStyleConfiguration(mock(TableTheme.class),
                false);
        configuration.configureRegistry(configRegistry);

        verify(configRegistry, times(1)).registerConfigAttribute(isA(ConfigAttribute.class),
                isA(CellPainterDecorator.class), eq(DisplayMode.NORMAL),
                eq(ImportTypesLabelAccumulator.IMPORT_TYPE_LABEL));
    }
}
