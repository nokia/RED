/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.rf.ide.core.testdata.text.read.recognizer.settings.LibraryAliasRecognizer;

/**
 * @author lwlodarc
 *
 */
public class WithNameLabelAccumulator implements IConfigLabelAccumulator {

    private final IRowDataProvider<Object> dataProvider;

    public WithNameLabelAccumulator(final IRowDataProvider<Object> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        if (columnPosition >= 2) {
            final String label = (String) dataProvider.getDataValue(columnPosition, rowPosition);
            if (LibraryAliasRecognizer.EXPECTED.matcher(label).matches()) {
                configLabels.addLabel(SpecialItemsLabelAccumulator.SPECIAL_ITEM_CONFIG_LABEL);
            }
        }
    }
}