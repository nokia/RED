/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

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
class Editors {

    static TextEditor openInTextEditor(final IFile redXmlFile) throws PartInitException {
        return (TextEditor) openInEditor(redXmlFile, EditorsUI.DEFAULT_TEXT_EDITOR_ID);
    }

    static RedProjectEditor openInProjectEditor(final IFile redXmlFile) throws PartInitException {
        return (RedProjectEditor) openInEditor(redXmlFile, RedProjectEditor.ID);
    }

    private static IEditorPart openInEditor(final IFile redXmlFile, final String editorId) throws PartInitException {
        return PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow()
                .getActivePage()
                .openEditor(new FileEditorInput(redXmlFile), editorId, true, IWorkbenchPage.MATCH_ID);
    }

    static boolean isAnyEditorOpened() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences().length > 0;
    }
}
