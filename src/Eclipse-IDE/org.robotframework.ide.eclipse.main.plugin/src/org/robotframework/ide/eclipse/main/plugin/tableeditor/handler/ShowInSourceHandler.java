/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.DefinitionPosition;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.ShowInSourceHandler.E4ShowInSourceHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class ShowInSourceHandler extends DIParameterizedHandler<E4ShowInSourceHandler> {

    public ShowInSourceHandler() {
        super(E4ShowInSourceHandler.class);
    }

    public static class E4ShowInSourceHandler {

        @Execute
        public Object showInSource(@Named(Selections.SELECTION) final IStructuredSelection selection,
                @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor) {

            final RobotFileInternalElement element = Selections.getSingleElement(selection, RobotFileInternalElement.class);
            
            final SuiteSourceEditor suiteEditor = editor.activateSourcePage();
            final ISelectionProvider selectionProvider = suiteEditor.getSite().getSelectionProvider();

            final DefinitionPosition position = element.getDefinitionPosition();
            selectionProvider.setSelection(new TextSelection(position.getOffset(), position.getLength()));

            return null;
        }
    }
}
