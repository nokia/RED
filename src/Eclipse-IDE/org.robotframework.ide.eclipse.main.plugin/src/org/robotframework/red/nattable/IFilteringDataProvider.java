package org.robotframework.red.nattable;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public interface IFilteringDataProvider extends IDataProvider {

    boolean isFilterSet();
}
