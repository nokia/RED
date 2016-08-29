/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.robotframework.red.swt.SwtThread;

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

    public int findNextSelectedElementRowIndex(final int initialRowIndex) {
        final PositionCoordinate[] positions = getSelectedPositions();
        for (final PositionCoordinate position : positions) {
            if (position.getRowPosition() > initialRowIndex) {
                return position.getRowPosition();
            }
        }
        return initialRowIndex;
    }

    public List<Integer> findSelectedColumnsIndexesByRowIndex(final int rowIndex) {
        final List<Integer> columnsIndexes = new ArrayList<>();
        for (final PositionCoordinate position : getSelectedPositions()) {
            if (position.getRowPosition() == rowIndex) {
                columnsIndexes.add(position.getColumnPosition());
            }
        }
        return columnsIndexes;
    }

    public void preserveSelectionWhen(final Runnable operation) {
        final PositionCoordinate[] positions = selectionLayer.getSelectedCellPositions();
        operation.run();
        // this has to be done separately, because it will hit locks on
        // some layers which can be already locked, so we have to schedule it
        // for later (example: adding element using key press hangs the main thread)
        SwtThread.asyncExec(new Runnable() {

            @Override
            public void run() {
                reestablishSelection(selectionLayer, positions);
            }
        });
    }

    public void preserveElementSelectionWhen(final Runnable operation) {
        final ISelection oldSelection = selectionProvider.getSelection();
        operation.run();
        selectionProvider.setSelection(oldSelection);

        final PositionCoordinate[] positions = selectionLayer.getSelectedCellPositions();
        SwtThread.asyncExec(new Runnable() {

            @Override
            public void run() {
                reestablishSelection(selectionLayer, positions);
            }
        });
    }

    private void reestablishSelection(final ILayer layer, final PositionCoordinate[] positions) {
        boolean shouldAdd = false;
        for (final PositionCoordinate coordinate : positions) {
            layer.doCommand(new SelectCellCommand(selectionLayer, coordinate.getColumnPosition(),
                    coordinate.getRowPosition(), false, shouldAdd));
            shouldAdd = true;
        }
    }
}
