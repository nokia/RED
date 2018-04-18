/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.libraries.Documentation.DocFormat;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.red.junit.ProjectProvider;


public class TestCaseInputTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(TestCaseInputTest.class);

    @Test
    public void properTestCaseDocUriIsProvidedForInput() throws Exception {
        final RobotCase test = createTest("case");
        final TestCaseInput input = new TestCaseInput(test);

        final String fileUri = test.getSuiteFile().getFile().getLocationURI().toString();
        assertThat(input.getInputUri().toString()).isEqualTo(fileUri + "?show_doc=true&test=case");
    }

    @Test
    public void theInputContainsGivenTestCase() throws Exception {
        final RobotCase test = createTest("case");
        final TestCaseInput input = new TestCaseInput(test);

        assertThat(input.contains("case")).isFalse();
        assertThat(input.contains(test.getSuiteFile())).isFalse();
        assertThat(input.contains(test)).isTrue();
    }

    @Test
    public void properHtmlIsReturned_forTestCaseWithoutTemplate() throws Exception {
        final RobotRuntimeEnvironment env = mock(RobotRuntimeEnvironment.class);
        when(env.createHtmlDoc(any(String.class), eq(DocFormat.ROBOT))).thenReturn("doc");

        final RobotCase test = createTest("case");
        final TestCaseInput input = new TestCaseInput(test);

        final String fileUri = test.getSuiteFile().getFile().getLocationURI().toString();
        final String fileLabel = test.getSuiteFile().getFile().getFullPath().toString();

        final String html = input.provideHtml(env);
        assertThat(html).contains(fileUri + "?show_source=true&test=case\">" + fileLabel + "</a>");
        assertThat(html).contains(fileUri + "?show_doc=true&suite=\">Documentation</a>");

        assertThat(html).doesNotContainPattern("Template");
    }

    @Test
    public void properHtmlIsReturned_forTestCaseWithTemplate() throws Exception {
        final RobotRuntimeEnvironment env = mock(RobotRuntimeEnvironment.class);
        when(env.createHtmlDoc(any(String.class), eq(DocFormat.ROBOT))).thenReturn("doc");

        final RobotCase test = createTemplatedTest("case", "template kw");
        final TestCaseInput input = new TestCaseInput(test);

        final String fileUri = test.getSuiteFile().getFile().getLocationURI().toString();
        final String fileLabel = test.getSuiteFile().getFile().getFullPath().toString();

        final String html = input.provideHtml(env);
        assertThat(html).contains(fileUri + "?show_source=true&test=case\">" + fileLabel + "</a>");
        assertThat(html).contains(fileUri + "?show_doc=true&suite=\">Documentation</a>");

        assertThat(html).contains("Template", "template kw");
    }

    @Test
    public void properRawDocumentationIsReturned_forTestCaseWithoutTemplate() throws Exception {
        final RobotCase test = createTest("case");
        final TestCaseInput input = new TestCaseInput(test);

        final String fileLabel = test.getSuiteFile().getFile().getFullPath().toString();

        final String raw = input.provideRawText();
        assertThat(raw).contains("Name: case");
        assertThat(raw).contains("Source: " + fileLabel);

        assertThat(raw).doesNotContainPattern("Template");
    }

    @Test
    public void properRawDocumentationIsReturned_forTestCaseWithTemplate() throws Exception {
        final RobotCase test = createTemplatedTest("case", "template kw");
        final TestCaseInput input = new TestCaseInput(test);

        final String fileLabel = test.getSuiteFile().getFile().getFullPath().toString();

        final String raw = input.provideRawText();
        assertThat(raw).contains("Name: case");
        assertThat(raw).contains("Source: " + fileLabel);
        assertThat(raw).contains("Template: template kw");
    }

    private static RobotCase createTest(final String testName) throws Exception {
        final IFile file = projectProvider.createFile("suite.robot",
                "*** Test Cases ***",
                testName,
                "  call  arg");
        return new RobotModel().createSuiteFile(file).findSection(RobotCasesSection.class).get().getChildren().get(0);
    }

    private static RobotCase createTemplatedTest(final String testName, final String template) throws Exception {
        final IFile file = projectProvider.createFile("suite.robot",
                "*** Test Cases ***",
                testName,
                "  [Template]  " + template,
                "  x  y");
        return new RobotModel().createSuiteFile(file).findSection(RobotCasesSection.class).get().getChildren().get(0);
    }
}
