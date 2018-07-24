/*
* Copyright 2018 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.red.nattable.configs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;


public class SettingsNestedExecsSpecialTokensLabelAccumulatorTest {

    @Test
    public void labelsAreOnlyAccumulated_forElseElseIfWordsInRunKeywordIf_inSuiteSetup() {
        final RobotSetting call = (RobotSetting) createModelWithSpecialKeywords()
                .findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(0);
        final Entry<String, RobotSetting> entry = entry(call.getName(), call);

        assertThat(labelsAt(entry, 0)).isEmpty();
        assertThat(labelsAt(entry, 1)).isEmpty();
        assertThat(labelsAt(entry, 2)).isEmpty();
        assertThat(labelsAt(entry, 3)).isEmpty();
        assertThat(labelsAt(entry, 4)).isEmpty();
        assertThat(labelsAt(entry, 5)).containsOnly(SpecialItemsLabelAccumulator.SPECIAL_ITEM_CONFIG_LABEL);
        assertThat(labelsAt(entry, 6)).isEmpty();
        assertThat(labelsAt(entry, 7)).isEmpty();
        assertThat(labelsAt(entry, 8)).isEmpty();
        assertThat(labelsAt(entry, 9)).isEmpty();
        assertThat(labelsAt(entry, 10)).containsOnly(SpecialItemsLabelAccumulator.SPECIAL_ITEM_CONFIG_LABEL);
        assertThat(labelsAt(entry, 11)).isEmpty();
        assertThat(labelsAt(entry, 12)).isEmpty();
    }

    @Test
    public void labelsAreOnlyAccumulated_forAndWordsInRunKeywords_inSuiteTeardown() {
        final RobotSetting call = (RobotSetting) createModelWithSpecialKeywords()
                .findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(1);
        final Entry<String, RobotSetting> entry = entry(call.getName(), call);

        assertThat(labelsAt(entry, 0)).isEmpty();
        assertThat(labelsAt(entry, 1)).isEmpty();
        assertThat(labelsAt(entry, 2)).isEmpty();
        assertThat(labelsAt(entry, 3)).isEmpty();
        assertThat(labelsAt(entry, 4)).containsOnly(SpecialItemsLabelAccumulator.SPECIAL_ITEM_CONFIG_LABEL);
        assertThat(labelsAt(entry, 5)).isEmpty();
        assertThat(labelsAt(entry, 6)).isEmpty();
        assertThat(labelsAt(entry, 7)).isEmpty();
        assertThat(labelsAt(entry, 8)).containsOnly(SpecialItemsLabelAccumulator.SPECIAL_ITEM_CONFIG_LABEL);
        assertThat(labelsAt(entry, 9)).isEmpty();
        assertThat(labelsAt(entry, 10)).isEmpty();
        assertThat(labelsAt(entry, 11)).isEmpty();
        assertThat(labelsAt(entry, 12)).isEmpty();
    }

    private static List<String> labelsAt(final Entry<String, RobotSetting> entry, final int column) {
        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(entry);

        final LabelStack labels = new LabelStack();
        final SettingsNestedExecsSpecialTokensLabelAccumulator labelAccumulator = new SettingsNestedExecsSpecialTokensLabelAccumulator(
                dataProvider);
        labelAccumulator.accumulateConfigLabels(labels, column, 0);
        return labels.getLabels();
    }

    private static RobotSuiteFile createModelWithSpecialKeywords() {
        return new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine(
                        "Suite Setup  Run Keyword If  condition1  kw1  arg1  ELSE IF  condition2  kw2  arg2  arg3  ELSE  kw3  arg4")
                .appendLine("Suite Teardown  Run Keywords  kw 1  arg1  AND  kw2  arg2  arg3  AND  kw3  arg4")
                .build();
    }

    private static <K, V> Entry<K, V> entry(final K key, final V value) {
        return new Entry<K, V>() {

            @Override
            public V setValue(final V value) {
                return value;
            }

            @Override
            public V getValue() {
                return value;
            }

            @Override
            public K getKey() {
                return key;
            }
        };
    }
}
