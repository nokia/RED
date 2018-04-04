/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;

/**
 * @author lwlodarc
 *
 */
public class VariablesLabelAccumulator implements IConfigLabelAccumulator {

    public static final String VARIABLE_CONFIG_LABEL = "VARIABLE";

    private static final Pattern varPattern = Pattern.compile("^[$@&]\\{[^\\}]*\\}(\\[.*\\])?=?");

    private final IRowDataProvider<Object> dataProvider;

    public VariablesLabelAccumulator(final IRowDataProvider<Object> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        if (columnPosition == 0) {
            final Object rowObject = dataProvider.getRowObject(rowPosition);

            if (rowObject instanceof RobotKeywordCall) {
                final List<RobotToken> tokens = ((RobotKeywordCall) rowObject).getLinkedElement().getElementTokens();
                if (!tokens.isEmpty()) {
                    final RobotToken token = tokens.get(0);
                    if (looksLikeVariable(token.getText())) {
                        configLabels.addLabel(VARIABLE_CONFIG_LABEL);
                    }
                }
            }
        }
    }

    public static boolean looksLikeVariable(final String text) {
        return varPattern.matcher(text).matches();
    }
}