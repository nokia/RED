/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.red.junit.jupiter.IntegerPreference;
import org.robotframework.red.junit.jupiter.PreferencesExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith({ ProjectExtension.class, PreferencesExtension.class })
public class KeywordsDataProviderTest {

    @Project
    static IProject project;

    private final KeywordsDataProvider dataProvider = new KeywordsDataProvider(
            new KeywordsColumnsPropertyAccessor(null, null), null);

    @Test
    public void columnsAreCountedCorrectly_whenKeywordsSectionIsEmpty() throws Exception {
        dataProvider.setInput(createKeywordsSection("*** Keywords ***"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(6);
    }

    @Test
    public void columnsAreCountedCorrectly_whenKeywordDefinitionIsEmpty() throws Exception {
        dataProvider.setInput(createKeywordsSection("*** Keywords ***",
                "kw"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(6);
    }

    @Test
    public void columnsAreCountedCorrectly_whenKeywordDefinitionContainsEmbeddedArguments() throws Exception {
        dataProvider.setInput(createKeywordsSection("*** Keywords ***",
                "kw ${a} embedded ${b} X ${c} Y ${d} Z ${e}"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(6);
    }

    @Test
    public void columnsAreCountedCorrectly_whenKeywordDefinitionArgumentsDoNotExceedLimit() throws Exception {
        dataProvider.setInput(createKeywordsSection("*** Keywords ***",
                "kw",
                "  [Arguments]    ${a}    ${b}    ${c}    ${d}}"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(6);
    }

    @Test
    public void columnsAreCountedCorrectly_whenKeywordDefinitionArgumentsExceedLimit() throws Exception {
        dataProvider.setInput(createKeywordsSection("*** Keywords ***",
                "kw",
                "  [Arguments]    ${a}    ${b}    ${c}    ${d}    ${e}"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(7);
    }

    @Test
    public void columnsAreCountedCorrectly_whenKeywordSettingArgumentsDoNotExceedLimit() throws Exception {
        dataProvider.setInput(createKeywordsSection("*** Keywords ***",
                "kw", "  [Tags]    a    b    c    d"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(6);
    }

    @Test
    public void columnsAreCountedCorrectly_whenKeywordSettingArgumentsExceedLimit() throws Exception {
        dataProvider.setInput(createKeywordsSection("*** Keywords ***",
                "kw",
                "  [Tags]    a    b    c    d    e    f    g"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(9);
    }

    @Test
    public void columnsAreCountedCorrectly_whenKeywordCallArgumentsDoNotExceedLimit() throws Exception {
        dataProvider.setInput(createKeywordsSection("*** Keywords ***",
                "kw",
                "  Log Many    a    b    c    d"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(6);
    }

    @Test
    public void columnsAreCountedCorrectly_whenKeywordCallArgumentsExceedLimit() throws Exception {
        dataProvider.setInput(createKeywordsSection("*** Keywords ***",
                "kw",
                "  Log Many    a    b    c    d    e    f"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(8);
    }

    @Test
    public void columnsAreCountedCorrectly_whenKeywordDocumentationExceedsLimit() throws Exception {
        dataProvider.setInput(createKeywordsSection("*** Keywords ***",
                "kw",
                "  [Documentation]    a    b    c    d    e    f    a    b    c    d    e    f"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(6);
    }

    @Test
    public void columnsAreCountedCorrectly_whenKeywordsSectionContainsManyKeywordDefinitions() throws Exception {
        dataProvider.setInput(createKeywordsSection("*** Keywords ***",
                "kw 1 ${a} ${b} ${c} ${d} ${e}",
                "  Log Many    ${a}    ${b}    ${c}    ${d}    ${e}",
                "kw 2",
                "  [Arguments]    ${a}    ${b}    ${c}    ${d}    ${e}    ${f}",
                "  Log Many    ${a}    ${b}    ${c}    ${d}    ${e}    ${f}",
                "kw 3",
                "  Log Many    ${a}    ${b}    ${c}    ${d}    ${e}    ${f}    ${g}    ${h}",
                "kw 4",
                "  Log    message",
                "kw 5",
                "  [Documentation]    a    b    c    d    e    f    a    b    c    d    e    f",
                "  Log    message"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(10);
    }

    @IntegerPreference(key = RedPreferences.MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS, value = 15)
    @Test
    public void columnsAreCountedCorrectly_whenMinimalArgumentsColumnsFieldIsChangedInPreferences() throws Exception {

        dataProvider.setInput(createKeywordsSection("*** Keywords ***",
                "kw",
                "  [Arguments]    ${a}    ${b}    ${c}"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(16);
    }

    private RobotKeywordsSection createKeywordsSection(final String... lines) throws Exception {
        final IFile file = createFile(project, "file.robot", lines);
        final RobotModel model = new RobotModel();
        return model.createSuiteFile(file).findSection(RobotKeywordsSection.class).get();
    }

}
