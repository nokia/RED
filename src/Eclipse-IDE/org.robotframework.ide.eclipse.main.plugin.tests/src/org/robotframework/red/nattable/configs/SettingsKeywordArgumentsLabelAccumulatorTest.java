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
import static org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries.createStdLib;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.robotframework.red.junit.jupiter.Managed;
import org.robotframework.red.junit.jupiter.PreferencesExtension;
import org.robotframework.red.junit.jupiter.PreferencesUpdater;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith({ ProjectExtension.class, PreferencesExtension.class })
public class SettingsKeywordArgumentsLabelAccumulatorTest {

    @Project(createDefaultRedXml = true)
    static IProject project;

    private static RobotModel robotModel;

    @Managed
    PreferencesUpdater prefsUpdater;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        createFile(project, "resource.robot",
                "*** Settings ***",
                "Test Template  Limited Args",
                "Suite Setup",
                "Suite Teardown  Unknown",
                "Test Setup  Limited Args  1  2  #comment",
                "Test Teardown  Limited Args  1  2",
                "Library  UserLib");
        createFile(project, "nested.robot",
                "*** Settings ***",
                "Suite Setup  Run Keyword",
                "Suite Teardown  Wait Until Keyword Succeeds  1  2  Limited Args  1",
                "Test Setup  Run Keywords  Limited Args  1  AND  Limited Args  3",
                "Test Teardown  Run Keyword If  c1  Limited Args  1",
                "Library  UserLib");

        robotModel = RedPlugin.getModelManager().getModel();
        final RobotProject robotProject = robotModel.createRobotProject(project);

        robotProject.setReferencedLibraries(
                createRefLib("UserLib", KeywordSpecification.create("Limited Args", "a", "b", "c", "d=10", "e=20")));

        robotProject.setStandardLibraries(createStdLib("BuiltIn",
                KeywordSpecification.create("Run Keyword", "name", "*args"),
                KeywordSpecification.create("Run Keyword If", "condition", "name", "*args"),
                KeywordSpecification.create("Run Keywords", "*kws"),
                KeywordSpecification.create("Repeat Keyword", "repeat", "name", "*args"),
                KeywordSpecification.create("Wait Until Keyword Succeeds", "retry", "interval", "name", "*args")));
    }

    @AfterAll
    public static void afterSuite() {
        RedPlugin.getModelManager().dispose();
        robotModel = null;
    }

    @BeforeEach
    public void before() {
        prefsUpdater.setValue(RedPreferences.KEYWORD_ARGUMENTS_CELL_COLORING, true);
    }

    @Test
    public void labelsAreNotAdded_whenColoringIsDisabledInPreferences() {
        prefsUpdater.setValue(RedPreferences.KEYWORD_ARGUMENTS_CELL_COLORING, false);

        final RobotSuiteFile file = robotModel.createSuiteFile(getFile(project, "resource.robot"));
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
        final RobotSuiteFile file = robotModel.createSuiteFile(getFile(project, "resource.robot"));
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
        final RobotSuiteFile file = robotModel.createSuiteFile(getFile(project, "resource.robot"));
        final RobotSetting setting = (RobotSetting) file.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(3);
        final Entry<String, RobotSetting> entry = new SimpleEntry<>("Setting", setting);

        assertThat(labelsAt(file, entry, 9)).isEmpty();
    }

    @Test
    public void labelsAreNotAdded_whenSettingIsNull() {
        final RobotSuiteFile file = robotModel.createSuiteFile(getFile(project, "resource.robot"));
        final Entry<String, RobotSetting> entry = new SimpleEntry<>("Setting", null);

        for (int column = 2; column < 9; column++) {
            assertThat(labelsAt(file, entry, column)).isEmpty();
        }
    }

    @Test
    public void labelsAreNotAdded_whenSettingIsNotSetupOrTeardown() {
        final RobotSuiteFile file = robotModel.createSuiteFile(getFile(project, "resource.robot"));
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
        final RobotSuiteFile file = robotModel.createSuiteFile(getFile(project, "resource.robot"));
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
        final RobotSuiteFile file = robotModel.createSuiteFile(getFile(project, "resource.robot"));
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
        final RobotSuiteFile file = robotModel.createSuiteFile(getFile(project, "resource.robot"));
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

    @Test
    public void labelsAreAdded_forEmptyNestedKeywordCall() {
        final RobotSuiteFile file = robotModel.createSuiteFile(getFile(project, "nested.robot"));
        final RobotSetting setting = (RobotSetting) file.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(0);
        final Entry<String, RobotSetting> entry = new SimpleEntry<>("Setting", setting);

        assertThat(labelsAt(file, entry, 2)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, entry, 3)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, entry, 4)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, entry, 5)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, entry, 6)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, entry, 7)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, entry, 8)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
    }

    @Test
    public void labelsAreAdded_forNotEmptyNestedKeywordCall() {
        final RobotSuiteFile file = robotModel.createSuiteFile(getFile(project, "nested.robot"));
        final RobotSetting setting = (RobotSetting) file.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(1);
        final Entry<String, RobotSetting> entry = new SimpleEntry<>("Setting", setting);

        assertThat(labelsAt(file, entry, 2)).isEmpty();
        assertThat(labelsAt(file, entry, 3)).isEmpty();
        assertThat(labelsAt(file, entry, 4)).isEmpty();
        assertThat(labelsAt(file, entry, 5)).isEmpty();
        assertThat(labelsAt(file, entry, 6)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, entry, 7)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, entry, 8)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
    }

    @Test
    public void labelsAreAdded_forRunKeywordsCall() {
        final RobotSuiteFile file = robotModel.createSuiteFile(getFile(project, "nested.robot"));
        final RobotSetting setting = (RobotSetting) file.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(2);
        final Entry<String, RobotSetting> entry = new SimpleEntry<>("Setting", setting);

        assertThat(labelsAt(file, entry, 2)).isEmpty();
        assertThat(labelsAt(file, entry, 3)).isEmpty();
        assertThat(labelsAt(file, entry, 4)).isEmpty();
        assertThat(labelsAt(file, entry, 5)).isEmpty();
        assertThat(labelsAt(file, entry, 6)).isEmpty();
        assertThat(labelsAt(file, entry, 7)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, entry, 8)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
    }

    @Test
    public void labelsAreAdded_forRunKeywordIfCall() {
        final RobotSuiteFile file = robotModel.createSuiteFile(getFile(project, "nested.robot"));
        final RobotSetting setting = (RobotSetting) file.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(3);
        final Entry<String, RobotSetting> entry = new SimpleEntry<>("Setting", setting);

        assertThat(labelsAt(file, entry, 2)).isEmpty();
        assertThat(labelsAt(file, entry, 3)).isEmpty();
        assertThat(labelsAt(file, entry, 4)).isEmpty();
        assertThat(labelsAt(file, entry, 5)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, entry, 6)).containsExactly(TableConfigurationLabels.MISSING_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, entry, 7)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
        assertThat(labelsAt(file, entry, 8)).containsExactly(TableConfigurationLabels.OPTIONAL_ARGUMENT_CONFIG_LABEL);
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
