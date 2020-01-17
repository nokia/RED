/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.configure;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class KeywordInLibrarySourceHyperlinkTest {

    @Project(files = { "testlib.py" })
    static IProject project;

    private static LibrarySpecification libSpec;

    private static KeywordSpecification kwSpec;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, "testlib",
                project.getName() + "/testlib.py");

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(lib);

        configure(project, config);

        final Map<LibraryDescriptor, LibrarySpecification> refLibs = Libraries.createRefLib(lib, "keyword");
        libSpec = refLibs.values().iterator().next();
        kwSpec = libSpec.getKeywords().get(0);

        final RobotProject robotProject = new RobotModel().createRobotProject(project);
        robotProject.setStandardLibraries(new HashMap<>());
        robotProject.setReferencedLibraries(refLibs);
    }

    @AfterAll
    public static void afterSuite() {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
    }

    @Test
    public void testFileHyperlinkProperties() {
        final KeywordInLibrarySourceHyperlink link = new KeywordInLibrarySourceHyperlink(new RobotModel(),
                new Region(20, 50), project, libSpec, kwSpec);
        assertThat(link.getTypeLabel()).isNull();
        assertThat(link.getHyperlinkRegion()).isEqualTo(new Region(20, 50));
        assertThat(link.getHyperlinkText()).isEqualTo("Open Definition keyword");
        assertThat(link.getLabelForCompoundHyperlinksDialog()).isEqualTo("testlib");
        assertThat(link.additionalLabelDecoration())
                .isEqualTo("[" + getFile(project, "testlib.py").getLocation().toString() + "]");
        assertThat(link.getImage()).isEqualTo(RedImages.getLibraryImage());
    }

    @Test
    public void testIfSourceFileOpensCorrectly() throws Exception {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

        assertThat(page.getEditorReferences()).isEmpty();

        final KeywordInLibrarySourceHyperlink link = new KeywordInLibrarySourceHyperlink(new RobotModel(),
                new Region(20, 50), project, libSpec, kwSpec);
        link.open();

        assertThat(page.getEditorReferences()).hasSize(1);
        final IFileEditorInput editorInput = (IFileEditorInput) page.getEditorReferences()[0].getEditorInput();
        assertThat(editorInput.getFile()).isEqualTo(getFile(project, "testlib.py"));
    }
}
