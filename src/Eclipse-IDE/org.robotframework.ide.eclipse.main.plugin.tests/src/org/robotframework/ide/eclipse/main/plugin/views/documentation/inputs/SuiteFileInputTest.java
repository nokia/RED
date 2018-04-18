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

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.libraries.Documentation.DocFormat;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ProjectProvider;


public class SuiteFileInputTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(SuiteFileInputTest.class);

    @Test
    public void properSuiteDocUriIsProvidedForInput() throws Exception {
        final RobotSuiteFile suite = createSuite("doc", "kw1", "kw2");
        final SuiteFileInput input = new SuiteFileInput(suite);

        final String fileUri = suite.getSuiteFile().getFile().getLocationURI().toString();
        assertThat(input.getInputUri().toString()).isEqualTo(fileUri + "?show_doc=true&suite=");
    }

    @Test
    public void theInputContainsGivenSuite() throws Exception {
        final RobotSuiteFile suite = createSuite("doc", "kw1", "kw2");
        final SuiteFileInput input = new SuiteFileInput(suite);

        assertThat(input.contains("suite")).isFalse();
        assertThat(input.contains(suite.getFile())).isFalse();
        assertThat(input.contains(suite)).isTrue();
    }

    @Test
    public void properHtmlIsReturned_forSuiteFile() throws Exception {
        final RobotRuntimeEnvironment env = mock(RobotRuntimeEnvironment.class);
        when(env.createHtmlDoc(any(String.class), eq(DocFormat.ROBOT))).thenReturn("doc");

        final RobotSuiteFile suite = createSuite("doc", "kw1", "kw2");
        final SuiteFileInput input = new SuiteFileInput(suite);

        final String fileUri = suite.getFile().getLocationURI().toString();
        final String fileLabel = suite.getFile().getFullPath().toString();

        final String html = input.provideHtml(env);
        assertThat(html).contains("<a href=\"" + fileUri + "\">" + fileLabel + "</a>");
        assertThat(html).contains("doc");
        assertThat(html).containsPattern("<h\\d.*>Introduction</h\\d>");
        assertThat(html).containsPattern("<h\\d.*>Shortcuts</h\\d>");
        assertThat(html).containsPattern("kw1.*kw2");
    }

    @Test
    public void properRawDocumentationIsReturned_forSuiteFile() throws Exception {
        final RobotSuiteFile suite = createSuite("doc", "kw1", "kw2");
        final SuiteFileInput input = new SuiteFileInput(suite);

        final String fileLabel = suite.getFile().getFullPath().toString();

        final String raw = input.provideRawText();
        assertThat(raw).contains("Name: suite");
        assertThat(raw).contains("Source: " + fileLabel);
        assertThat(raw).contains("doc");
    }

    private static RobotSuiteFile createSuite(final String documentation, final String... keywords) throws Exception {
        final List<String> lines = newArrayList(
                "*** Settings ***",
                "Documentation  " + documentation,
                "*** Keywords ***");
        lines.addAll(Arrays.asList(keywords));

        final IFile file = projectProvider.createFile("suite.robot", lines.toArray(new String[0]));
        return new RobotModel().createSuiteFile(file);
    }
}
