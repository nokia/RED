/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.PasteInKeywordTableHandler.E4PasteInKeywordTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class PasteInKeywordTableHandler extends DIParameterizedHandler<E4PasteInKeywordTableHandler> {

    public PasteInKeywordTableHandler() {
        super(E4PasteInKeywordTableHandler.class);
    }

    public static class E4PasteInKeywordTableHandler {

        @Execute
        public void pasteCellContent(@Named(Selections.SELECTION) final IStructuredSelection selection,
                @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                final RobotEditorCommandsStack commandsStack, final RedClipboard clipboard) {

            final List<RobotElement> selectedKeywords = Selections.getElements(selection, RobotElement.class);

            final List<EditorCommand> pasteCommands = new PasteKeywordsCellsCommandsCollector()
                    .collectPasteCommands(editor.getSelectionLayerAccessor(), selectedKeywords, clipboard);

            final EditorCommand parentCommand = new EmptyCommand(); 
            for (final EditorCommand command : pasteCommands) {
                command.setParent(parentCommand);
                commandsStack.execute(command);
            }
        }
    }
}
