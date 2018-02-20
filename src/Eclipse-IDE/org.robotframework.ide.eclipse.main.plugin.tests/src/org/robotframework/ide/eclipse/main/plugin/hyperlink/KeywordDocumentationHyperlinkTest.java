/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.hyperlink.Conditions.shellWithText;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.LibraryDescriptor;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.KeywordDocumentationPopup;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.Libraries;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.junit.ProjectProvider;

public class KeywordDocumentationHyperlinkTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(KeywordDocumentationHyperlinkTest.class);

    private final RobotModel model = new RobotModel();

    private LibrarySpecification libSpec;

    private KeywordSpecification kwSpec;

    @Before
    public void before() throws Exception {
        final ReferencedLibrary lib = ReferencedLibrary.create(LibraryType.PYTHON, "testlib",
                projectProvider.getProject().getName());

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(lib);

        projectProvider.createFile("testlib.py");
        projectProvider.configure(config);

        final Map<LibraryDescriptor, LibrarySpecification> refLibs = Libraries.createRefLib("testlib", "keyword");
        libSpec = refLibs.values().iterator().next();
        kwSpec = libSpec.getKeywords().get(0);

        final RobotProject project = model.createRobotProject(projectProvider.getProject());
        project.setStandardLibraries(new HashMap<LibraryDescriptor, LibrarySpecification>());
        project.setReferencedLibraries(refLibs);
    }

    @Test
    public void testFileHyperlinkProperties() {
        final KeywordDocumentationHyperlink link = new KeywordDocumentationHyperlink(model, new Region(20, 50),
                projectProvider.getProject(), libSpec, kwSpec);
        assertThat(link.getTypeLabel()).isNull();
        assertThat(link.getHyperlinkRegion()).isEqualTo(new Region(20, 50));
        assertThat(link.getHyperlinkText()).isEqualTo("Open Documentation");
        assertThat(link.getLabelForCompoundHyperlinksDialog()).isEqualTo("testlib");
        assertThat(link.additionalLabelDecoration())
                .isEqualTo("[" + projectProvider.getFile("testlib.py").getLocation().toString() + "]");
        assertThat(link.getImage()).isEqualTo(RedImages.getLibraryImage());
    }

    @Test
    public void testIfPopupOpensCorrectly() {
        final Display display = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay();
        assertThat(display.getShells()).doesNotHave(shellWithText(KeywordDocumentationPopup.POPUP_TEXT));

        final KeywordDocumentationHyperlink link = new KeywordDocumentationHyperlink(model, new Region(20, 50),
                projectProvider.getProject(), libSpec, kwSpec);
        link.open();
        assertThat(display.getShells()).has(shellWithText(KeywordDocumentationPopup.POPUP_TEXT));

        for (final Shell shell : display.getShells()) {
            if (shell.getText().equals(KeywordDocumentationPopup.POPUP_TEXT)) {
                shell.close();
            }
        }
    }
}
