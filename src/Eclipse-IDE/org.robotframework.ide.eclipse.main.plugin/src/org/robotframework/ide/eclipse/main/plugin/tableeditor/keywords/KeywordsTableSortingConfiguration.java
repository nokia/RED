/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.NullComparator;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.sort.SortConfigAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;

class KeywordsTableSortingConfiguration extends AbstractRegistryConfiguration {

    private final IRowDataProvider<?> dataProvider;
    
    public KeywordsTableSortingConfiguration(IRowDataProvider<?> dataProvider) {
        this.dataProvider = dataProvider;
    }
    
    @Override
    public void configureRegistry(IConfigRegistry configRegistry) {

        for (int i = 1; i < dataProvider.getColumnCount(); i++) {
            configRegistry.registerConfigAttribute(SortConfigAttributes.SORT_COMPARATOR, new NullComparator(),
                    DisplayMode.NORMAL, ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + i);
        }

    }
    
}