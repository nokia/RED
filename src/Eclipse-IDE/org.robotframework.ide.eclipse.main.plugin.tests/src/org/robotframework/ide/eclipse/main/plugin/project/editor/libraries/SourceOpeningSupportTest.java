/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
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

import com.google.common.collect.ImmutableMap;

public class SourceOpeningSupportTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(SourceOpeningSupportTest.class);

    private final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

    private final RobotModel model = new RobotModel();

    private LibrarySpecification libSpec;

    private RobotProject project;

    @Before
    public void before() throws Exception {
        projectProvider.createFile("testlib.py",
                "#comment",
                "def defined_kw():",
                "  print(\"kw\")",
                "def discovered_kw():",
                "  print(\"kw\")");

        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, "testlib",
                projectProvider.getProject().getName());

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(lib);
        projectProvider.configure(config);

        final LibraryDescriptor libDesc = LibraryDescriptor.ofReferencedLibrary(lib);
        libSpec = LibrarySpecification.create("testlib");
        libSpec.setDescriptor(libDesc);

        project = model.createRobotProject(projectProvider.getProject());
        project.setStandardLibraries(new HashMap<>());
        project.setReferencedLibraries(ImmutableMap.of(libDesc, libSpec));
    }

    @After
    public void after() {
        page.closeAllEditors(false);
    }

    @Test
    public void testIfLibraryIsOpened() throws Exception {
        assertThat(page.getEditorReferences()).isEmpty();

        SourceOpeningSupport.open(page, model, project.getProject(), libSpec);

        verifyEmptySelection("testlib.py");
    }

    @Test
    public void testIfLibraryIsOpened_whenKeywordNotFound() throws Exception {
        assertThat(page.getEditorReferences()).isEmpty();

        final KeywordSpecification kwSpec = new KeywordSpecification();
        kwSpec.setName("Not Existing Kw");

        SourceOpeningSupport.open(page, model, project.getProject(), libSpec, kwSpec);

        verifyEmptySelection("testlib.py");
    }

    @Test
    public void testIfLibraryIsOpenedAndTextIsSelected_whenKeywordFound() throws Exception {
        assertThat(page.getEditorReferences()).isEmpty();

        final KeywordSpecification kwSpec = new KeywordSpecification();
        kwSpec.setName("Defined Kw");

        final RobotDryRunKeywordSource kwSource = new RobotDryRunKeywordSource();
        kwSource.setFilePath(projectProvider.getFile("testlib.py").getLocation().toOSString());
        kwSource.setLibraryName(libSpec.getName());
        kwSource.setName(kwSpec.getName());
        kwSource.setLine(1);
        kwSource.setOffset(4);
        kwSource.setLength(10);

        project.addKeywordSource(kwSource);

        SourceOpeningSupport.open(page, model, project.getProject(), libSpec, kwSpec);

        verifySelection("testlib.py", 1, 13, "defined_kw");
    }

    @Test
    public void testIfLibraryIsOpenedAndTextIsSelected_whenKeywordFoundByAutoDiscoverer() throws Exception {
        assertThat(page.getEditorReferences()).isEmpty();

        final KeywordSpecification kwSpec = new KeywordSpecification();
        kwSpec.setName("Discovered Kw");

        SourceOpeningSupport.open(page, model, project.getProject(), libSpec, kwSpec);

        verifySelection("testlib.py", 3, 45, "discovered_kw");
    }

    @Test
    public void testIfFileIsOpenedInEditor() throws Exception {
        assertThat(page.getEditorReferences()).isEmpty();

        SourceOpeningSupport.tryToOpenInEditor(page, projectProvider.getFile("testlib.py"));

        verifyEmptySelection("testlib.py");
    }

    private void verifyEmptySelection(final String expectedFilePath) throws PartInitException {
        assertThat(page.getEditorReferences()).hasSize(1);
        final IFileEditorInput editorInput = (IFileEditorInput) page.getEditorReferences()[0].getEditorInput();
        assertThat(editorInput.getFile()).isEqualTo(projectProvider.getFile(expectedFilePath));

        final TextEditor editor = page.getEditorReferences()[0].getEditor(true).getAdapter(TextEditor.class);
        final TextSelection selection = (TextSelection) editor.getSelectionProvider().getSelection();
        assertThat(selection.getText()).isEqualTo("");
        assertThat(selection.getStartLine()).isEqualTo(0);
        assertThat(selection.getOffset()).isEqualTo(0);
        assertThat(selection.getLength()).isEqualTo(0);
    }

    private void verifySelection(final String expectedFilePath, final int expectedLine, final int expectedOffset,
            final String expectedText) throws PartInitException {
        assertThat(page.getEditorReferences()).hasSize(1);
        final IFileEditorInput editorInput = (IFileEditorInput) page.getEditorReferences()[0].getEditorInput();
        assertThat(editorInput.getFile()).isEqualTo(projectProvider.getFile(expectedFilePath));

        final TextEditor editor = page.getEditorReferences()[0].getEditor(true).getAdapter(TextEditor.class);
        final TextSelection selection = (TextSelection) editor.getSelectionProvider().getSelection();
        assertThat(selection.getText()).isEqualTo(expectedText);
        assertThat(selection.getStartLine()).isEqualTo(expectedLine);
        assertThat(selection.getOffset()).isEqualTo(expectedOffset);
        assertThat(selection.getLength()).isEqualTo(expectedText.length());
    }

}
