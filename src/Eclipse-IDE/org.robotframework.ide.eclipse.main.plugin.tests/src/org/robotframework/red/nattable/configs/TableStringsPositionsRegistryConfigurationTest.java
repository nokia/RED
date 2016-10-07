/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.junit.Test;
import org.robotframework.red.nattable.ITableStringsDecorationsSupport;
import org.robotframework.red.nattable.TableCellsStrings;

public class TableStringsPositionsRegistryConfigurationTest {

    @Test
    public void tableStringsAreProperlyRegistered() {
        final IConfigRegistry registry = mock(IConfigRegistry.class);

        final TableCellsStrings strings = new TableCellsStrings();
        final TableStringsPositionsRegistryConfiguration config = new TableStringsPositionsRegistryConfiguration(
                strings);
        config.configureRegistry(registry);

        verify(registry).registerConfigAttribute(ITableStringsDecorationsSupport.TABLE_STRINGS, strings,
                DisplayMode.NORMAL);
        verifyNoMoreInteractions(registry);
    }
}
