package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.net.URI;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationsLinksSupport.DocumentationDisplayer;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationsLinksSupport.UnableToOpenUriException;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.KeywordDefinitionInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.KeywordSpecificationInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.LibrarySpecificationInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.SuiteFileInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.TestCaseInput;
import org.robotframework.red.junit.ProjectProvider;


public class DocumentationsLinksSupportTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(DocumentationsLinksSupportTest.class);

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("suite_with_doc.robot",
                "*** Settings ***",
                "Documentation  doc");
        projectProvider.createFile("test_with_doc.robot",
                "*** Test Cases ***",
                "case",
                "  [Documentation]  doc");
        projectProvider.createFile("keyword_with_doc.robot",
                "*** Keywords ***",
                "kw",
                "  [Documentation]  doc");
    }
    
    @Test
    public void linksSupportDoesNotHandleAboutBlankUris_1() {
        final IWorkbenchBrowserSupport browserSupport = mock(IWorkbenchBrowserSupport.class);
        final DocumentationDisplayer displayer = mock(DocumentationDisplayer.class);
        final Runnable outsideDisplayerCallback = mock(Runnable.class);

        final DocumentationsLinksSupport support = createSupport(browserSupport, displayer, outsideDisplayerCallback);
        final boolean isHandled = support.changeLocationTo(URI.create("about:blank"));

        assertThat(isHandled).isFalse();
        verifyZeroInteractions(browserSupport, displayer, outsideDisplayerCallback);
    }

    @Test
    public void linksSupportDoesNotHandleAboutBlankUris_2() {
        final IWorkbenchBrowserSupport browserSupport = mock(IWorkbenchBrowserSupport.class);
        final DocumentationDisplayer displayer = mock(DocumentationDisplayer.class);
        final Runnable outsideDisplayerCallback = mock(Runnable.class);

        final DocumentationsLinksSupport support = createSupport(browserSupport, displayer, outsideDisplayerCallback);
        final boolean isHandled = support.changeLocationTo(URI.create("about:blank#some_id"));

        assertThat(isHandled).isFalse();
        verifyZeroInteractions(browserSupport, displayer, outsideDisplayerCallback);
    }

    @Test
    public void displayerIsAskedToDisplayDocumentationOfLibrary_whenMovingToLibraryDocUri() {
        final RobotProject robotProject = RedPlugin.getModelManager().getModel().createRobotProject(
                projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("library", "kw"));
        robotProject.setReferencedLibraries(new HashMap<>());

        final IWorkbenchBrowserSupport browserSupport = mock(IWorkbenchBrowserSupport.class);
        final DocumentationDisplayer displayer = mock(DocumentationDisplayer.class);
        final Runnable outsideDisplayerCallback = mock(Runnable.class);

        final DocumentationsLinksSupport support = createSupport(browserSupport, displayer, outsideDisplayerCallback);
        final boolean isHandled = support
                .changeLocationTo(URI.create("library:/DocumentationsLinksSupportTest/library?show_doc=true"));

        assertThat(isHandled).isTrue();
        verifyZeroInteractions(browserSupport, outsideDisplayerCallback);
        verify(displayer).displayDocumentation(any(LibrarySpecificationInput.class));
    }

    @Test
    public void displayerIsAskedToDisplayDocumentationOfLibrary_whenMovingToNonExistingKeywordButInKnownLibraryDocUri() {
        final RobotProject robotProject = RedPlugin.getModelManager().getModel().createRobotProject(
                projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("library", "kw"));
        robotProject.setReferencedLibraries(new HashMap<>());

        final IWorkbenchBrowserSupport browserSupport = mock(IWorkbenchBrowserSupport.class);
        final DocumentationDisplayer displayer = mock(DocumentationDisplayer.class);
        final Runnable outsideDisplayerCallback = mock(Runnable.class);

        final DocumentationsLinksSupport support = createSupport(browserSupport, displayer, outsideDisplayerCallback);
        final boolean isHandled = support
                .changeLocationTo(
                        URI.create("library:/DocumentationsLinksSupportTest/library/non_existing_kw?show_doc=true"));

        assertThat(isHandled).isTrue();
        verifyZeroInteractions(browserSupport, outsideDisplayerCallback);
        verify(displayer).displayDocumentation(any(LibrarySpecificationInput.class));
    }

    @Test
    public void displayerIsAskedToDisplayDocumentationOfLibraryKeyword_whenMovingToKnownKeyword() {
        final RobotProject robotProject = RedPlugin.getModelManager().getModel().createRobotProject(
                projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("library", "kw"));
        robotProject.setReferencedLibraries(new HashMap<>());

        final IWorkbenchBrowserSupport browserSupport = mock(IWorkbenchBrowserSupport.class);
        final DocumentationDisplayer displayer = mock(DocumentationDisplayer.class);
        final Runnable outsideDisplayerCallback = mock(Runnable.class);

        final DocumentationsLinksSupport support = createSupport(browserSupport, displayer, outsideDisplayerCallback);
        final boolean isHandled = support
                .changeLocationTo(URI.create("library:/DocumentationsLinksSupportTest/library/kw?show_doc=true"));

        assertThat(isHandled).isTrue();
        verifyZeroInteractions(browserSupport, outsideDisplayerCallback);
        verify(displayer).displayDocumentation(any(KeywordSpecificationInput.class));
    }

    @Test
    public void exceptionIsThrown_whenTryingToOpenDocumentationOfMissingLibrary() {
        final RobotProject robotProject = RedPlugin.getModelManager().getModel().createRobotProject(
                projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLib("library", "kw"));
        robotProject.setReferencedLibraries(new HashMap<>());

        final IWorkbenchBrowserSupport browserSupport = mock(IWorkbenchBrowserSupport.class);
        final DocumentationDisplayer displayer = mock(DocumentationDisplayer.class);
        final Runnable outsideDisplayerCallback = mock(Runnable.class);

        final DocumentationsLinksSupport support = createSupport(browserSupport, displayer, outsideDisplayerCallback);
        assertThatExceptionOfType(UnableToOpenUriException.class).isThrownBy(() -> support
                .changeLocationTo(URI.create("library:/DocumentationsLinksSupportTest/unknown?show_doc=true")))
                .withMessage("Unable to open library documentation. Cannot find given library/keyword");
    }

    @Test
    public void displayerIsAskedToDisplayDocumentationOfSuite_whenMovingToExistingFileDoc() {
        final IFile file = projectProvider.getFile("suite_with_doc.robot");

        final IWorkbenchBrowserSupport browserSupport = mock(IWorkbenchBrowserSupport.class);
        final DocumentationDisplayer displayer = mock(DocumentationDisplayer.class);
        final Runnable outsideDisplayerCallback = mock(Runnable.class);

        final DocumentationsLinksSupport support = createSupport(browserSupport, displayer, outsideDisplayerCallback);
        final boolean isHandled = support.changeLocationTo(URI.create(file.getLocationURI() + "?show_doc=true&suite="));

        assertThat(isHandled).isTrue();
        verifyZeroInteractions(browserSupport, outsideDisplayerCallback);
        verify(displayer).displayDocumentation(any(SuiteFileInput.class));
    }

    @Test
    public void displayerIsAskedToDisplayDocumentationOfTestCase_whenMovingToExistingFileTestDoc() {
        final IFile file = projectProvider.getFile("test_with_doc.robot");

        final IWorkbenchBrowserSupport browserSupport = mock(IWorkbenchBrowserSupport.class);
        final DocumentationDisplayer displayer = mock(DocumentationDisplayer.class);
        final Runnable outsideDisplayerCallback = mock(Runnable.class);

        final DocumentationsLinksSupport support = createSupport(browserSupport, displayer, outsideDisplayerCallback);
        final boolean isHandled = support
                .changeLocationTo(URI.create(file.getLocationURI() + "?show_doc=true&test=case"));

        assertThat(isHandled).isTrue();
        verifyZeroInteractions(browserSupport, outsideDisplayerCallback);
        verify(displayer).displayDocumentation(any(TestCaseInput.class));
    }

    @Test
    public void displayerIsAskedToDisplayDocumentationOfKeyword_whenMovingToExistingFileKeywordDoc() {
        final IFile file = projectProvider.getFile("keyword_with_doc.robot");

        final IWorkbenchBrowserSupport browserSupport = mock(IWorkbenchBrowserSupport.class);
        final DocumentationDisplayer displayer = mock(DocumentationDisplayer.class);
        final Runnable outsideDisplayerCallback = mock(Runnable.class);

        final DocumentationsLinksSupport support = createSupport(browserSupport, displayer, outsideDisplayerCallback);
        final boolean isHandled = support
                .changeLocationTo(URI.create(file.getLocationURI() + "?show_doc=true&keyword=kw"));

        assertThat(isHandled).isTrue();
        verifyZeroInteractions(browserSupport, outsideDisplayerCallback);
        verify(displayer).displayDocumentation(any(KeywordDefinitionInput.class));
    }

    @Test
    public void exceptionIsThrown_whenTryingToOpenDocumentationOfSuite_butTheFileIsMissing() {
        final IWorkbenchBrowserSupport browserSupport = mock(IWorkbenchBrowserSupport.class);
        final DocumentationDisplayer displayer = mock(DocumentationDisplayer.class);
        final Runnable outsideDisplayerCallback = mock(Runnable.class);

        final DocumentationsLinksSupport support = createSupport(browserSupport, displayer, outsideDisplayerCallback);
        assertThatExceptionOfType(UnableToOpenUriException.class)
                .isThrownBy(() -> support
                        .changeLocationTo(URI.create("file:///location/to/missing/file.robot?show_doc=true&suite=")))
                .withMessage("Unable to find given element");
    }

    @Test
    public void exceptionIsThrown_whenTryingToOpenDocumentationOfTestCase_butTheFileIsMissing() {
        final IWorkbenchBrowserSupport browserSupport = mock(IWorkbenchBrowserSupport.class);
        final DocumentationDisplayer displayer = mock(DocumentationDisplayer.class);
        final Runnable outsideDisplayerCallback = mock(Runnable.class);

        final DocumentationsLinksSupport support = createSupport(browserSupport, displayer, outsideDisplayerCallback);
        assertThatExceptionOfType(UnableToOpenUriException.class)
                .isThrownBy(() -> support
                        .changeLocationTo(URI.create("file:///location/to/missing/file.robot?show_doc=true&test=case")))
                .withMessage("Unable to find given element");
    }

    @Test
    public void exceptionIsThrown_whenTryingToOpenDocumentationOfKeyword_butTheFileIsMissing() {
        final IWorkbenchBrowserSupport browserSupport = mock(IWorkbenchBrowserSupport.class);
        final DocumentationDisplayer displayer = mock(DocumentationDisplayer.class);
        final Runnable outsideDisplayerCallback = mock(Runnable.class);

        final DocumentationsLinksSupport support = createSupport(browserSupport, displayer, outsideDisplayerCallback);
        assertThatExceptionOfType(UnableToOpenUriException.class)
                .isThrownBy(() -> support.changeLocationTo(
                        URI.create("file:///location/to/missing/file.robot?show_doc=true&keyword=kw")))
                .withMessage("Unable to find given element");
    }

    @Test
    public void exceptionIsThrown_whenTryingToOpenDocumentation_butQueryParamIsMissing() {
        final IFile file = projectProvider.getFile("suite_with_doc.robot");

        final IWorkbenchBrowserSupport browserSupport = mock(IWorkbenchBrowserSupport.class);
        final DocumentationDisplayer displayer = mock(DocumentationDisplayer.class);
        final Runnable outsideDisplayerCallback = mock(Runnable.class);

        final DocumentationsLinksSupport support = createSupport(browserSupport, displayer, outsideDisplayerCallback);
        assertThatExceptionOfType(UnableToOpenUriException.class)
                .isThrownBy(() -> support.changeLocationTo(URI.create(file.getLocationURI() + "?show_doc=true")))
                .withMessage("Unable to find given element");
    }

    private static DocumentationsLinksSupport createSupport(final IWorkbenchBrowserSupport browserSupport,
            final DocumentationDisplayer displayer, final Runnable outsideDisplayerCallback) {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        return new DocumentationsLinksSupport(page, browserSupport, displayer, outsideDisplayerCallback);
    }
}
