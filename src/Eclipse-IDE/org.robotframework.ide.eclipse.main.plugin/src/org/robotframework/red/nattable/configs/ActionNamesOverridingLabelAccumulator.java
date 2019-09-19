/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.List;
import java.util.function.Function;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.KeywordUsagesFinder;


public class ActionNamesOverridingLabelAccumulator implements IConfigLabelAccumulator {

    private final Function<Integer, Object> rowObjectProvider;

    private final KeywordUsagesFinder kwUsagesFinder;

    public ActionNamesOverridingLabelAccumulator(final Function<Integer, Object> rowObjectProvider,
            final KeywordUsagesFinder kwUsagesFinder) {
        this.rowObjectProvider = rowObjectProvider;
        this.kwUsagesFinder = kwUsagesFinder;
    }

    @Override
    public void accumulateConfigLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition) {
        if (configLabels.hasLabel(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL)) {
            final Object rowObject = rowObjectProvider.apply(rowPosition);

            if (rowObject instanceof RobotKeywordCall) {
                final RobotKeywordCall call = (RobotKeywordCall) rowObject;
                final List<RobotToken> tokens = call.getLinkedElement().getElementTokens();

                if (tokens.size() > columnPosition) {
                    final String token = tokens.get(columnPosition).getText();
                    
                    if (kwUsagesFinder.isLibraryKeyword(token)) {
                        configLabels.removeLabel(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
                        configLabels.addLabel(ActionNamesLabelAccumulator.ACTION_FROM_LIB_NAME_CONFIG_LABEL);
                    }
                }
            }
        }
    }
}
