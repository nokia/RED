/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.PasteKeywordsCellsCommandsCollector;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

public abstract class E4PasteCellContentHandler {

    @Execute
    public void pasteCellContent(@Named(Selections.SELECTION) final IStructuredSelection selection,
            @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
            final RobotEditorCommandsStack commandsStack, final RedClipboard clipboard) {

        final SelectionLayer selectionLayer = editor.getSelectionLayerAccessor().getSelectionLayer();
        final List<RobotElement> selectedKeywords = Selections.getElements(selection, RobotElement.class);

        final List<EditorCommand> pasteCommands = new PasteKeywordsCellsCommandsCollector()
                .collectPasteCommands(selectionLayer, selectedKeywords, clipboard);

        for (final EditorCommand command : pasteCommands) {
            commandsStack.execute(command);
        }
    }

    protected abstract Optional<? extends EditorCommand> provideCommandForAttributeChange(RobotElement element,
            int index, int noOfColumns, String newAttribute);

}
