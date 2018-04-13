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


public class LibrarySpecificationInputTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(LibrarySpecificationInputTest.class);

    @Test
    public void properLibraryDocUriIsProvidedForInput() throws URISyntaxException {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final LibrarySpecification specification = LibrarySpecification.create("library");
        final LibrarySpecificationInput input = new LibrarySpecificationInput(robotProject, specification);

        assertThat(input.getInputUri().toString())
                .isEqualTo("library:/LibrarySpecificationInputTest/library?show_doc=true");
    }

    @Test
    public void theInputContainsGivenProjectAndSpecification() {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final LibrarySpecification specification = LibrarySpecification.create("library");
        final LibrarySpecificationInput input = new LibrarySpecificationInput(robotProject, specification);

        assertThat(input.contains("library")).isFalse();
        assertThat(input.contains(projectProvider.getProject())).isTrue();
        assertThat(input.contains(specification)).isTrue();
    }

    @Test
    public void properHtmlIsReturned_forLibraryWithoutConstructor() {
        final RobotRuntimeEnvironment env = mock(RobotRuntimeEnvironment.class);
        when(env.createHtmlDoc(any(String.class), eq(DocFormat.ROBOT))).thenReturn("doc");

        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final LibrarySpecification specification = LibrarySpecification.create("library",
                KeywordSpecification.create("kw1"), KeywordSpecification.create("kw2"));
        specification.setFormat("ROBOT");
        specification.setScope("scope");
        specification.setVersion("1.0");
        specification.setDocumentation("lib documentation");

        final LibrarySpecificationInput input = new LibrarySpecificationInput(robotProject, specification);
        final String html = input.provideHtml(env);
        assertThat(html)
                .contains("<a href=\"library:/LibrarySpecificationInputTest/library?show_source=true\">library</a>");
        assertThat(html).contains("scope");
        assertThat(html).contains("1.0");
        assertThat(html).contains("[]");
        assertThat(html).containsPattern("<h\\d.*>Introduction</h\\d>");
        assertThat(html).containsPattern("<h\\d.*>Shortcuts</h\\d>");
        assertThat(html).containsPattern("kw1.*kw2");

        assertThat(html).doesNotContainPattern("<h\\d.*>Importing</h\\d>");
    }

    @Test
    public void properHtmlIsReturned_forLibraryWithConstructor() {
        final RobotRuntimeEnvironment env = mock(RobotRuntimeEnvironment.class);
        when(env.createHtmlDoc(any(String.class), eq(DocFormat.ROBOT))).thenReturn("doc");

        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final LibrarySpecification specification = LibrarySpecification.create("library",
                KeywordSpecification.create("kw1"), KeywordSpecification.create("kw2"));
        specification.setFormat("ROBOT");
        specification.setScope("scope");
        specification.setVersion("1.0");
        specification.setConstructor(LibraryConstructor.create("constructor", newArrayList("arg1", "arg2=42")));
        specification.setDocumentation("lib documentation");

        final LibrarySpecificationInput input = new LibrarySpecificationInput(robotProject, specification);
        final String html = input.provideHtml(env);
        assertThat(html)
                .contains("<a href=\"library:/LibrarySpecificationInputTest/library?show_source=true\">library</a>");
        assertThat(html).contains("scope");
        assertThat(html).contains("1.0");
        assertThat(html).contains("[arg1, arg2=42]");
        assertThat(html).containsPattern("<h\\d.*>Introduction</h\\d>");
        assertThat(html).containsPattern("<h\\d.*>Shortcuts</h\\d>");
        assertThat(html).containsPattern("kw1.*kw2");

        assertThat(html).containsPattern("<h\\d.*>Importing</h\\d>");
    }

    @Test
    public void properRawDocumentationIsReturnedForLibrary() {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final LibrarySpecification specification = LibrarySpecification.create("library",
                KeywordSpecification.create("kw1"), KeywordSpecification.create("kw2"));
        specification.setFormat("ROBOT");
        specification.setScope("scope");
        specification.setVersion("1.0");
        specification.setConstructor(LibraryConstructor.create("constructor", newArrayList("arg1", "arg2=42")));
        specification.setDocumentation("lib documentation");

        final LibrarySpecificationInput input = new LibrarySpecificationInput(robotProject, specification);
        final String raw = input.provideRawText();

        assertThat(raw).contains("Version: 1.0");
        assertThat(raw).contains("Scope: scope");
        assertThat(raw).contains("Arguments: [arg1, arg2=42]");
        assertThat(raw).contains("lib documentation");
    }

    @Test
    public void nothingHappensWhenTryingToShowTheInput() throws Exception {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final LibrarySpecification specification = LibrarySpecification.create("library");
        final LibrarySpecificationInput input = new LibrarySpecificationInput(robotProject, specification);

        final IWorkbenchPage page = mock(IWorkbenchPage.class);
        input.showInput(page);

        verifyZeroInteractions(page);
    }

    @Test
    public void twoInputsAreEqual_whenProjectIsSameAndSpecificationsAreEqual() {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final LibrarySpecification specification1 = LibrarySpecification.create("library",
                KeywordSpecification.create("kw1"), KeywordSpecification.create("kw2"));
        specification1.setFormat("ROBOT");
        specification1.setScope("scope");
        specification1.setVersion("1.0");
        specification1.setConstructor(LibraryConstructor.create("constructor", newArrayList("arg1", "arg2=42")));
        specification1.setDocumentation("lib documentation");

        final LibrarySpecification specification2 = LibrarySpecification.create("library",
                KeywordSpecification.create("kw1"), KeywordSpecification.create("kw2"));
        specification2.setFormat("ROBOT");
        specification2.setScope("scope");
        specification2.setVersion("1.0");
        specification2.setConstructor(LibraryConstructor.create("constructor", newArrayList("arg1", "arg2=42")));
        specification2.setDocumentation("lib documentation");

        final LibrarySpecificationInput input1 = new LibrarySpecificationInput(robotProject, specification1);
        final LibrarySpecificationInput input2 = new LibrarySpecificationInput(robotProject, specification2);

        assertThat(input1.equals(input2)).isTrue();
        assertThat(input1.hashCode()).isEqualTo(input2.hashCode());
    }

    @Test
    public void twoInputsAreNotEqual_whenProjectIsDifferent() {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final LibrarySpecification specification1 = LibrarySpecification.create("library",
                KeywordSpecification.create("kw1"), KeywordSpecification.create("kw2"));
        specification1.setFormat("ROBOT");
        specification1.setScope("scope");
        specification1.setVersion("1.0");
        specification1.setConstructor(LibraryConstructor.create("constructor", newArrayList("arg1", "arg2=42")));
        specification1.setDocumentation("lib documentation");

        final LibrarySpecification specification2 = LibrarySpecification.create("library",
                KeywordSpecification.create("kw1"), KeywordSpecification.create("kw2"));
        specification2.setFormat("ROBOT");
        specification2.setScope("scope");
        specification2.setVersion("1.0");
        specification2.setConstructor(LibraryConstructor.create("constructor", newArrayList("arg1", "arg2=42")));
        specification2.setDocumentation("lib documentation");

        final LibrarySpecificationInput input1 = new LibrarySpecificationInput(robotProject, specification1);
        final LibrarySpecificationInput input2 = new LibrarySpecificationInput(mock(RobotProject.class),
                specification2);

        assertThat(input1.equals(input2)).isFalse();
    }

    @Test
    public void twoInputsAreNotEqual_whenSpecificationsAreDifferent() {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

        final LibrarySpecification specification1 = LibrarySpecification.create("library",
                KeywordSpecification.create("kw1"), KeywordSpecification.create("kw2"));
        specification1.setFormat("ROBOT");
        specification1.setScope("scope");
        specification1.setVersion("1.0");
        specification1.setConstructor(LibraryConstructor.create("constructor", newArrayList("arg1", "arg2=42")));
        specification1.setDocumentation("lib documentation");

        final LibrarySpecification specification2 = LibrarySpecification.create("library",
                KeywordSpecification.create("kw1"), KeywordSpecification.create("kw2"));
        specification2.setFormat("ROBOT");
        specification2.setScope("scope");
        specification2.setVersion("1.0");
        specification2.setConstructor(LibraryConstructor.create("constructor", newArrayList("arg1", "arg2=42")));
        specification2.setDocumentation("lib documentation2");

        final LibrarySpecification specification3 = LibrarySpecification.create("library",
                KeywordSpecification.create("kw1"), KeywordSpecification.create("kw2"));
        specification3.setFormat("ROBOT");
        specification3.setScope("scope");
        specification3.setVersion("2.0");
        specification3.setConstructor(LibraryConstructor.create("constructor", newArrayList("arg1", "arg2=42")));
        specification3.setDocumentation("lib documentation");

        final LibrarySpecification specification4 = LibrarySpecification.create("library",
                KeywordSpecification.create("kw1"));
        specification4.setFormat("ROBOT");
        specification4.setScope("scope");
        specification4.setVersion("1.0");
        specification4.setConstructor(LibraryConstructor.create("constructor", newArrayList("arg1", "arg2=42")));
        specification4.setDocumentation("lib documentation");

        final LibrarySpecification specification5 = LibrarySpecification.create("library2",
                KeywordSpecification.create("kw1"), KeywordSpecification.create("kw2"));
        specification5.setFormat("ROBOT");
        specification5.setScope("scope");
        specification5.setVersion("1.0");
        specification5.setConstructor(LibraryConstructor.create("constructor", newArrayList("arg1", "arg2=42")));
        specification5.setDocumentation("lib documentation");

        final LibrarySpecificationInput input1 = new LibrarySpecificationInput(robotProject, specification1);
        final LibrarySpecificationInput input2 = new LibrarySpecificationInput(robotProject, specification2);
        final LibrarySpecificationInput input3 = new LibrarySpecificationInput(robotProject, specification3);
        final LibrarySpecificationInput input4 = new LibrarySpecificationInput(robotProject, specification4);
        final LibrarySpecificationInput input5 = new LibrarySpecificationInput(robotProject, specification5);

        assertThat(input1.equals(input2)).isFalse();
        assertThat(input1.equals(input3)).isFalse();
        assertThat(input1.equals(input4)).isFalse();
        assertThat(input1.equals(input5)).isFalse();
    }

}
