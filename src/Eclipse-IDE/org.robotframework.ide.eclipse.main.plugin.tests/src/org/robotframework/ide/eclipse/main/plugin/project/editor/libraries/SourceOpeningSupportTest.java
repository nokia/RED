/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;
import java.util.function.BiConsumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rf.ide.core.dryrun.RobotDryRunKeywordSource;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ResourceCreator;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class SourceOpeningSupportTest {

    public static ProjectProvider projectProvider = new ProjectProvider(SourceOpeningSupportTest.class);

    public static TemporaryFolder tempFolder = new TemporaryFolder();

    @ClassRule
    public static TestRule rulesChain = RuleChain.outerRule(projectProvider).around(tempFolder);

    @Rule
    public ResourceCreator resourceCreator = new ResourceCreator();

    @Mock
    private BiConsumer<String, Exception> errorHandler;

    private LibrarySpecification libSpec;

    private RobotProject robotProject;

    private IWorkbenchPage page;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("testlib.py",
                "#comment",
                "def defined_kw():",
                "  print(\"kw\")",
                "def discovered_kw():",
                "  print(\"kw\")");
    }

    @Before
    public void beforeTest() throws Exception {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, "testlib",
                projectProvider.getProject().getName());

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(lib);
        projectProvider.configure(config);

        final LibraryDescriptor libDesc = LibraryDescriptor.ofReferencedLibrary(lib);
        libSpec = LibrarySpecification.create("testlib");
        libSpec.setDescriptor(libDesc);

        robotProject = new RobotModel().createRobotProject(projectProvider.getProject());
        robotProject.setReferencedLibraries(ImmutableMap.of(libDesc, libSpec));

        page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    }

    @After
    public void afterTest() {
        robotProject.clearConfiguration();

        page.closeAllEditors(false);
    }

    @Test
    public void testIfWorkspaceLibraryIsOpened() throws Exception {
        SourceOpeningSupport.open(page, robotProject, libSpec, errorHandler);

        assertOpenedEditor("testlib.py");
        assertEmptySelection();
        verifyZeroInteractions(errorHandler);
    }

    @Test
    public void testIfLinkedLibraryIsOpened() throws Exception {
        final File nonWorkspaceLib = tempFolder.newFile("linkedLib.py");
        final LibrarySpecification linkedLibSpec = LibrarySpecification.create("linkedLib");
        final LibraryDescriptor libDesc = LibraryDescriptor.ofReferencedLibrary(ReferencedLibrary
                .create(LibraryType.PYTHON, "linkedLib", nonWorkspaceLib.getParentFile().getAbsolutePath()));
        linkedLibSpec.setDescriptor(libDesc);

        final IFile linkedFile = projectProvider.getFile("linkedLib.py");
        resourceCreator.createLink(nonWorkspaceLib.toURI(), linkedFile);

        robotProject.setReferencedLibraries(ImmutableMap.of(libDesc, linkedLibSpec));

        SourceOpeningSupport.open(page, robotProject, linkedLibSpec, errorHandler);

        assertOpenedEditor("linkedLib.py");
        assertEmptySelection();
        verifyZeroInteractions(errorHandler);
    }

    @Test
    public void testIfNonWorkspaceLibraryIsOpened() throws Exception {
        final File nonWorkspaceLib = tempFolder.newFile("outsideLib.py");
        final LibrarySpecification nonWorkspaceLibSpec = LibrarySpecification.create("outsideLib");
        final LibraryDescriptor libDesc = LibraryDescriptor.ofReferencedLibrary(ReferencedLibrary
                .create(LibraryType.PYTHON, "outsideLib", nonWorkspaceLib.getParentFile().getAbsolutePath()));
        nonWorkspaceLibSpec.setDescriptor(libDesc);

        robotProject.setReferencedLibraries(ImmutableMap.of(libDesc, nonWorkspaceLibSpec));

        SourceOpeningSupport.open(page, robotProject, nonWorkspaceLibSpec, errorHandler);

        assertOpenedEditor("libspecs/outsideLib.py");
        assertEmptySelection();
        verifyZeroInteractions(errorHandler);
    }

    @Test
    public void testIfUnknownLibraryIsNotOpened() throws Exception {
        final LibrarySpecification unknownLibSpec = LibrarySpecification.create("unknown");
        final LibraryDescriptor libDesc = LibraryDescriptor
                .ofReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "unknown", "path"));
        unknownLibSpec.setDescriptor(libDesc);

        SourceOpeningSupport.open(page, robotProject, unknownLibSpec, errorHandler);

        assertThat(page.getEditorReferences()).isEmpty();
        verify(errorHandler).accept("Unknown source location", null);
    }

    @Test
    public void testIfJavaLibraryIsNotOpened() throws Exception {
        final LibrarySpecification unsupportedLibSpec = LibrarySpecification.create("unsupported");
        final LibraryDescriptor libDesc = LibraryDescriptor
                .ofReferencedLibrary(ReferencedLibrary.create(LibraryType.JAVA, "unsupported", "path.jar"));
        unsupportedLibSpec.setDescriptor(libDesc);

        SourceOpeningSupport.open(page, robotProject, unsupportedLibSpec, errorHandler);

        assertThat(page.getEditorReferences()).isEmpty();
        verify(errorHandler).accept("Unsupported library type", null);
    }

    @Test
    public void testIfVirtualLibraryIsNotOpened() throws Exception {
        final LibrarySpecification unsupportedLibSpec = LibrarySpecification.create("unsupported");
        final LibraryDescriptor libDesc = LibraryDescriptor
                .ofReferencedLibrary(ReferencedLibrary.create(LibraryType.VIRTUAL, "unsupported", "path.xml"));
        unsupportedLibSpec.setDescriptor(libDesc);

        SourceOpeningSupport.open(page, robotProject, unsupportedLibSpec, errorHandler);

        assertThat(page.getEditorReferences()).isEmpty();
        verify(errorHandler).accept("Unsupported library type", null);
    }

    @Test
    public void testIfJavaLibraryKeywordIsNotOpened() throws Exception {
        final KeywordSpecification kwSpec = new KeywordSpecification();

        final LibrarySpecification unsupportedLibSpec = LibrarySpecification.create("unsupported");
        final LibraryDescriptor libDesc = LibraryDescriptor
                .ofReferencedLibrary(ReferencedLibrary.create(LibraryType.JAVA, "unsupported", "path.jar"));
        unsupportedLibSpec.setDescriptor(libDesc);

        SourceOpeningSupport.open(page, robotProject, unsupportedLibSpec, kwSpec, errorHandler);

        assertThat(page.getEditorReferences()).isEmpty();
        verify(errorHandler).accept("Unsupported library type", null);
    }

    @Test
    public void testIfVirtualLibraryKeywordIsNotOpened() throws Exception {
        final KeywordSpecification kwSpec = new KeywordSpecification();

        final LibrarySpecification unsupportedLibSpec = LibrarySpecification.create("unsupported");
        final LibraryDescriptor libDesc = LibraryDescriptor
                .ofReferencedLibrary(ReferencedLibrary.create(LibraryType.VIRTUAL, "unsupported", "path.xml"));
        unsupportedLibSpec.setDescriptor(libDesc);

        SourceOpeningSupport.open(page, robotProject, unsupportedLibSpec, kwSpec, errorHandler);

        assertThat(page.getEditorReferences()).isEmpty();
        verify(errorHandler).accept("Unsupported library type", null);
    }

    @Test
    public void testIfLibraryIsOpened_whenKeywordNotFound() throws Exception {
        final KeywordSpecification kwSpec = new KeywordSpecification();
        kwSpec.setName("Not Existing Kw");

        SourceOpeningSupport.open(page, robotProject, libSpec, kwSpec, errorHandler);

        assertOpenedEditor("testlib.py");
        assertEmptySelection();
        verifyZeroInteractions(errorHandler);
    }

    @Test
    public void testIfLibraryIsOpenedAndTextIsSelected_whenKeywordFound() throws Exception {
        final KeywordSpecification kwSpec = new KeywordSpecification();
        kwSpec.setName("Defined Kw");

        final RobotDryRunKeywordSource kwSource = new RobotDryRunKeywordSource();
        kwSource.setFilePath(projectProvider.getFile("testlib.py").getLocation().toOSString());
        kwSource.setLibraryName(libSpec.getName());
        kwSource.setName(kwSpec.getName());
        kwSource.setLine(1);
        kwSource.setOffset(4);
        kwSource.setLength(10);

        robotProject.addKeywordSource(kwSource);

        SourceOpeningSupport.open(page, robotProject, libSpec, kwSpec, errorHandler);

        assertOpenedEditor("testlib.py");
        assertSelection(1, 13, "defined_kw");
        verifyZeroInteractions(errorHandler);
    }

    @Test
    public void testIfLibraryIsOpenedAndTextIsSelected_whenKeywordFoundByAutoDiscoverer() throws Exception {
        final KeywordSpecification kwSpec = new KeywordSpecification();
        kwSpec.setName("Discovered Kw");

        SourceOpeningSupport.open(page, robotProject, libSpec, kwSpec, errorHandler);

        assertOpenedEditor("testlib.py");
        assertSelection(3, 45, "discovered_kw");
        verifyZeroInteractions(errorHandler);
    }

    @Test
    public void testIfFileIsOpenedInEditor() throws Exception {
        SourceOpeningSupport.tryToOpenInEditor(page, projectProvider.getFile("testlib.py"));

        assertOpenedEditor("testlib.py");
        assertEmptySelection();
    }

    private void assertOpenedEditor(final String expectedFilePath) throws PartInitException {
        assertThat(page.getEditorReferences()).hasSize(1);
        final IFileEditorInput editorInput = (IFileEditorInput) page.getEditorReferences()[0].getEditorInput();
        assertThat(editorInput.getFile()).isEqualTo(projectProvider.getFile(expectedFilePath));
    }

    private void assertEmptySelection() throws PartInitException {
        final TextEditor editor = page.getEditorReferences()[0].getEditor(true).getAdapter(TextEditor.class);
        final TextSelection selection = (TextSelection) editor.getSelectionProvider().getSelection();
        assertThat(selection.getText()).isEqualTo("");
        assertThat(selection.getStartLine()).isEqualTo(0);
        assertThat(selection.getOffset()).isEqualTo(0);
        assertThat(selection.getLength()).isEqualTo(0);
    }

    private void assertSelection(final int expectedLine, final int expectedOffset, final String expectedText)
            throws PartInitException {
        final TextEditor editor = page.getEditorReferences()[0].getEditor(true).getAdapter(TextEditor.class);
        final TextSelection selection = (TextSelection) editor.getSelectionProvider().getSelection();
        assertThat(selection.getText()).isEqualTo(expectedText);
        assertThat(selection.getStartLine()).isEqualTo(expectedLine);
        assertThat(selection.getOffset()).isEqualTo(expectedOffset);
        assertThat(selection.getLength()).isEqualTo(expectedText.length());
    }

}
