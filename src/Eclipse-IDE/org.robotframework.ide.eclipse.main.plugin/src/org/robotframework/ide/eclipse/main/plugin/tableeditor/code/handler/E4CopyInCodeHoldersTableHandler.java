/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.ArraysSerializerDeserializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.SerializablePositionCoordinate;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.red.viewers.Selections;

public abstract class E4CopyInCodeHoldersTableHandler {

    protected final boolean copyContent(final SelectionLayerAccessor selectionLayerAccessor,
            final IStructuredSelection selection, final RedClipboard clipboard,
            final Class<? extends RobotCodeHoldingElement<?>> holderClass) {

        final PositionCoordinate[] selectedCellPositions = selectionLayerAccessor.getSelectedPositions();

        if (selectedCellPositions.length > 0) {

            final RobotKeywordCall[] keywordCalls = Selections.getElementsArray(selection, RobotKeywordCall.class);
            final RobotCodeHoldingElement<?>[] codeHolders = Selections.getElementsArray(selection, holderClass);

            final RobotKeywordCall[] keywordCallsCopy = ArraysSerializerDeserializer.copy(RobotKeywordCall.class,
                    keywordCalls);
            final RobotCodeHoldingElement<?>[] codeHoldersCopy = ArraysSerializerDeserializer.copy(holderClass,
                    codeHolders);

            final SerializablePositionCoordinate[] serializablePositions = createPositionCoordinates(
                    selectionLayerAccessor, selectedCellPositions);

            if (keywordCallsCopy.length == 0 && codeHoldersCopy.length == 0) {
                return false;
            } else if (keywordCallsCopy.length > 0 && codeHoldersCopy.length > 0) {
                clipboard.insertContent(serializablePositions, keywordCallsCopy, codeHoldersCopy);
            } else if (keywordCallsCopy.length > 0) {
                clipboard.insertContent(serializablePositions, keywordCallsCopy);
            } else {
                clipboard.insertContent(serializablePositions, codeHoldersCopy);
            }
            return true;
        }
        return false;
    }

    private SerializablePositionCoordinate[] createPositionCoordinates(
            final SelectionLayerAccessor selectionLayerAccessor, final PositionCoordinate[] selectedCellPositions) {

        // we're filtering out positions where non-robot elements are selected (e.g. AddingToken)
        final List<PositionCoordinate> selectedCellsOnRobotElements = Stream.of(selectedCellPositions)
                .filter(coord -> selectionLayerAccessor
                        .getElementSelectedAt(coord.getRowPosition()) instanceof RobotElement)
                .collect(toList());

        return SerializablePositionCoordinate.createFrom(selectedCellsOnRobotElements.toArray(new PositionCoordinate[0]));
    }
}
