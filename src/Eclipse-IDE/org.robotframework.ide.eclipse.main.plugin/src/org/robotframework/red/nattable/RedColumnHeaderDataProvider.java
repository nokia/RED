/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.red.nattable;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public abstract class RedColumnHeaderDataProvider implements IDataProvider {

    private final IDataProvider dataProvider;

    public RedColumnHeaderDataProvider(final IDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    protected final boolean isLastColumn(final int column) {
        return column == getColumnCount() - 1;
    }

    @Override
    public void setDataValue(final int columnIndex, final int rowIndex, final Object newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColumnCount() {
        return dataProvider.getColumnCount();
    }

    @Override
    public int getRowCount() {
        return 1;
    }
}
