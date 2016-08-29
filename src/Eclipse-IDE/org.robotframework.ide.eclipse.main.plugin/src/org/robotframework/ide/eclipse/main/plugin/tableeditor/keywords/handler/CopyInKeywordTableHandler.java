/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.ArraysSerializerDeserializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.TableHandlersSupport;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.CopyInKeywordTableHandler.E4CopyInKeywordTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CopyInKeywordTableHandler extends DIParameterizedHandler<E4CopyInKeywordTableHandler> {

    public CopyInKeywordTableHandler() {
        super(E4CopyInKeywordTableHandler.class);
    }

    public static class E4CopyInKeywordTableHandler {

        @Execute
        public boolean copyContent(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final RedClipboard clipboard) {

            final PositionCoordinate[] selectedCellPositions = editor.getSelectionLayerAccessor()
                    .getSelectedPositions();

            if (selectedCellPositions.length > 0) {
               
                final RobotKeywordCall[] keywordCalls = Selections.getElementsArray(selection, RobotKeywordCall.class);
                final RobotKeywordDefinition[] keywordDefinitions = Selections.getElementsArray(selection,
                        RobotKeywordDefinition.class);

                final RobotKeywordCall[] keywordCallsCopy = ArraysSerializerDeserializer
                        .copy(RobotKeywordCall.class, keywordCalls);
                final RobotKeywordDefinition[] keywordDefinitionsCopy = ArraysSerializerDeserializer
                        .copy(RobotKeywordDefinition.class, keywordDefinitions);
                
                final PositionCoordinateSerializer[] serializablePositions = TableHandlersSupport
                        .createSerializablePositionsCoordinates(selectedCellPositions);
                
                if (keywordCallsCopy.length > 0 && keywordDefinitionsCopy.length > 0) {
                    clipboard.insertContent(serializablePositions, keywordCallsCopy, keywordDefinitionsCopy);
                } else if (keywordCallsCopy.length > 0) {
                    clipboard.insertContent(serializablePositions, keywordCallsCopy);
                } else {
                    clipboard.insertContent(serializablePositions, keywordDefinitionsCopy);
                }
                return true;
            }
            return false;
        }
    }
}
