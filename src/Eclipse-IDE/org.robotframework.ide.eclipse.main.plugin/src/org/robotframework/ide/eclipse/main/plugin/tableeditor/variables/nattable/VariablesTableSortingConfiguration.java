/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.DefaultComparator;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.NullComparator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.sort.SortConfigAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;

class VariablesTableSortingConfiguration extends AbstractRegistryConfiguration {

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        configRegistry.registerConfigAttribute(SortConfigAttributes.SORT_COMPARATOR,
                DefaultComparator.getInstance(), DisplayMode.NORMAL,
                ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 0);
        configRegistry.registerConfigAttribute(SortConfigAttributes.SORT_COMPARATOR,
                DefaultComparator.getInstance(), DisplayMode.NORMAL,
                ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 1);
        configRegistry.registerConfigAttribute(SortConfigAttributes.SORT_COMPARATOR, new NullComparator(),
                DisplayMode.NORMAL, ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2);
    }
}