/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;

/**
 * @author Michal Anglart
 *
 */
public class RedNattableDataProvidersFactory {

    public IDataProvider createColumnHeaderDataProvider(final String... columnLabels) {
        return new DefaultColumnHeaderDataProvider(columnLabels);
    }

    public IDataProvider createRowHeaderDataProvider(final IDataProvider tableDataProvider) {
        return new DefaultRowHeaderDataProvider(tableDataProvider) {
            @Override
            public Object getDataValue(final int columnIndex, final int rowIndex) {
                return "";
            }
        };
    }
}
