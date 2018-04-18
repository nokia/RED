package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.libraries.Documentation.DocFormat;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries;
import org.robotframework.red.junit.ProjectProvider;


public class LibraryImportSettingInputTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(LibraryImportSettingInputTest.class);

    private static RobotModel model = new RobotModel();

    @Before
    public void beforeTest() {
        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(new HashMap<>());
        robotProject.setReferencedLibraries(Libraries.createRefLib("lib", "Lib Kw 1", "Lib Kw 2"));
    }

    @After
    public void afterTest() {
        model.createSuiteFile(projectProvider.getFile("suite.robot")).dispose();
    }

    @AfterClass
    public static void afterSuite() {
        model = null;
    }

    @Test
    public void exceptionIsThrown_whenPreparingInputButLibraryDoesNotExist() throws Exception {
        final RobotSetting libImport = createLibraryImportSetting("unknown_lib");
        final LibraryImportSettingInput input = new LibraryImportSettingInput(libImport);

        assertThatExceptionOfType(DocumentationInputGenerationException.class).isThrownBy(() -> input.prepare())
                .withMessage("Library specification not found, nothing to display");
    }

    @Test
    public void properLibraryDocUriIsProvidedForInput() throws Exception {
        final RobotSetting libImport = createLibraryImportSetting("lib");
        final LibraryImportSettingInput input = new LibraryImportSettingInput(libImport);
        input.prepare();

        assertThat(input.getInputUri().toString())
                .isEqualTo("library:/LibraryImportSettingInputTest/lib?show_doc=true");
    }

    @Test
    public void theInputContainsGivenProjectAndImportSetting() throws Exception {
        final RobotSetting libImport = createLibraryImportSetting("lib");
        final LibraryImportSettingInput input = new LibraryImportSettingInput(libImport);

        assertThat(input.contains("lib")).isFalse();
        assertThat(input.contains(libImport.getSuiteFile())).isFalse();
        assertThat(input.contains(libImport)).isTrue();
        assertThat(input.contains(libImport.getSuiteFile().getFile().getProject())).isTrue();
    }

    @Test
    public void properHtmlIsReturned_forLibraryImport() throws Exception {
        final RobotRuntimeEnvironment env = mock(RobotRuntimeEnvironment.class);
        when(env.createHtmlDoc(any(String.class), eq(DocFormat.ROBOT))).thenReturn("doc");

        final RobotSetting libImport = createLibraryImportSetting("lib");
        final LibraryImportSettingInput input = new LibraryImportSettingInput(libImport);
        input.prepare();

        final String html = input.provideHtml(env);
        assertThat(html)
                .contains("<a href=\"library:/LibraryImportSettingInputTest/lib?show_source=true\">lib</a>");
        assertThat(html).contains("global");
        assertThat(html).contains("1.0");
        assertThat(html).contains("[]");
        assertThat(html).containsPattern("<h\\d.*>Introduction</h\\d>");
        assertThat(html).containsPattern("<h\\d.*>Shortcuts</h\\d>");
        assertThat(html).containsPattern("Lib Kw 1.*Lib Kw 2");
    }

    @Test
    public void properRawDocumentationIsReturnedForLibraryImport() throws Exception {
        final RobotSetting libImport = createLibraryImportSetting("lib");
        final LibraryImportSettingInput input = new LibraryImportSettingInput(libImport);
        input.prepare();

        final String raw = input.provideRawText();

        assertThat(raw).contains("Version: 1.0");
        assertThat(raw).contains("Scope: global");
        assertThat(raw).contains("Arguments: []");
        assertThat(raw).contains("library documentation");
    }

    private static RobotSetting createLibraryImportSetting(final String libraryNameOrPath)
            throws Exception {
        final IFile file = projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  " + libraryNameOrPath);
        return (RobotSetting) model.createSuiteFile(file)
                .findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .get(0);
    }
}
