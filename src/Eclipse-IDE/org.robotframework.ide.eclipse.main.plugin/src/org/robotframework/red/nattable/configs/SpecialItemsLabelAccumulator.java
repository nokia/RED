/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.List;
import java.util.function.Function;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.SpecialTokensRule;

/**
 * @author lwlodarc
 *
 */
public class SpecialItemsLabelAccumulator implements IConfigLabelAccumulator {

    public static final String SPECIAL_ITEM_CONFIG_LABEL = "SPECIAL_ITEM";

    private final Function<Integer, Object> rowObjectProvider;

    public SpecialItemsLabelAccumulator(final Function<Integer, Object> rowObjectProvider) {
        this.rowObjectProvider = rowObjectProvider;
    }

    @Override
    public void accumulateConfigLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition) {
        final Object rowObject = rowObjectProvider.apply(rowPosition);

        if (rowObject instanceof RobotKeywordCall) {
            final RobotKeywordCall call = (RobotKeywordCall) rowObject;
            final List<RobotToken> tokens = call.getLinkedElement().getElementTokens();

            if (tokens.size() > columnPosition) {
                final List<IRobotTokenType> types = tokens.get(columnPosition).getTypes();
                if (types.contains(RobotTokenType.IN_TOKEN) || types.contains(RobotTokenType.FOR_TOKEN)
                        || types.contains(RobotTokenType.FOR_END_TOKEN)) {
                    configLabels.addLabel(SPECIAL_ITEM_CONFIG_LABEL);

                } else if (SpecialTokensRule.isNoneAwareSetting(tokens.get(columnPosition))) {
                    configLabels.addLabel(SPECIAL_ITEM_CONFIG_LABEL);
                }
            }
        }
    }
}