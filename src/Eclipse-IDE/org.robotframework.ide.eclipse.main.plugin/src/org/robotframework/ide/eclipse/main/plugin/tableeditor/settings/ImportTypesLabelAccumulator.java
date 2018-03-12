/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;


class ImportTypesLabelAccumulator implements IConfigLabelAccumulator {

    static final String IMPORT_TYPE_LABEL = "IMPORT_TYPE";

    private final ImportSettingsDataProvider dataProvider;

    ImportTypesLabelAccumulator(final ImportSettingsDataProvider provider) {
        this.dataProvider = provider;
    }

    @Override
    public void accumulateConfigLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition) {
        final Object variable = dataProvider.getRowObject(rowPosition);
        if (variable instanceof RobotSetting && columnPosition == 0) {
            configLabels.addLabel(IMPORT_TYPE_LABEL);
        }
    }
}
