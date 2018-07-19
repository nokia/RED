/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.validation.SpecialKeywords;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;

public class SettingsNestedExecsSpecialTokensLabelAccumulator implements IConfigLabelAccumulator {

    private final IRowDataProvider<?> dataProvider;

    public SettingsNestedExecsSpecialTokensLabelAccumulator(final IRowDataProvider<?> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void accumulateConfigLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition) {
        if (columnPosition < 2) {
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
        @SuppressWarnings("unchecked")
        final RobotSetting setting = ((Entry<String, RobotSetting>) dataProvider.getRowObject(rowPosition)).getValue();
        if (setting != null && setting.isKeywordBased()) {
            final AKeywordBaseSetting<?> linkedSetting = (AKeywordBaseSetting<?>) setting.getLinkedElement();

            final List<RobotToken> tokens = new ArrayList<>();
            tokens.add(linkedSetting.getKeywordName());
            tokens.addAll(linkedSetting.getArguments()
                    .subList(0, Math.min(linkedSetting.getArguments().size(), columnPosition - 1)));
            return tokens;
        }
        return new ArrayList<>();
    }
}
