/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.red.junit.jupiter.PreferencesExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.junit.jupiter.StringPreference;
import org.robotframework.red.nattable.edit.CellEditorValueValidator.CellEditorValueValidationException;

@ExtendWith({ PreferencesExtension.class, ProjectExtension.class })
public class DefaultRedCellEditorValueValidatorTest {

    @Project
    static IProject project;

    private static RobotModel robotModel;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        robotModel = RedPlugin.getModelManager().getModel();

        createFile(project, "space_separator.robot",
                "*** Settings ***",
                "Library  OperatingSystem",
                "*** Variables ***",
                "${MESSAGE}  Hello, world!",
                "*** Test Cases ***",
                "My Test",
                "  Log  123",
                "  Log  ${MESSAGE}");

        createFile(project, "pipe_separator.robot",
                "| *** Settings ***   |",
                "| Library            | OperatingSystem |",
                "| *** Variables ***  |",
                "| ${MESSAGE}         | Hello, world!   |",
                "| *** Test Cases *** |",
                "| My Test            |",
                "|                    | Log             | 123          |",
                "|                    | Log             | ${MESSAGE}   |");
    }

    @Test
    public void doNothing_whenValueIsNull() {
        final IRowDataProvider<?> dataProvider = mock(IRowDataProvider.class);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        validator.validate(null, 0);

        verifyNoInteractions(dataProvider);
    }

    @Test
    public void noErrorIsThrown_forCorrectValueInNewElement() {
        final IRowDataProvider<?> dataProvider = mock(IRowDataProvider.class);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        validator.validate("correct value", 0);
    }

    @Test
    public void noErrorIsThrown_forValueWithPipeSeparatorInNewElement() {
        final IRowDataProvider<?> dataProvider = mock(IRowDataProvider.class);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        validator.validate("value | separated", 0);
    }

    @Test
    public void errorIsThrown_forValueWithSpaceSeparatorInNewElement() {
        final IRowDataProvider<?> dataProvider = mock(IRowDataProvider.class);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        assertThatExceptionOfType(CellEditorValueValidationException.class)
                .isThrownBy(() -> validator.validate("value  separated", 0))
                .withMessage("Single entry cannot contain cells separator");
    }

    @StringPreference(key = RedPreferences.SEPARATOR_MODE, value = "FILE_TYPE_DEPENDENT")
    @StringPreference(key = RedPreferences.SEPARATOR_TO_USE, value = "s|s")
    @Test
    public void errorIsThrown_forValueWithPipeSeparatorInNewElement() {
        final IRowDataProvider<?> dataProvider = mock(IRowDataProvider.class);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        assertThatExceptionOfType(CellEditorValueValidationException.class)
                .isThrownBy(() -> validator.validate("value | separated", 0))
                .withMessage("Single entry cannot contain cells separator");
    }

    @Test
    public void errorIsThrown_forValueStartingWithSpaceInNewElement() {
        final IRowDataProvider<?> dataProvider = mock(IRowDataProvider.class);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        assertThatExceptionOfType(CellEditorValueValidationException.class)
                .isThrownBy(() -> validator.validate(" value", 0))
                .withMessage("Space should be escaped");
    }

    @Test
    public void errorIsThrown_forValueEndingWithSpaceInNewElement() {
        final IRowDataProvider<?> dataProvider = mock(IRowDataProvider.class);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        assertThatExceptionOfType(CellEditorValueValidationException.class)
                .isThrownBy(() -> validator.validate("value ", 0))
                .withMessage("Space should be escaped");
    }

    @Test
    public void noErrorIsThrown_forValueEndingWithEscapedSpaceInNewElement() {
        final IRowDataProvider<?> dataProvider = mock(IRowDataProvider.class);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        validator.validate("value\\ ", 0);
    }

    @Test
    public void errorIsThrown_forValueWithSpaceSeparatorInNewEmptyRow() throws Exception {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "space_separator.robot"));
        final RobotCase robotCase = suiteFile.findSection(RobotCasesSection.class).get().getChildren().get(0);
        final AModelElement<?> linkedElement = new RobotEmptyRow<TestCase>();
        final RobotKeywordCall call = new RobotKeywordCall(robotCase, linkedElement);

        final IRowDataProvider<?> dataProvider = prepareDataProvider(call);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        assertThatExceptionOfType(CellEditorValueValidationException.class)
                .isThrownBy(() -> validator.validate("value  separated", 0))
                .withMessage("Single entry cannot contain cells separator");
    }

    @Test
    public void errorIsThrown_forValueWithSpaceSeparatorInKeywordCall() throws Exception {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "space_separator.robot"));
        final RobotKeywordCall call = suiteFile.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);

        final IRowDataProvider<?> dataProvider = prepareDataProvider(call);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        assertThatExceptionOfType(CellEditorValueValidationException.class)
                .isThrownBy(() -> validator.validate("value  separated", 0))
                .withMessage("Single entry cannot contain cells separator");
    }

    @Test
    public void errorIsThrown_forValueWithSpaceSeparatorInVariable() throws Exception {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "space_separator.robot"));
        final RobotVariable variable = suiteFile.findSection(RobotVariablesSection.class).get().getChildren().get(0);

        final IRowDataProvider<?> dataProvider = prepareDataProvider(variable);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        assertThatExceptionOfType(CellEditorValueValidationException.class)
                .isThrownBy(() -> validator.validate("value  separated", 0))
                .withMessage("Single entry cannot contain cells separator");
    }

    @Test
    public void errorIsThrown_forValueWithSpaceSeparatorInSetting() throws Exception {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "space_separator.robot"));
        final RobotSetting setting = (RobotSetting) suiteFile.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(0);

        final IRowDataProvider<?> dataProvider = prepareDataProvider(setting);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        assertThatExceptionOfType(CellEditorValueValidationException.class)
                .isThrownBy(() -> validator.validate("value  separated", 0))
                .withMessage("Single entry cannot contain cells separator");
    }

    @Test
    public void errorIsThrown_forValueWithPipeSeparatorInKeywordCall() throws Exception {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "pipe_separator.robot"));
        final RobotKeywordCall call = suiteFile.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);

        final IRowDataProvider<?> dataProvider = prepareDataProvider(call);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        assertThatExceptionOfType(CellEditorValueValidationException.class)
                .isThrownBy(() -> validator.validate("value | separated", 0))
                .withMessage("Single entry cannot contain cells separator");
    }

    @Test
    public void errorIsThrown_forValueWithPipeSeparatorInVariable() throws Exception {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "pipe_separator.robot"));
        final RobotVariable variable = suiteFile.findSection(RobotVariablesSection.class).get().getChildren().get(0);

        final IRowDataProvider<?> dataProvider = prepareDataProvider(variable);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        assertThatExceptionOfType(CellEditorValueValidationException.class)
                .isThrownBy(() -> validator.validate("value | separated", 0))
                .withMessage("Single entry cannot contain cells separator");
    }

    @Test
    public void errorIsThrown_forValueWithPipeSeparatorInSetting() throws Exception {
        final RobotSuiteFile suiteFile = robotModel.createSuiteFile(getFile(project, "pipe_separator.robot"));
        final RobotSetting setting = (RobotSetting) suiteFile.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(0);

        final IRowDataProvider<?> dataProvider = prepareDataProvider(setting);
        final DefaultRedCellEditorValueValidator validator = new DefaultRedCellEditorValueValidator(dataProvider);

        assertThatExceptionOfType(CellEditorValueValidationException.class)
                .isThrownBy(() -> validator.validate("value | separated", 0))
                .withMessage("Single entry cannot contain cells separator");
    }

    private static IRowDataProvider<Object> prepareDataProvider(final RobotFileInternalElement element) {
        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(anyInt())).thenReturn(element);
        return dataProvider;
    }

    private static IRowDataProvider<Object> prepareDataProvider(final RobotSetting setting) {
        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        final Entry<String, Object> entry = new SimpleEntry<>(setting.getName(), setting);
        when(dataProvider.getRowObject(anyInt())).thenReturn(entry);
        return dataProvider;
    }

}
