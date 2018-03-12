/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.red.nattable;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public interface IFilteringDataProvider extends IDataProvider {

    boolean isFilterSet();
}
