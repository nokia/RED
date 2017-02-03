/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.junit;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditor;

/**
 * @author Michal Anglart
 *
 */
public class Editors {

    public static TextEditor openInTextEditor(final IFile file) throws PartInitException {
        return (TextEditor) openInEditor(file, EditorsUI.DEFAULT_TEXT_EDITOR_ID);
    }

    public static RedProjectEditor openInProjectEditor(final IFile file) throws PartInitException {
        return (RedProjectEditor) openInEditor(file, RedProjectEditor.ID);
    }

    private static IEditorPart openInEditor(final IFile file, final String editorId) throws PartInitException {
        return PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow()
                .getActivePage()
                .openEditor(new FileEditorInput(file), editorId, true, IWorkbenchPage.MATCH_ID);
    }

    public static boolean isAnyEditorOpened() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences().length > 0;
    }
}
