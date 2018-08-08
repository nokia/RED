/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.EmptyCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler.PasteInTasksTableHandler.E4PasteInTasksTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class PasteInTasksTableHandler extends DIParameterizedHandler<E4PasteInTasksTableHandler> {

    public PasteInTasksTableHandler() {
        super(E4PasteInTasksTableHandler.class);
    }

    public static class E4PasteInTasksTableHandler {

        @Execute
        public void pasteCellContent(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection,
                final RobotEditorCommandsStack commandsStack, final RedClipboard clipboard) {

            final List<RobotElement> selectedElements = Selections.getElements(selection, RobotElement.class);

            final List<EditorCommand> pasteCommands = new PasteTasksCellsCommandsCollector()
                    .collectPasteCommands(editor.getSelectionLayerAccessor(), selectedElements, clipboard);

            final EditorCommand parentCommand = new EmptyCommand();
            for (int i = pasteCommands.size() - 1; i >= 0; i--) {
                final EditorCommand command = pasteCommands.get(i);
                command.setParent(parentCommand);
                commandsStack.execute(command);
            }
        }
    }
}
