/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries.createRefLib;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.KeywordUsagesFinder;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableConfigurationLabels;
import org.robotframework.red.junit.PreferenceUpdater;
import org.robotframework.red.junit.ProjectProvider;

public class SettingsKeywordArgumentsLabelAccumulatorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(SettingsKeywordArgumentsLabelAccumulatorTest.class);

    @Rule
    public PreferenceUpdater preferenceUpdater = new PreferenceUpdater();

    private static RobotModel robotModel;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("resource.robot",
                "*** Settings ***",
                "Test Template  Limited Args",
                "Suite Setup",
                "Suite Teardown  Unknown",
                "Test Setup  Limited Args  1  2  #comment",
                "Test Teardown  Limited Args  1  2",
                "Library  UserLib");

        robotModel = RedPlugin.getModelManager().getModel();
        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());

        robotProject.setReferencedLibraries(
                createRefLib("UserLib", KeywordSpecification.create("Limited Args", "a", "b", "c", "d=10", "e=20")));

        projectProvider.configure();
    }

    @AfterClass
    public static void afterSuite() {
        RedPlugin.getModelManager().dispose();
        robotModel = null;
    }

    @Before
    public void before() {
        preferenceUpdater.setValue(RedPreferences.KEYWORD_ARGUMENTS_CELL_COLORING, true);
    }

    @Test
    public void labelsAreNotAdded_whenColoringIsDisabledInPreferences() {
        preferenceUpdater.setValue(RedPreferences.KEYWORD_ARGUMENTS_CELL_COLORING, false);

        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("resource.robot"));
        final RobotSetting setting = (RobotSetting) file.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(4);
        final Entry<String, RobotSetting> entry = new SimpleEntry<>("Setting", setting);

        for (int column = 2; column < 9; column++) {
            assertThat(labelsAt(file, entry, column)).isEmpty();
        }
    }

    @Test
    public void labelsAreNotAdded_forFirstTwoColumns() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("resource.robot"));
        final RobotSetting setting = (RobotSetting) file.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(4);
        final Entry<String, RobotSetting> entry = new SimpleEntry<>("Setting", setting);

        assertThat(labelsAt(file, entry, 0)).isEmpty();
        assertThat(labelsAt(file, entry, 1)).isEmpty();
    }

    @Test
    public void labelsAreNotAdded_forCommentColumn() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("resource.robot"));
        final RobotSetting setting = (RobotSetting) file.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(3);
        final Entry<String, RobotSetting> entry = new SimpleEntry<>("Setting", setting);

        assertThat(labelsAt(file, entry, 9)).isEmpty();
    }

    @Test
    public void labelsAreNotAdded_whenSettingIsNull() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("resource.robot"));
        final Entry<String, RobotSetting> entry = new SimpleEntry<>("Setting", null);

        for (int column = 2; column < 9; column++) {
            assertThat(labelsAt(file, entry, column)).isEmpty();
        }
    }

    @Test
    public void labelsAreNotAdded_whenSettingIsNotSetupOrTeardown() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("resource.robot"));
        final RobotSetting setting = (RobotSetting) file.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(0);
        final Entry<String, RobotSetting> entry = new SimpleEntry<>("Setting", setting);

        for (int column = 2; column < 9; column++) {
            assertThat(labelsAt(file, entry, column)).isEmpty();
        }
    }

    @Test
    public void labelsAreNotAdded_whenSettingIsDisabled() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("resource.robot"));
        final RobotSetting setting = (RobotSetting) file.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(1);
        final Entry<String, RobotSetting> entry = new SimpleEntry<>("Setting", setting);

        for (int column = 2; column < 9; column++) {
            assertThat(labelsAt(file, entry, column)).isEmpty();
        }
    }

    @Test
    public void labelsAreNotAdded_whenSettingIsUnknown() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("resource.robot"));
        final RobotSetting setting = (RobotSetting) file.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(2);
        final Entry<String, RobotSetting> entry = new SimpleEntry<>("Setting", setting);

        for (int column = 2; column < 9; column++) {
            assertThat(labelsAt(file, entry, column)).isEmpty();
        }
    }

    @Test
    public void labelsAreAdded_forKeywordWithLimitedArgumentList() {
        final RobotSuiteFile file = robotModel.createSuiteFile(projectProvider.getFile("resource.robot"));
        final RobotSetting setting = (RobotSetting) file.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(4);
        final Entry<String, RobotSetting> entry = new SimpleEntry<>("Setting", setting);

        assertThat(labelsAt(file, entry, 2)).isEmpty();
        assertThat(labelsAt(file, entry, 3)).isEmpty();
        assertThat(labelsAt(file, entry, 4)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, entry, 5)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, entry, 6)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, entry, 7)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, entry, 8)).containsExactly(TableConfigurationLabels.REDUNDANT_ARGUMENT_CONFIG_LABEL);
    }

    private static List<String> labelsAt(final RobotSuiteFile file, final Entry<String, RobotSetting> entry,
            final int column) {
        final IRowDataProvider<?> dataProvider = createDataProvider(entry);
        final KeywordUsagesFinder keywordFinder = createKeywordFinder(file);
        final SettingsKeywordArgumentsLabelAccumulator labelAccumulator = new SettingsKeywordArgumentsLabelAccumulator(
                dataProvider, keywordFinder);
        final LabelStack labels = new LabelStack();
        labelAccumulator.accumulateConfigLabels(labels, column, 0);
        return labels.getLabels();
    }

    private static IRowDataProvider<Object> createDataProvider(final Entry<String, RobotSetting> entry) {
        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(anyInt())).thenReturn(entry);
        if (entry.getValue() != null) {
            final List<RobotToken> tokens = ((AModelElement<?>) entry.getValue().getLinkedElement()).getElementTokens();
            for (int column = 0; column < 10; column++) {
                final String value = column < tokens.size() ? tokens.get(column).getText() : "";
                when(dataProvider.getDataValue(eq(column), anyInt())).thenReturn(value);
            }
        }
        when(dataProvider.getColumnCount()).thenReturn(10);
        return dataProvider;
    }

    private static KeywordUsagesFinder createKeywordFinder(final RobotSuiteFile file) {
        final KeywordUsagesFinder keywordFinder = new KeywordUsagesFinder(() -> file);
        keywordFinder.refresh().join();
        return keywordFinder;
    }
}
