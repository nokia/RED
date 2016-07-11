/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.nattable;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.nattable.KeywordsDataProvider.RobotKeywordCallAdder;
import org.robotframework.red.nattable.IFilteringDataProvider;
import org.robotframework.red.nattable.configs.AddingElementStyleConfiguration;

public class KeywordElementsInTreeLabelAccumulator implements IConfigLabelAccumulator {

    private final IRowDataProvider<Object> dataProvider;

    public KeywordElementsInTreeLabelAccumulator(final IRowDataProvider<Object> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void accumulateConfigLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition) {
        if (dataProvider instanceof IFilteringDataProvider && ((IFilteringDataProvider) dataProvider).isFilterSet()) {
            return;
        }

        Object rowObject = dataProvider.getRowObject(rowPosition);

        if (columnPosition == 0) {
            if (rowObject instanceof RobotDefinitionSetting) {
                configLabels.addLabel(KeywordDefinitionElementStyleConfiguration.KEYWORD_DEFINITION_SETTING_CONFIG_LABEL);
            } else if (rowObject instanceof RobotKeywordCallAdder) {
                configLabels.addLabel(AddingElementStyleConfiguration.ELEMENT_IN_TREE_ADDER_CONFIG_LABEL);
                configLabels.addLabel(AddingElementStyleConfiguration.ELEMENT_IN_TREE_ADDER_ROW_CONFIG_LABEL);
            } else if (rowObject instanceof RobotKeywordDefinition) {
                configLabels.addLabel(KeywordDefinitionElementStyleConfiguration.KEYWORD_DEFINITION_CONFIG_LABEL);
            }
        } else if (columnPosition > 0) {
            if (rowObject instanceof RobotKeywordDefinition) {
                configLabels.addLabel(KeywordDefinitionElementStyleConfiguration.KEYWORD_DEFINITION_ARGUMENT_CONFIG_LABEL);
            } else if (rowObject instanceof RobotKeywordCallAdder) {
                configLabels.addLabel(AddingElementStyleConfiguration.ELEMENT_IN_TREE_ADDER_ROW_CONFIG_LABEL);
            }
        }

    }
}
