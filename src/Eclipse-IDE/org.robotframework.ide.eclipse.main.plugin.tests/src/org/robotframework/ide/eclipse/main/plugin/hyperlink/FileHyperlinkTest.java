/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jface.text.Region;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.red.junit.ProjectProvider;

public class FileHyperlinkTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(FileHyperlinkTest.class);

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("file.txt", "");
    }

    @AfterClass
    public static void afterSuite() {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
    }

    @Test
    public void testFileHyperlinkProperties() {
        final FileHyperlink link = new FileHyperlink(new Region(20, 50), projectProvider.getFile("file.txt"),
                "Link label");
        assertThat(link.getTypeLabel()).isNull();
        assertThat(link.getHyperlinkRegion()).isEqualTo(new Region(20, 50));
        assertThat(link.getHyperlinkText()).isEqualTo("Link label");
    }

    @Test
    public void testIfFileOpensProperly() throws Exception {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

        assertThat(page.getEditorReferences()).isEmpty();

        final FileHyperlink link = new FileHyperlink(new Region(20, 50), projectProvider.getFile("file.txt"),
                "Link label");
        link.open();

        assertThat(page.getEditorReferences()).hasSize(1);
        final IFileEditorInput editorInput = (IFileEditorInput) page.getEditorReferences()[0].getEditorInput();
        assertThat(editorInput.getFile()).isEqualTo(projectProvider.getFile("file.txt"));
    }

}
