/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;

/**
 * @author lwlodarc
 *
 */
public class SettingsCommentsLabelAccumulator implements IConfigLabelAccumulator {

    private final IRowDataProvider<?> dataProvider;

    public SettingsCommentsLabelAccumulator(final IRowDataProvider<?> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        final String label = (String) dataProvider.getDataValue(columnPosition, rowPosition);

        if (label.startsWith("#")) {
            configLabels.addLabel(CommentsLabelAccumulator.COMMENT_CONFIG_LABEL);
        }
    }
}