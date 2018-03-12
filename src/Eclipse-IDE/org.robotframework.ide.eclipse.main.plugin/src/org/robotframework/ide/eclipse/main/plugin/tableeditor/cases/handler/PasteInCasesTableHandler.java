/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.PasteInCasesTableHandler.E4PasteInCasesTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class PasteInCasesTableHandler extends DIParameterizedHandler<E4PasteInCasesTableHandler> {

    public PasteInCasesTableHandler() {
        super(E4PasteInCasesTableHandler.class);
    }

    public static class E4PasteInCasesTableHandler {

        @Execute
        public void pasteCellContent(@Named(Selections.SELECTION) final IStructuredSelection selection,
                @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                final RobotEditorCommandsStack commandsStack, final RedClipboard clipboard) {

            final List<RobotElement> selectedElements = Selections.getElements(selection, RobotElement.class);

            final List<EditorCommand> pasteCommands = new PasteCasesCellsCommandsCollector()
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
