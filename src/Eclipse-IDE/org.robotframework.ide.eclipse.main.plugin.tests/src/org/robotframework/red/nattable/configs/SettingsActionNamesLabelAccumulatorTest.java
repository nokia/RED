/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;

/**
 * @author lwlodarc
 *
 */
public class SettingsActionNamesLabelAccumulatorTest {

    private IRowDataProvider<Object> dataProvider;
    private LabelStack labels;
    private SettingsActionNamesLabelAccumulator labelAccumulator;

    @SuppressWarnings("unchecked")
    @Before
    public void cleanData() {
        dataProvider = mock(IRowDataProvider.class);
        labels = new LabelStack();
        labelAccumulator = new SettingsActionNamesLabelAccumulator(dataProvider);
    }

    @Test
    public void labelIsNotAdded_forEmptyLine() {
        final RobotKeywordCall call = new RobotKeywordCall(null, new RobotExecutableRow<>());
        when(dataProvider.getRowObject(0)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forNonFirstColumnKeywordBased() {
        final RobotSetting setting = mock(RobotSetting.class);
        final Entry<String, RobotSetting> entry = createEntry(setting);
        when(setting.isKeywordBased()).thenReturn(true);
        when(dataProvider.getRowObject(0)).thenReturn(entry);
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        labelAccumulator.accumulateConfigLabels(labels, 2, 0);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forNonKeywordBased() {
        final RobotSetting setting = mock(RobotSetting.class);
        final Entry<String, RobotSetting> entry = createEntry(setting);
        when(setting.isKeywordBased()).thenReturn(false);
        when(dataProvider.getRowObject(0)).thenReturn(entry);
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        labelAccumulator.accumulateConfigLabels(labels, 1, 0);
        labelAccumulator.accumulateConfigLabels(labels, 2, 0);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsAdded_forFirstColumnKeywordBased() {
        final RobotSetting setting = mock(RobotSetting.class);
        final Entry<String, RobotSetting> entry = createEntry(setting);
        when(setting.isKeywordBased()).thenReturn(true);
        when(dataProvider.getRowObject(0)).thenReturn(entry);
        labelAccumulator.accumulateConfigLabels(labels, 1, 0);
        assertThat(labels.getLabels()).containsExactly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
    }

    private static Entry<String, RobotSetting> createEntry(final RobotSetting setting) {
        return new Entry<String, RobotSetting>() {

            @Override
            public RobotSetting setValue(RobotSetting value) {
                return null;
            }

            @Override
            public RobotSetting getValue() {
                return setting;
            }

            @Override
            public String getKey() {
                return null;
            }
        };
    }
}