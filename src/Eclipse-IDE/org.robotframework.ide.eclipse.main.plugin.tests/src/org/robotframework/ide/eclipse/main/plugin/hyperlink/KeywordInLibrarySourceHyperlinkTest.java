/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.text.Region;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.LibraryType;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.ImmutableMap;

public class KeywordInLibrarySourceHyperlinkTest {

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(KeywordInLibrarySourceHyperlinkTest.class);

    private final RobotModel model = new RobotModel();

    private LibrarySpecification libSpec;

    private KeywordSpecification kwSpec;

    @Before
    public void before() throws Exception {
        final ReferencedLibrary lib = new ReferencedLibrary();
        lib.setType(LibraryType.PYTHON.toString());
        lib.setName("testlib");
        lib.setPath(projectProvider.getProject().getName());

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(lib);

        projectProvider.createFile("testlib.py");
        projectProvider.configure(config);

        kwSpec = new KeywordSpecification();
        kwSpec.setFormat("ROBOT");
        kwSpec.setName("keyword");
        kwSpec.setArguments(new ArrayList<String>());
        kwSpec.setDocumentation("");

        libSpec = new LibrarySpecification();
        libSpec.setName("testlib");
        libSpec.getKeywords().add(kwSpec);

        final RobotProject project = model.createRobotProject(projectProvider.getProject());
        project.setStandardLibraries(new HashMap<String, LibrarySpecification>());
        project.setReferencedLibraries(ImmutableMap.of(lib, libSpec));
    }

    @AfterClass
    public static void afterSuite() {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
    }

    @Test
    public void testFileHyperlinkProperties() {
        final KeywordInLibrarySourceHyperlink link = new KeywordInLibrarySourceHyperlink(model, new Region(20, 50),
                projectProvider.getProject(), libSpec);
        assertThat(link.getTypeLabel()).isNull();
        assertThat(link.getHyperlinkRegion()).isEqualTo(new Region(20, 50));
        assertThat(link.getHyperlinkText()).isEqualTo("Open Definition");
        assertThat(link.getLabelForCompoundHyperlinksDialog()).isEqualTo("testlib");
        assertThat(link.additionalLabelDecoration())
                .isEqualTo("[" + projectProvider.getFile("testlib.py").getLocation().toString() + "]");
        assertThat(link.getImage()).isEqualTo(RedImages.getLibraryImage());
    }

    @Test
    public void testIfSourceFileOpensCorrectly() throws Exception {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

        assertThat(page.getEditorReferences()).isEmpty();

        final KeywordInLibrarySourceHyperlink link = new KeywordInLibrarySourceHyperlink(model, new Region(20, 50),
                projectProvider.getProject(), libSpec);
        link.open();

        assertThat(page.getEditorReferences()).hasSize(1);
        final IFileEditorInput editorInput = (IFileEditorInput) page.getEditorReferences()[0].getEditorInput();
        assertThat(editorInput.getFile())
                .isEqualTo(LibspecsFolder.get(projectProvider.getProject()).getFile("testlib.py"));
    }
}
