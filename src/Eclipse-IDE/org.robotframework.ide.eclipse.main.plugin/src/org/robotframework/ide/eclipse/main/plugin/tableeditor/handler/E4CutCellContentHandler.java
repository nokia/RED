/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.red.viewers.Selections;

public abstract class E4CutCellContentHandler {

    @Execute
    public void cutCellContent(@Named(Selections.SELECTION) final IStructuredSelection selection,
            @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor, final RobotEditorCommandsStack commandsStack,
            final RedClipboard clipboard) {
        
        final PositionCoordinate[] selectedCellPositions = editor.getSelectionLayerAccessor()
                .getSelectionLayer()
                .getSelectedCellPositions();

        if (selectedCellPositions.length > 0) {
           
            final List<RobotKeywordCall> keywordCallsCopy = new ArrayList<>();
            final List<RobotKeywordCall> keywordCalls = Selections.getElements(selection, RobotKeywordCall.class);
            if (!keywordCalls.isEmpty()) {
                keywordCallsCopy.addAll(TableHandlersSupport.createKeywordCallsCopy(keywordCalls));
            }

            final List<RobotKeywordDefinition> keywordDefinitionsCopy = new ArrayList<>();
            final List<RobotKeywordDefinition> keywordDefinitions = Selections.getElements(selection,
                    RobotKeywordDefinition.class);
            if (!keywordDefinitions.isEmpty()) {
                keywordDefinitionsCopy.addAll(TableHandlersSupport.createKeywordDefsCopy(keywordDefinitions));
            }
            
            final PositionCoordinateSerializer[] serializablePositions = TableHandlersSupport
                    .createSerializablePositionsCoordinates(selectedCellPositions);
            
            if (!keywordCallsCopy.isEmpty() && !keywordDefinitionsCopy.isEmpty()) {
                clipboard.insertContent(serializablePositions,
                        keywordCallsCopy.toArray(new RobotKeywordCall[0]),
                        keywordDefinitionsCopy.toArray(new RobotKeywordDefinition[0]));
            } else if (!keywordCallsCopy.isEmpty()) {
                clipboard.insertContent(serializablePositions,
                        keywordCallsCopy.toArray(new RobotKeywordCall[0]));
            } else {
                clipboard.insertContent(serializablePositions,
                        keywordDefinitionsCopy.toArray(new RobotKeywordDefinition[0]));
            }
        }

        final E4DeleteCellContentHandler deleteHandler = new E4DeleteCellContentHandler();
        deleteHandler.deleteCellContent(selection, editor, commandsStack);
    }
}
