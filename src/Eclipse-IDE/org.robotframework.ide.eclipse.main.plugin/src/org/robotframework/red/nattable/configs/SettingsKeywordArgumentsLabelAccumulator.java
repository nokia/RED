/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.rf.ide.core.testdata.model.ExecutableSetting;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
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
        if (setting != null && setting.isAnySetupOrTeardown()) {
            final ExecutableSetting kwBasedSetting = (ExecutableSetting) setting.getLinkedElement();
            if (!kwBasedSetting.isDisabled()) {
                final IExecutableRowDescriptor<?> desc = kwBasedSetting.asExecutableRow().buildLineDescription();
                final int start = 1;
                addRowLabels(desc, kwBasedSetting.getKeywordName().getText(), 0,
                        (name, offset) -> addLabels(configLabels, columnPosition, rowPosition, name, start + offset));
            }
        }
    }

    private void addRowLabels(final IExecutableRowDescriptor<?> desc, final String kwName, final int offset,
            final BiConsumer<String, Integer> labelAdder) {
        final List<RobotExecutableRow<?>> nestedExecutables = kwUsagesFinder.getQualifiedName(kwName)
                .map(name -> SpecialKeywords.findNestedExecutableRows(desc, name))
                .orElseGet(ArrayList::new);
        if (nestedExecutables.isEmpty()) {
            labelAdder.accept(kwName, offset);
        } else {
            final RobotExecutableRow<?> nestedExec = nestedExecutables.get(nestedExecutables.size() - 1);
            final IExecutableRowDescriptor<?> nestedDesc = nestedExec.buildLineDescription();
            final String lastExecutableKwName = nestedDesc.getKeywordAction().getToken().getText();
            final int nestedOffset = offset + desc.getKeywordArguments().size() - nestedExec.getElementTokens().size();
            addRowLabels(nestedDesc, lastExecutableKwName, nestedOffset + 1, labelAdder);
        }
    }

    private void addLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition,
            final String kwName, final int argStartOffset) {
        if (columnPosition > argStartOffset) {
            kwUsagesFinder.getArgumentsDescriptor(kwName).ifPresent(descriptor -> {
                if (columnPosition > descriptor.getRequiredArguments().size() + argStartOffset) {
                    if (descriptor.getPossibleNumberOfArguments().contains(columnPosition - argStartOffset)) {
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
}
