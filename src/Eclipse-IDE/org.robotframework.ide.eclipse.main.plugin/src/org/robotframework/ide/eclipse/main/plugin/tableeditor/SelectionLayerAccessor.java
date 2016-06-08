/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * @author Michal Anglart
 *
 */
public class SelectionLayerAccessor {

    private final SelectionLayer selectionLayer;

    public SelectionLayerAccessor(final SelectionLayer selectionLayer) {
        this.selectionLayer = selectionLayer;
    }

    public SelectionLayer getSelectionLayer() {
        return selectionLayer;
    }

    public boolean onlyFullRowsAreSelected() {
        if (selectionLayer.getSelectedCellPositions().length == 0) {
            return false;
        }
        for (final PositionCoordinate selectedCellPosition : selectionLayer.getSelectedCellPositions()) {
            if (!selectionLayer.isRowPositionFullySelected(selectedCellPosition.rowPosition)) {
                return false;
            }
        }
        return true;
    }

    public boolean noFullRowIsSelected() {
        return selectionLayer.getFullySelectedRowPositions().length == 0;
    }
}
