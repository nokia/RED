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
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;

import org.eclipse.ui.IWorkbenchPage;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.libraries.Documentation.DocFormat;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibraryConstructor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.ProjectProvider;


public class KeywordSpecificationInputTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(KeywordSpecificationInputTest.class);

    @Test
    public void properLibraryDocUriIsProvidedForInput() throws URISyntaxException {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final KeywordSpecification kwSpec = KeywordSpecification.create("kw");
        final LibrarySpecification libSpec = LibrarySpecification.create("library", kwSpec);
        final KeywordSpecificationInput input = new KeywordSpecificationInput(robotProject, libSpec, kwSpec);

        assertThat(input.getInputUri().toString())
                .isEqualTo("library:/KeywordSpecificationInputTest/library/kw?show_doc=true");
    }

    @Test
    public void theInputContainsGivenSpecification() {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final KeywordSpecification kwSpec = KeywordSpecification.create("kw");
        final LibrarySpecification libSpec = LibrarySpecification.create("library", kwSpec);
        final KeywordSpecificationInput input = new KeywordSpecificationInput(robotProject, libSpec, kwSpec);

        assertThat(input.contains("library")).isFalse();
        assertThat(input.contains(projectProvider.getProject())).isFalse();
        assertThat(input.contains(libSpec)).isFalse();
        assertThat(input.contains(kwSpec)).isTrue();
    }

    @Test
    public void properHtmlIsReturnedForKeyword() {
        final RobotRuntimeEnvironment env = mock(RobotRuntimeEnvironment.class);
        when(env.createHtmlDoc(any(String.class), eq(DocFormat.ROBOT))).thenReturn("doc");

        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final KeywordSpecification kwSpec = KeywordSpecification.create("kw");
        kwSpec.setArguments(newArrayList("a", "b=10"));
        kwSpec.setDocumentation("kw documentation");
        final LibrarySpecification libSpec = LibrarySpecification.create("library", kwSpec);
        libSpec.setFormat("ROBOT");

        final KeywordSpecificationInput input = new KeywordSpecificationInput(robotProject, libSpec, kwSpec);
        final String html = input.provideHtml(env);
        assertThat(html)
                .contains("<a href=\"library:/KeywordSpecificationInputTest/library/kw?show_source=true\">library</a>");
        assertThat(html)
                .contains("<a href=\"library:/KeywordSpecificationInputTest/library?show_doc=true\">Documentation</a>");
        assertThat(html).contains("doc");
        assertThat(html).contains("[a, b=10]");
    }

    @Test
    public void properRawDocumentationIsReturnedForLibrary() {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final KeywordSpecification kwSpec = KeywordSpecification.create("kw");
        kwSpec.setArguments(newArrayList("a", "b=10"));
        kwSpec.setDocumentation("kw documentation");
        final LibrarySpecification libSpec = LibrarySpecification.create("library", kwSpec);
        libSpec.setFormat("ROBOT");

        final KeywordSpecificationInput input = new KeywordSpecificationInput(robotProject, libSpec, kwSpec);
        final String raw = input.provideRawText();

        assertThat(raw).contains("Name: kw");
        assertThat(raw).contains("Source: library");
        assertThat(raw).contains("Arguments: [a, b=10]");
        assertThat(raw).contains("kw documentation");
    }

    @Test
    public void nothingHappensWhenTryingToShowTheInput() throws Exception {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final KeywordSpecification kwSpec = KeywordSpecification.create("kw");
        final LibrarySpecification libSpec = LibrarySpecification.create("library", kwSpec);
        final KeywordSpecificationInput input = new KeywordSpecificationInput(robotProject, libSpec, kwSpec);

        final IWorkbenchPage page = mock(IWorkbenchPage.class);
        input.showInput(page);

        verifyZeroInteractions(page);
    }

    @Test
    public void twoInputsAreEqual_whenProjectIsSameAndSpecificationsAreEqual() {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final KeywordSpecification kwSpec1 = KeywordSpecification.create("kw");
        kwSpec1.setArguments(newArrayList("a", "b=10"));
        kwSpec1.setDocumentation("kw documentation");
        final LibrarySpecification libSpec1 = LibrarySpecification.create("library", kwSpec1);
        libSpec1.setFormat("ROBOT");
        libSpec1.setScope("scope");
        libSpec1.setVersion("1.0");
        libSpec1.setConstructor(LibraryConstructor.create("constructor", newArrayList("arg1", "arg2=42")));
        libSpec1.setDocumentation("lib documentation");

        final KeywordSpecification kwSpec2 = KeywordSpecification.create("kw");
        kwSpec2.setArguments(newArrayList("a", "b=10"));
        kwSpec2.setDocumentation("kw documentation");
        final LibrarySpecification libSpec2 = LibrarySpecification.create("library", kwSpec2);
        libSpec2.setFormat("ROBOT");
        libSpec2.setScope("scope");
        libSpec2.setVersion("1.0");
        libSpec2.setConstructor(LibraryConstructor.create("constructor", newArrayList("arg1", "arg2=42")));
        libSpec2.setDocumentation("lib documentation");

        final KeywordSpecificationInput input1 = new KeywordSpecificationInput(robotProject, libSpec1, kwSpec1);
        final KeywordSpecificationInput input2 = new KeywordSpecificationInput(robotProject, libSpec2, kwSpec2);

        assertThat(input1.equals(input2)).isTrue();
        assertThat(input1.hashCode()).isEqualTo(input2.hashCode());
    }

    @Test
    public void twoInputsAreNotEqual_whenProjectIsDifferent() {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final KeywordSpecification kwSpec1 = KeywordSpecification.create("kw");
        kwSpec1.setArguments(newArrayList("a", "b=10"));
        kwSpec1.setDocumentation("kw documentation");
        final LibrarySpecification libSpec1 = LibrarySpecification.create("library", kwSpec1);
        libSpec1.setFormat("ROBOT");
        libSpec1.setScope("scope");
        libSpec1.setVersion("1.0");
        libSpec1.setConstructor(LibraryConstructor.create("constructor", newArrayList("arg1", "arg2=42")));
        libSpec1.setDocumentation("lib documentation");

        final KeywordSpecification kwSpec2 = KeywordSpecification.create("kw");
        kwSpec2.setArguments(newArrayList("a", "b=10"));
        kwSpec2.setDocumentation("kw documentation");
        final LibrarySpecification libSpec2 = LibrarySpecification.create("library", kwSpec2);
        libSpec2.setFormat("ROBOT");
        libSpec2.setScope("scope");
        libSpec2.setVersion("1.0");
        libSpec2.setConstructor(LibraryConstructor.create("constructor", newArrayList("arg1", "arg2=42")));
        libSpec2.setDocumentation("lib documentation");

        final KeywordSpecificationInput input1 = new KeywordSpecificationInput(robotProject, libSpec1, kwSpec1);
        final KeywordSpecificationInput input2 = new KeywordSpecificationInput(mock(RobotProject.class), libSpec2,
                kwSpec2);

        assertThat(input1.equals(input2)).isFalse();
    }

    @Test
    public void twoInputsAreNotEqual_whenSpecificationsAreDifferent() {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final KeywordSpecification kwSpec1 = KeywordSpecification.create("kw");
        kwSpec1.setArguments(newArrayList("a", "b=10"));
        kwSpec1.setDocumentation("kw documentation");

        final KeywordSpecification kwSpec2 = KeywordSpecification.create("kw2");
        kwSpec2.setArguments(newArrayList("a", "b=10"));
        kwSpec2.setDocumentation("kw documentation");

        final KeywordSpecification kwSpec3 = KeywordSpecification.create("kw");
        kwSpec3.setArguments(newArrayList("a", "c=10"));
        kwSpec3.setDocumentation("kw documentation");

        final KeywordSpecification kwSpec4 = KeywordSpecification.create("kw");
        kwSpec4.setArguments(newArrayList("a", "b=10"));
        kwSpec4.setDocumentation("kw doc");

        final LibrarySpecification libSpec = LibrarySpecification.create("library", kwSpec1, kwSpec2, kwSpec3, kwSpec4);
        libSpec.setFormat("ROBOT");
        libSpec.setScope("scope");
        libSpec.setVersion("1.0");
        libSpec.setConstructor(LibraryConstructor.create("constructor", newArrayList("arg1", "arg2=42")));
        libSpec.setDocumentation("lib documentation");

        final KeywordSpecificationInput input1 = new KeywordSpecificationInput(robotProject, libSpec, kwSpec1);
        final KeywordSpecificationInput input2 = new KeywordSpecificationInput(robotProject, libSpec, kwSpec2);
        final KeywordSpecificationInput input3 = new KeywordSpecificationInput(robotProject, libSpec, kwSpec3);
        final KeywordSpecificationInput input4 = new KeywordSpecificationInput(robotProject, libSpec, kwSpec4);

        assertThat(input1.equals(input2)).isFalse();
        assertThat(input1.equals(input3)).isFalse();
        assertThat(input1.equals(input4)).isFalse();
    }
}
