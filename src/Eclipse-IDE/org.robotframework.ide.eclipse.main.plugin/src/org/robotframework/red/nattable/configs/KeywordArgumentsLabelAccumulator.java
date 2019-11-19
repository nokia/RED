/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.List;
import java.util.function.BiConsumer;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ExecutableSetting;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.validation.SpecialKeywords;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.KeywordUsagesFinder;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableConfigurationLabels;

public class KeywordArgumentsLabelAccumulator implements IConfigLabelAccumulator {

    private final IRowDataProvider<?> dataProvider;

    private final KeywordUsagesFinder kwUsagesFinder;

    private final boolean isEnabled;

    public KeywordArgumentsLabelAccumulator(final IRowDataProvider<?> dataProvider,
            final KeywordUsagesFinder kwUsagesFinder) {
        this.dataProvider = dataProvider;
        this.kwUsagesFinder = kwUsagesFinder;
        this.isEnabled = RedPlugin.getDefault().getPreferences().isKeywordArgumentCellsColoringEnabled();
    }

    @Override
    public void accumulateConfigLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition) {
        if (!isEnabled || columnPosition < 1) {
            return;
        }

        final Object rowObject = dataProvider.getRowObject(rowPosition);
        if (!(rowObject instanceof RobotKeywordCall)) {
            return;
        }

        final RobotKeywordCall call = (RobotKeywordCall) rowObject;
        final AModelElement<?> linkedElement = call.getLinkedElement();
        final List<RobotToken> elementTokens = linkedElement.getElementTokens();
        final List<RobotToken> commentTokens = call.getCommentTokens();
        if (!commentTokens.isEmpty() && columnPosition >= elementTokens.size() - commentTokens.size()) {
            return;
        }

        if (call.isExecutable() && !call.isForLoopDefinition() && !call.isTemplateData()) {
            final RobotExecutableRow<?> row = (RobotExecutableRow<?>) linkedElement;
            final RobotToken action = row.buildLineDescription().getKeywordAction().getToken();
            if (!action.isEmpty()) {
                final String kwName = action.getText();
                if (kwUsagesFinder.getQualifiedName(kwName).filter(SpecialKeywords::isNestingKeyword).isPresent()) {
                    addNestedLabels(row,
                            (name, offset) -> addLabels(configLabels, columnPosition, rowPosition, name, offset));
                } else {
                    addLabels(configLabels, columnPosition, rowPosition, kwName,
                            call.isForLoopBody() || call.isVariableDeclaration() ? 1 : 0);
                }
            }
        } else if (call.isExecutableSetting()) {
            final ExecutableSetting kwBasedSetting = call.getExecutableSetting();
            if (!kwBasedSetting.isDisabled()) {
                final String kwName = kwBasedSetting.getKeywordName().getText();
                if (kwUsagesFinder.getQualifiedName(kwName).filter(SpecialKeywords::isNestingKeyword).isPresent()) {
                    final RobotExecutableRow<?> row = kwBasedSetting.asExecutableRow();
                    addNestedLabels(row,
                            (name, offset) -> addLabels(configLabels, columnPosition, rowPosition, name, offset + 1));
                } else {
                    addLabels(configLabels, columnPosition, rowPosition, kwName, 1);
                }
            }
        }
    }

    private void addNestedLabels(final RobotExecutableRow<?> row, final BiConsumer<String, Integer> consumer) {
        // TODO: find executables in nested row
        consumer.accept(row.getAction().getText(), 0);
    }

    private void addLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition,
            final String kwName, final int argStartOffset) {
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
