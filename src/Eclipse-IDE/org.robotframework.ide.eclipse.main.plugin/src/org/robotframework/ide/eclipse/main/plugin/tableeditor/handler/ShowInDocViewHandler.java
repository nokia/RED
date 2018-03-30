/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.ShowInDocViewHandler.E4ShowInDocViewHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.Documentations;
import org.robotframework.red.commands.DIParameterizedHandler;


public class ShowInDocViewHandler extends DIParameterizedHandler<E4ShowInDocViewHandler> {

    public ShowInDocViewHandler() {
        super(E4ShowInDocViewHandler.class);
    }

    public static class E4ShowInDocViewHandler {

        @Execute
        public void showInDocView(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor) {
            final IWorkbenchPage page = editor.getSite().getPage();
            Documentations.markViewSyncBroken(page);

            if (editor.getActiveEditor() instanceof SuiteSourceEditor) {
                final SuiteSourceEditor sourceEditor = (SuiteSourceEditor) editor.getActiveEditor();
                final RobotSuiteFile suiteModel = editor.provideSuiteModel();
                final IDocument document = sourceEditor.getDocument();
                final int offset = sourceEditor.getViewer().getTextWidget().getCaretOffset();

                Documentations.showDocForEditorSourceSelection(page, suiteModel, document, offset);
            } else {
                final SelectionLayerAccessor selectionLayerAccessor = editor.getSelectionLayerAccessor();

                Documentations.showDocForEditorTablesSelection(page, selectionLayerAccessor);
            }
        }
    }
}
