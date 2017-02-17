/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Function;

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
                "Link label", null);
        assertThat(link.getTypeLabel()).isNull();
        assertThat(link.getHyperlinkRegion()).isEqualTo(new Region(20, 50));
        assertThat(link.getHyperlinkText()).isEqualTo("Link label");
    }

    @Test
    public void testIfFileOpensProperly() throws Exception {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

        assertThat(page.getEditorReferences()).isEmpty();

        @SuppressWarnings("unchecked")
        final Function<IFile, Void> operation = mock(Function.class);
        final FileHyperlink link = new FileHyperlink(new Region(20, 50), projectProvider.getFile("file.txt"),
                "Link label", operation);
        link.open();

        assertThat(page.getEditorReferences()).hasSize(1);
        final IFileEditorInput editorInput = (IFileEditorInput) page.getEditorReferences()[0].getEditorInput();
        assertThat(editorInput.getFile()).isEqualTo(projectProvider.getFile("file.txt"));

        verify(operation).apply(eq(projectProvider.getFile("file.txt")));
    }

}
