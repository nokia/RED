/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeElementsColumnsPropertyAccessor;
import org.robotframework.red.junit.jupiter.IntegerPreference;
import org.robotframework.red.junit.jupiter.PreferencesExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith({ ProjectExtension.class, PreferencesExtension.class })
public class CasesDataProviderTest {

    @Project
    static IProject project;

    private final CasesDataProvider dataProvider = new CasesDataProvider(
            new CodeElementsColumnsPropertyAccessor(null, null), null);

    @Test
    public void columnsAreCountedCorrectly_whenTestCasesSectionIsEmpty() throws Exception {
        dataProvider.setInput(createCasesSection("*** Test Cases ***"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(6);
    }

    @Test
    public void columnsAreCountedCorrectly_whenTestCaseIsEmpty() throws Exception {
        dataProvider.setInput(createCasesSection("*** Test Cases ***",
                "tc"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(6);
    }

    @Test
    public void columnsAreCountedCorrectly_whenTestCaseSettingArgumentsDoNotExceedLimit() throws Exception {
        dataProvider.setInput(createCasesSection("*** Test Cases ***",
                "tc",
                "  [Tags]    a    b    c    d"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(6);
    }

    @Test
    public void columnsAreCountedCorrectly_whenTestCaseSettingArgumentsExceedLimit() throws Exception {
        dataProvider.setInput(createCasesSection("*** Test Cases ***",
                "tc",
                "  [Tags]    a    b    c    d    e    f    g"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(9);
    }

    @Test
    public void columnsAreCountedCorrectly_whenTestCaseKeywordCallArgumentsDoNotExceedLimit() throws Exception {
        dataProvider.setInput(createCasesSection("*** Test Cases ***",
                "tc",
                "  Log Many    a    b    c    d"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(6);
    }

    @Test
    public void columnsAreCountedCorrectly_whenTestCaseKeywordCallArgumentsExceedLimit() throws Exception {
        dataProvider.setInput(createCasesSection("*** Test Cases ***",
                "tc",
                "  Log Many    a    b    c    d    e    f"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(8);
    }

    @Test
    public void columnsAreCountedCorrectly_whenTestCaseDocumentationExceedsLimit() throws Exception {
        dataProvider.setInput(createCasesSection("*** Test Cases ***",
                "tc",
                "  [Documentation]    a    b    c    d    e    f    a    b    c    d    e    f"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(6);
    }

    @Test
    public void columnsAreCountedCorrectly_whenTestCasesSectionContainsManyTestCases() throws Exception {
        dataProvider.setInput(createCasesSection("*** Test Cases ***",
                "tc 1",
                "  Log Many    ${a}    ${b}    ${c}    ${d}    ${e}",
                "tc 2",
                "  [Setup]    Log Many    ${a}    ${b}    ${c}    ${d}    ${e}",
                "  Log Many    message",
                "tc 3",
                "  Log Many    ${a}    ${b}    ${c}    ${d}    ${e}    ${f}    ${g}    ${h}",
                "tc 4",
                "  Log    message",
                "tc 5",
                "  [Documentation]    a    b    c    d    e    f    a    b    c    d    e    f",
                "  Log    message"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(10);
    }

    @IntegerPreference(key = RedPreferences.MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS, value = 15)
    @Test
    public void columnsAreCountedCorrectly_whenMinimalArgumentsColumnsFieldIsChangedInPreferences() throws Exception {
        dataProvider.setInput(createCasesSection("*** Test Cases ***",
                "tc",
                "  Log Many    ${a}    ${b}    ${c}"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(16);
    }

    private RobotCasesSection createCasesSection(final String... lines) throws Exception {
        final IFile file = createFile(project, "file.robot", lines);
        final RobotModel model = new RobotModel();
        return model.createSuiteFile(file).findSection(RobotCasesSection.class).get();
    }

}
