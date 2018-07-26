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
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.validation.SpecialKeywords;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;

/**
 * @author lwlodarc
 *
 */
public class SettingsActionNamesLabelAccumulator implements IConfigLabelAccumulator {

    private final IRowDataProvider<?> dataProvider;

    public SettingsActionNamesLabelAccumulator(final IRowDataProvider<?> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void accumulateConfigLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition) {
        final RobotSetting setting = ((Entry<String, RobotSetting>) dataProvider.getRowObject(rowPosition)).getValue();
        if (setting != null && setting.isKeywordBased()) {
            if (columnPosition == 1) {
                // don't worry about variable here - this case would be served by
                // SettingsVariablesLabelAccumulator
                configLabels.addLabel(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);

            } else if (columnPosition > 1
                    && setting.getLinkedElement().getModelType() != ModelType.SUITE_TEST_TEMPLATE) {
                final AKeywordBaseSetting<?> linkedSetting = (AKeywordBaseSetting<?>) setting.getLinkedElement();
                final List<RobotToken> allTokens = new ArrayList<>();
                allTokens.add(linkedSetting.getKeywordName());
                allTokens.addAll(linkedSetting.getArguments());

                if (columnPosition - 1 < allTokens.size()) {
                    for (int i = columnPosition - 2; i >= 0; i--) {
                        final QualifiedKeywordName qualifiedKwName = QualifiedKeywordName
                                .fromOccurrence(allTokens.get(i).getText());
                        if (SpecialKeywords.isNestingKeyword(qualifiedKwName) && SpecialKeywords
                                .isKeywordNestedInKeyword(qualifiedKwName, i, columnPosition - i - 1, allTokens)) {
                            configLabels.addLabel(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
                            return;
                        }
                    }
                }
            }
        }
    }
}