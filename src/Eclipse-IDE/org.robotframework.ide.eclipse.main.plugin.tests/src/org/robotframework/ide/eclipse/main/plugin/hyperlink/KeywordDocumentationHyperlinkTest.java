/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;

import org.assertj.core.api.Condition;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.KeywordDocumentationPopup;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.LibraryType;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.ImmutableMap;

public class KeywordDocumentationHyperlinkTest {

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(KeywordDocumentationHyperlinkTest.class);

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

    private static Condition<? super Shell[]> shellWithText(final String text) {
        return new Condition<Shell[]>() {
            @Override
            public boolean matches(final Shell[] shells) {
                for (final Shell shell : shells) {
                    if (shell.getText().equals(text)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
