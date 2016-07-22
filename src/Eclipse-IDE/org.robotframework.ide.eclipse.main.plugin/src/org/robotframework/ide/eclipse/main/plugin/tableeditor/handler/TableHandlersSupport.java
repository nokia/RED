/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;

/**
 * @author mmarzec
 *
 */
public class TableHandlersSupport {

    private TableHandlersSupport() {
    }
    
    public static PositionCoordinateSerializer[] createSerializablePositionsCoordinates(
            final PositionCoordinate[] selectedCellPositions) {
        final PositionCoordinateSerializer[] serializablePositions = new PositionCoordinateSerializer[selectedCellPositions.length];
        for (int i = 0; i < selectedCellPositions.length; i++) {
            serializablePositions[i] = new PositionCoordinateSerializer(selectedCellPositions[i]);
        }
        return serializablePositions;
    }
    
    public static int findNextSelectedElementRowIndex(final int initialIndex, final SelectionLayer selectionLayer) {
        final PositionCoordinate[] selectedCellPositions = selectionLayer.getSelectedCellPositions();
        for (int i = 0; i < selectedCellPositions.length; i++) {
            if(selectedCellPositions[i].rowPosition > initialIndex) {
                return selectedCellPositions[i].rowPosition;
            }
        }
        return initialIndex;
    }
    
    public static List<Integer> findSelectedColumnsIndexesByRowIndex(final int selectedElementRowIndex,
            final SelectionLayer selectionLayer) {

        final List<Integer> columnsIndexes = new ArrayList<>();
        final PositionCoordinate[] selectedCellPositions = selectionLayer.getSelectedCellPositions();
        for (int i = 0; i < selectedCellPositions.length; i++) {
            if (selectedCellPositions[i].rowPosition == selectedElementRowIndex) {
                columnsIndexes.add(selectedCellPositions[i].columnPosition);
            }
        }
        return columnsIndexes;
    }
}
