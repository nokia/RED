/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;

/**
 * @author Michal Anglart
 *
 */
public class SelectionLayerAccessor {

    private final SelectionLayer selectionLayer;

    private final ISelectionProvider selectionProvider;

    public SelectionLayerAccessor(final SelectionLayer selectionLayer, final ISelectionProvider selectionProvider) {
        this.selectionLayer = selectionLayer;
        this.selectionProvider = selectionProvider;
    }

    public SelectionLayer getSelectionLayer() {
        return selectionLayer;
    }

    public int getColumnCount() {
        return selectionLayer.getColumnCount();
    }

    public PositionCoordinate[] getSelectedPositions() {
        return selectionLayer.getSelectedCellPositions();
    }

    public void clear() {
        selectionLayer.clear();
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

    public void expandSelectionToWholeRows() {
        final Set<Integer> rowsToSelect = new LinkedHashSet<>();
        for (final PositionCoordinate selectedCellPosition : getSelectedPositions()) {
            rowsToSelect.add(selectedCellPosition.rowPosition);
        }
        clear();
        for (final int rowToSelect : rowsToSelect) {
            for (int i = 0; i < selectionLayer.getColumnCount(); i++) {
                selectionLayer.selectRow(0, rowToSelect, false, true);
            }
        }
    }
}
