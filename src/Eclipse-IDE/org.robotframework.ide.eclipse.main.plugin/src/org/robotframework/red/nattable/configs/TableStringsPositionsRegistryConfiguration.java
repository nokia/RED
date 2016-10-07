/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.robotframework.red.nattable.ITableStringsDecorationsSupport;
import org.robotframework.red.nattable.TableCellsStrings;


public class TableStringsPositionsRegistryConfiguration extends AbstractRegistryConfiguration {

    private final TableCellsStrings tableStrings;

    public TableStringsPositionsRegistryConfiguration(final TableCellsStrings tableStrings) {
        this.tableStrings = tableStrings;
    }

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        configRegistry.registerConfigAttribute(ITableStringsDecorationsSupport.TABLE_STRINGS, tableStrings,
                DisplayMode.NORMAL);
    }
}
