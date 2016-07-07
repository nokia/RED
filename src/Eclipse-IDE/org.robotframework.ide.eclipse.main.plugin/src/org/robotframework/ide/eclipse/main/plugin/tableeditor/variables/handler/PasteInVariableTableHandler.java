/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.PasteInVariableTableHandler.E4PasteInVariableTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class PasteInVariableTableHandler extends DIParameterizedHandler<E4PasteInVariableTableHandler> {

    public PasteInVariableTableHandler() {
        super(E4PasteInVariableTableHandler.class);
    }

    public static class E4PasteInVariableTableHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public void paste(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor, @Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedClipboard clipboard) {

            final List<RobotElement> selectedVariables = Selections.getElements(selection, RobotElement.class);
            final List<EditorCommand> pasteCommands = new PasteVariablesCellsCommandsCollector().collectPasteCommands(
                    editor.getSelectionLayerAccessor().getSelectionLayer(), selectedVariables, clipboard);

            for (final EditorCommand command : pasteCommands) {
                commandsStack.execute(command);
            }
        }
    }
}
