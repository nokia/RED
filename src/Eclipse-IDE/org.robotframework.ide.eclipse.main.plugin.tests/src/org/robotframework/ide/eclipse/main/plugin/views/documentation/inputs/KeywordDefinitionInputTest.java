/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.libraries.Documentation.DocFormat;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class KeywordDefinitionInputTest {

    @Project
    static IProject project;

    @Test
    public void properKeywordDocUriIsProvidedForInput() throws Exception {
        final RobotKeywordDefinition keyword = createKeyword("kw");
        final KeywordDefinitionInput input = new KeywordDefinitionInput(keyword);

        final String fileUri = keyword.getSuiteFile().getFile().getLocationURI().toString();
        assertThat(input.getInputUri().toString()).isEqualTo(fileUri + "?show_doc=true&keyword=kw");
    }

    @Test
    public void theInputContainsGivenKeyword() throws Exception {
        final RobotKeywordDefinition keyword = createKeyword("kw");
        final KeywordDefinitionInput input = new KeywordDefinitionInput(keyword);

        assertThat(input.contains("kw")).isFalse();
        assertThat(input.contains(keyword.getSuiteFile())).isFalse();
        assertThat(input.contains(keyword)).isTrue();
    }

    @Test
    public void properHtmlIsReturned_forKeywordDefinition() throws Exception {
        final IRuntimeEnvironment env = mock(IRuntimeEnvironment.class);
        when(env.createHtmlDoc(any(String.class), eq(DocFormat.ROBOT))).thenReturn("doc");

        final RobotKeywordDefinition keyword = createKeyword("kw", newArrayList("${x}", "${y}"));
        final KeywordDefinitionInput input = new KeywordDefinitionInput(keyword);

        final String fileUri = keyword.getSuiteFile().getFile().getLocationURI().toString();
        final String fileLabel = keyword.getSuiteFile().getFile().getFullPath().toString();

        final String html = input.provideHtml(env);
        assertThat(html).contains(fileUri + "?show_source=true&keyword=kw\">" + fileLabel + "</a>");
        assertThat(html).contains(fileUri + "?show_doc=true&suite=\">Documentation</a>");
        assertThat(html).contains("Arguments", "[x, y]");
    }

    @Test
    public void properRawDocumentationIsReturned_forKeywordDefinition() throws Exception {
        final RobotKeywordDefinition keyword = createKeyword("kw", newArrayList("${x}", "${y}"));
        final KeywordDefinitionInput input = new KeywordDefinitionInput(keyword);

        final String fileLabel = keyword.getSuiteFile().getFile().getFullPath().toString();

        final String raw = input.provideRawText();
        assertThat(raw).contains("Name: kw");
        assertThat(raw).contains("Source: " + fileLabel);
        assertThat(raw).contains("Arguments: [x, y]");
    }

    private static RobotKeywordDefinition createKeyword(final String keywordName) throws Exception {
        final IFile file = createFile(project, "suite.robot",
                "*** Keywords ***",
                keywordName,
                "  call  arg");
        return new RobotModel().createSuiteFile(file).findSection(RobotKeywordsSection.class).get().getChildren().get(
                0);
    }

    private static RobotKeywordDefinition createKeyword(final String keywordName, final List<String> arguments)
            throws Exception {
        final IFile file = createFile(project, "suite.robot",
                "*** Keywords ***",
                keywordName,
                "  [Arguments]  " + String.join("  ", arguments),
                "  call  arg");
        return new RobotModel().createSuiteFile(file).findSection(RobotKeywordsSection.class).get().getChildren().get(
                0);
    }
}
