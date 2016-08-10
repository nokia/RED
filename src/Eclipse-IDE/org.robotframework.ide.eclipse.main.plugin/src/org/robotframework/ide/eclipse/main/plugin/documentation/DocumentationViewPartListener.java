/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.documentation;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.CasesEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.KeywordsEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;
import org.robotframework.ide.eclipse.main.plugin.views.DocumentationView;

public class DocumentationViewPartListener implements IPartListener {

    private SourceDocumentationSelectionChangedListener sourceDocSelectionChangedListener;

    private TableDocumentationSelectionChangedListener tableDocSelectionChangedListener;

    private IEditorPart currentlyActiveEditor;

    public DocumentationViewPartListener(final DocumentationView view) {
        sourceDocSelectionChangedListener = new SourceDocumentationSelectionChangedListener(view);
        tableDocSelectionChangedListener = new TableDocumentationSelectionChangedListener(view);
    }
 
    @Override
    public void partActivated(final IWorkbenchPart part) {
        if (part.getSite().getId().equals(RobotFormEditor.ID)) {
            final RobotFormEditor editor = (RobotFormEditor) part;
            editor.setDocumentationViewPartListener(this);
            addDocSelectionChangedListenerToActiveEditor(editor.getActiveEditor());
        }
    }

    @Override
    public void partDeactivated(final IWorkbenchPart part) {
        if (part.getSite().getId().equals(RobotFormEditor.ID)) {
            final RobotFormEditor editor = (RobotFormEditor) part;
            editor.removeDocumentationViewPartListener();
            currentlyActiveEditor = null;
            removeDocSelectionChangedListenerFromActiveEditor(editor.getActiveEditor());
        }
    }
    
    @Override
    public void partOpened(final IWorkbenchPart part) {
    }

    @Override
    public void partClosed(final IWorkbenchPart part) {
    }

    @Override
    public void partBroughtToTop(final IWorkbenchPart part) {
    }


    public void pageChanged(final IEditorPart activeEditor) {
        if (currentlyActiveEditor != null) {
            removeDocSelectionChangedListenerFromActiveEditor(currentlyActiveEditor);
        }
        addDocSelectionChangedListenerToActiveEditor(activeEditor);
    }

    public void dispose() {
        if (currentlyActiveEditor != null) {
            removeDocSelectionChangedListenerFromActiveEditor(currentlyActiveEditor);
            currentlyActiveEditor = null;
        }
    }

    private void addDocSelectionChangedListenerToActiveEditor(final IEditorPart activeEditor) {
        currentlyActiveEditor = activeEditor;
        if (activeEditor instanceof SuiteSourceEditor) {
            ((SuiteSourceEditor) activeEditor).setSourceDocSelectionChangedListener(sourceDocSelectionChangedListener);
        } else if (activeEditor instanceof KeywordsEditorPart || activeEditor instanceof CasesEditorPart) {
            activeEditor.getEditorSite()
                    .getSelectionProvider()
                    .addSelectionChangedListener(tableDocSelectionChangedListener);
        }
    }

    private void removeDocSelectionChangedListenerFromActiveEditor(final IEditorPart activeEditor) {
        if (activeEditor instanceof SuiteSourceEditor) {
            ((SuiteSourceEditor) activeEditor).removeSourceDocSelectionChangedListener();
        } else if (activeEditor instanceof KeywordsEditorPart || activeEditor instanceof CasesEditorPart) {
            activeEditor.getEditorSite()
                    .getSelectionProvider()
                    .removeSelectionChangedListener(tableDocSelectionChangedListener);
        }
    }

}
