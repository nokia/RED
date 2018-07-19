/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.validation.SpecialKeywords;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;

public class NestedExecsSpecialTokensLabelAccumulator implements IConfigLabelAccumulator {

    private final IRowDataProvider<Object> dataProvider;

    public NestedExecsSpecialTokensLabelAccumulator(final IRowDataProvider<Object> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void accumulateConfigLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition) {
        if (columnPosition == 0) {
            return;
        }
        final List<RobotToken> tokensBefore = getTokensBefore(columnPosition, rowPosition);
        if (tokensBefore.isEmpty()) {
            return;
        }

        final RobotToken token = tokensBefore.remove(tokensBefore.size() - 1);
        for (int j = tokensBefore.size() - 1; j >= 0; j--) {
            final QualifiedKeywordName qualifiedKeywordName = QualifiedKeywordName
                    .fromOccurrence(tokensBefore.get(j).getText());
            if (SpecialKeywords.isNestingKeyword(qualifiedKeywordName)
                    && SpecialKeywords.isNestedSyntaxSpecialToken(qualifiedKeywordName, token)) {
                configLabels.addLabel(SpecialItemsLabelAccumulator.SPECIAL_ITEM_CONFIG_LABEL);
            }
        }
    }

    private List<RobotToken> getTokensBefore(final int columnPosition, final int rowPosition) {
        final Object rowObject = dataProvider.getRowObject(rowPosition);
        if (rowObject instanceof RobotKeywordCall && ((RobotKeywordCall) rowObject).isExecutable()) {
            final RobotKeywordCall call = (RobotKeywordCall) rowObject;
            
            final List<RobotToken> tokens = new ArrayList<>();
            call.getAction().ifPresent(tokens::add);
            tokens.addAll(
                    call.getArgumentTokens().subList(0, Math.min(call.getArgumentTokens().size(), columnPosition)));
            return tokens;

        } else if (rowObject instanceof RobotDefinitionSetting && ((RobotDefinitionSetting) rowObject).isKeywordBased()) {
            final AKeywordBaseSetting<?> setting = (AKeywordBaseSetting<?>) ((RobotDefinitionSetting) rowObject).getLinkedElement();

            final List<RobotToken> tokens = new ArrayList<>();
            tokens.add(setting.getKeywordName());
            tokens.addAll(
                    setting.getArguments().subList(0, Math.min(setting.getArguments().size(), columnPosition - 1)));
            return tokens;
        }
        return new ArrayList<>();
    }
}
