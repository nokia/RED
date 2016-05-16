/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.nattable;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;

class SettingsDynamicTableColumnHeaderLabelAcumulator implements IConfigLabelAccumulator {
    
    public static final String SETTING_COMMENT_LABEL = "SETTING_COMMENT" ;
    
    private final IRowDataProvider<?> dataProvider;
    
    public SettingsDynamicTableColumnHeaderLabelAcumulator(final IRowDataProvider<?> dataProvider) {
        this.dataProvider = dataProvider;
    }
    
    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
       if((dataProvider.getColumnCount()-1) == columnPosition) {
           configLabels.addLabel(SETTING_COMMENT_LABEL);
       } else {
           configLabels.addLabel(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + columnPosition);
       }
    }
}