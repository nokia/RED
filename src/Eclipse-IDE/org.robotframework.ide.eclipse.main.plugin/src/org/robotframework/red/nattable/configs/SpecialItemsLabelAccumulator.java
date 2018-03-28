/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;

/**
 * @author lwlodarc
 *
 */
public class SpecialItemsLabelAccumulator implements IConfigLabelAccumulator {

    public static final String SPECIAL_ITEM_CONFIG_LABEL = "SPECIAL_ITEM";

    private final IRowDataProvider<Object> dataProvider;

    public SpecialItemsLabelAccumulator(final IRowDataProvider<Object> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        final Object rowObject = dataProvider.getRowObject(rowPosition);

        if (rowObject instanceof RobotKeywordCall) {
            final List<RobotToken> tokens = ((RobotKeywordCall) rowObject).getLinkedElement().getElementTokens();
            if (tokens.size() > columnPosition) {
                final List<IRobotTokenType> types = tokens.get(columnPosition).getTypes();
                if (types.contains(RobotTokenType.IN_TOKEN)) {
                    configLabels.addLabel(SPECIAL_ITEM_CONFIG_LABEL);
                }
            }
        }
    }
}