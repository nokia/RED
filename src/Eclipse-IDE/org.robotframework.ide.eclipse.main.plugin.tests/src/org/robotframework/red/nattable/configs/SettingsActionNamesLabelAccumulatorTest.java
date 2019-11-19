/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;

/**
 * @author lwlodarc
 *
 */
public class SettingsActionNamesLabelAccumulatorTest {

    @Test
    public void labelIsNotAdded_forMissingSetting() {
        for (int i = 0; i < 10; i++) {
            assertThat(labelsAt(new SimpleEntry<>("Setting", null), i)).isEmpty();
        }
    }

    @Test
    public void labelIsNotAdded_forNonKeywordBasedSetting() {
        final RobotSetting setting = mock(RobotSetting.class);
        when(setting.isAnySetupOrTeardown()).thenReturn(false);
        final Entry<String, RobotSetting> entry = new SimpleEntry<>("Setting", setting);

        assertThat(labelsAt(entry, 0)).isEmpty();
        assertThat(labelsAt(entry, 1)).isEmpty();
        assertThat(labelsAt(entry, 2)).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forFirstColumnOfKeywordBasedSetting() {
        final SuiteSetup setup = new SuiteSetup(RobotToken.create("[Setup]"));
        setup.setKeywordName("keyword");
        final RobotSetting setting = new RobotSetting(null, setup);
        final Entry<String, RobotSetting> entry = new SimpleEntry<>("Setting", setting);

        assertThat(labelsAt(entry, 0)).isEmpty();
    }

    @Test
    public void labelIsAdded_forSecondColumnOfKeywordBasedSetting() {
        final SuiteSetup linkedSetting = new SuiteSetup(RobotToken.create("Suite Setup"));
        linkedSetting.setKeywordName("Keyword");
        final RobotSetting setting = new RobotSetting(null, linkedSetting);
        final Entry<String, RobotSetting> entry = new SimpleEntry<>("Setting", setting);

        assertThat(labelsAt(entry, 1)).containsOnly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
    }

    @Test
    public void labelIsNotAdded_forSecondColumnOfEmptyKeywordBasedSetting() {
        final SuiteSetup linkedSetting = new SuiteSetup(RobotToken.create("Suite Setup"));
        final RobotSetting setting = new RobotSetting(null, linkedSetting);
        final Entry<String, RobotSetting> entry = new SimpleEntry<>("Setting", setting);

        assertThat(labelsAt(entry, 1)).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forColumnsGreaterThanSecondInKwBasedSettingWithOrdinaryKeyword() {
        final SuiteSetup setup = new SuiteSetup(RobotToken.create("[Setup]"));
        setup.setKeywordName("keyword");
        setup.addArgument("arg1");
        setup.addArgument("arg2");
        final RobotSetting setting = new RobotSetting(null, setup);
        final Entry<String, RobotSetting> entry = new SimpleEntry<>("Setting", setting);

        for (int i = 2; i < 10; i++) {
            assertThat(labelsAt(entry, i)).isEmpty();
        }
    }

    @Test
    public void labelIsAdded_forNestedKeywordsUsedInsideSpecialKeywordInKwBasedSetting() {
        final SuiteSetup setup = new SuiteSetup(RobotToken.create("[Setup]"));
        setup.setKeywordName("Run Keyword If");
        setup.addArgument("condition");
        setup.addArgument("kw");
        setup.addArgument("ELSE");
        setup.addArgument("kw");
        final RobotSetting setting = new RobotSetting(null, setup);
        final Entry<String, RobotSetting> entry = new SimpleEntry<>("Setting", setting);

        assertThat(labelsAt(entry, 0)).isEmpty();
        assertThat(labelsAt(entry, 1)).containsOnly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
        assertThat(labelsAt(entry, 2)).isEmpty();
        assertThat(labelsAt(entry, 3)).containsOnly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
        assertThat(labelsAt(entry, 4)).isEmpty();
        assertThat(labelsAt(entry, 5)).containsOnly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
    }

    private static List<String> labelsAt(final Entry<String, RobotSetting> entry, final int column) {
        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(entry);

        final LabelStack labels = new LabelStack();
        final SettingsActionNamesLabelAccumulator labelAccumulator = new SettingsActionNamesLabelAccumulator(
                dataProvider);
        labelAccumulator.accumulateConfigLabels(labels, column, 0);
        return labels.getLabels();
    }
}