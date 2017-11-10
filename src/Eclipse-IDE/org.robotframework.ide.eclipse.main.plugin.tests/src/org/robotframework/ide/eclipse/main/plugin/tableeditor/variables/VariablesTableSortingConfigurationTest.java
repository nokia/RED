/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultComparator;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.NullComparator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.sort.SortConfigAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.junit.Test;

public class VariablesTableSortingConfigurationTest {

    @Test
    public void thereIsADefaultComparatorRegisteredForVariableNameColumn() {
        final VariablesTableSortingConfiguration config = new VariablesTableSortingConfiguration();

        final IConfigRegistry configRegistry = new ConfigRegistry();
        config.configureRegistry(configRegistry);

        final Comparator<?> comparator = configRegistry.getConfigAttribute(SortConfigAttributes.SORT_COMPARATOR,
                DisplayMode.NORMAL, ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 0);
        assertThat(comparator).isInstanceOf(DefaultComparator.class);
    }

    @Test
    public void thereIsADefaultComparatorRegisteredForVariableValuesColumn() {
        final VariablesTableSortingConfiguration config = new VariablesTableSortingConfiguration();

        final IConfigRegistry configRegistry = new ConfigRegistry();
        config.configureRegistry(configRegistry);

        final Comparator<?> comparator = configRegistry.getConfigAttribute(SortConfigAttributes.SORT_COMPARATOR,
                DisplayMode.NORMAL, ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 1);
        assertThat(comparator).isInstanceOf(DefaultComparator.class);
    }

    @Test
    public void thereIsNoSortingEnabledForCommentsColumn() {
        final VariablesTableSortingConfiguration config = new VariablesTableSortingConfiguration();

        final IConfigRegistry configRegistry = new ConfigRegistry();
        config.configureRegistry(configRegistry);

        final Comparator<?> comparator = configRegistry.getConfigAttribute(SortConfigAttributes.SORT_COMPARATOR,
                DisplayMode.NORMAL, ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + 2);
        assertThat(comparator).isInstanceOf(NullComparator.class);
    }
}
