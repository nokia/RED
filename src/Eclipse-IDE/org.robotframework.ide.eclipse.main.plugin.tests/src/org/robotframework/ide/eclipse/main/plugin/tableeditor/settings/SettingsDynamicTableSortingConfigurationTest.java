/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.NullComparator;
import org.eclipse.nebula.widgets.nattable.sort.SortConfigAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.junit.Test;

public class SettingsDynamicTableSortingConfigurationTest {

    @Test
    public void thereIsNoSortingEnabledForCommentsColumn() {
        final SettingsDynamicTableSortingConfiguration config = new SettingsDynamicTableSortingConfiguration();

        final IConfigRegistry configRegistry = new ConfigRegistry();
        config.configureRegistry(configRegistry);

        final Comparator<?> comparator = configRegistry.getConfigAttribute(SortConfigAttributes.SORT_COMPARATOR,
                DisplayMode.NORMAL, SettingsDynamicTableColumnHeaderLabelAccumulator.SETTING_COMMENT_LABEL);
        assertThat(comparator).isInstanceOf(NullComparator.class);
    }
}
