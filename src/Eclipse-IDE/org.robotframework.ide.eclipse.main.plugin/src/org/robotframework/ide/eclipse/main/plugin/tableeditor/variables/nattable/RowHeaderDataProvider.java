/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;

/**
 * @author Michal Anglart
 *
 */
public class RowHeaderDataProvider extends DefaultRowHeaderDataProvider {

    public RowHeaderDataProvider(final IDataProvider bodyDataProvider) {
        super(bodyDataProvider);
    }

    @Override
    public Object getDataValue(final int columnIndex, final int rowIndex) {
        return "";
    }
}
