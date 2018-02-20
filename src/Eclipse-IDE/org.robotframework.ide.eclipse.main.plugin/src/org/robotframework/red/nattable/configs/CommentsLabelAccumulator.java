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
public class CommentsLabelAccumulator implements IConfigLabelAccumulator {

    public static final String COMMENT_CONFIG_LABEL = "COMMENT";

    private final IRowDataProvider<Object> dataProvider;

    public CommentsLabelAccumulator(final IRowDataProvider<Object> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        final Object rowObject = dataProvider.getRowObject(rowPosition);

        if (rowObject instanceof RobotKeywordCall) {
            final List<RobotToken> tokens = ((RobotKeywordCall) rowObject).getLinkedElement().getElementTokens();
            if (tokens.size() > columnPosition) {
                final List<IRobotTokenType> types = tokens.get(columnPosition).getTypes();
                if (types.contains(RobotTokenType.START_HASH_COMMENT)
                        || types.contains(RobotTokenType.COMMENT_CONTINUE)) {
                    configLabels.addLabel(COMMENT_CONFIG_LABEL);
                }
            }
        }
    }
}
