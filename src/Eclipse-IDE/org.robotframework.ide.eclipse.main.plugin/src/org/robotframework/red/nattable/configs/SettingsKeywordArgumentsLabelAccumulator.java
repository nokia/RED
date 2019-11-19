/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.Map.Entry;
import java.util.function.BiConsumer;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.rf.ide.core.testdata.model.ExecutableSetting;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.validation.SpecialKeywords;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.KeywordUsagesFinder;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableConfigurationLabels;

public class SettingsKeywordArgumentsLabelAccumulator implements IConfigLabelAccumulator {

    private final IRowDataProvider<?> dataProvider;

    private final KeywordUsagesFinder kwUsagesFinder;

    private final boolean isEnabled;

    public SettingsKeywordArgumentsLabelAccumulator(final IRowDataProvider<?> dataProvider,
            final KeywordUsagesFinder kwUsagesFinder) {
        this.dataProvider = dataProvider;
        this.kwUsagesFinder = kwUsagesFinder;
        this.isEnabled = RedPlugin.getDefault().getPreferences().isKeywordArgumentCellsColoringEnabled();
    }

    @Override
    public void accumulateConfigLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition) {
        if (!isEnabled || columnPosition < 2 || columnPosition == dataProvider.getColumnCount() - 1) {
            return;
        }

        @SuppressWarnings("unchecked")
        final RobotSetting setting = ((Entry<String, RobotSetting>) dataProvider.getRowObject(rowPosition)).getValue();
        if (setting != null && setting.isAnySetupOrTeardown()
                && !((ExecutableSetting) setting.getLinkedElement()).isDisabled()) {
            final ExecutableSetting linkedSetting = (ExecutableSetting) setting.getLinkedElement();
            final String kwName = linkedSetting.getKeywordName().getText();
            if (kwUsagesFinder.getQualifiedName(kwName).filter(SpecialKeywords::isNestingKeyword).isPresent()) {
                final RobotExecutableRow<?> row = linkedSetting.asExecutableRow();
                addNestedKwLabels(row,
                        (name, offset) -> addLabels(configLabels, columnPosition, rowPosition, name, offset + 1));
            } else {
                addLabels(configLabels, columnPosition, rowPosition, kwName, 1);
            }
        }
    }

    private void addNestedKwLabels(final RobotExecutableRow<?> row, final BiConsumer<String, Integer> consumer) {
        // TODO: find executables in nested row
        consumer.accept(row.getAction().getText(), 0);
    }

    private void addLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition,
            final String kwName, final int offset) {
        kwUsagesFinder.getArgumentsDescriptor(kwName).ifPresent(descriptor -> {
            if (columnPosition > descriptor.getRequiredArguments().size() + offset) {
                if (descriptor.getPossibleNumberOfArguments().contains(columnPosition - offset)) {
                    configLabels.addLabelOnTop(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
                } else {
                    configLabels.addLabelOnTop(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
                }
            } else {
                final String value = (String) dataProvider.getDataValue(columnPosition, rowPosition);
                if (value.isEmpty()) {
                    configLabels.addLabelOnTop(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
                }
            }
        });
    }
}
