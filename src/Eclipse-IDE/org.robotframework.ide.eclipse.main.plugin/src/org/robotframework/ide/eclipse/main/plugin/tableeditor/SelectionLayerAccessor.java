/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Sets.newHashSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.robotframework.red.swt.SwtThread;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.primitives.Ints;

/**
 * @author Michal Anglart
 *
 */
public class SelectionLayerAccessor {

    private final IRowDataProvider<? extends Object> dataProvider;

    private final SelectionLayer selectionLayer;

    private final ISelectionProvider selectionProvider;

    public SelectionLayerAccessor(final IRowDataProvider<? extends Object> dataProvider,
            final SelectionLayer selectionLayer,
            final ISelectionProvider selectionProvider) {
        this.dataProvider = dataProvider;
        this.selectionLayer = selectionLayer;
        this.selectionProvider = selectionProvider;
    }

    public int getLastSelectedRowPosition() {
        final PositionCoordinate lastSelectedCellPosition = selectionLayer.getLastSelectedCellPosition();
        return lastSelectedCellPosition != null ? lastSelectedCellPosition.getRowPosition() : -1;
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

    public Object getElementSelectedAt(final int row) {
        return selectionLayer.isRowPositionSelected(row) ? dataProvider.getRowObject(row) : null;
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
            selectionLayer.selectRow(0, rowToSelect, false, true);
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
        preserveSelectionWhen(operation, Functions.<PositionCoordinate> identity());
    }

    public void preserveSelectionWhen(final Runnable operation,
            final Function<PositionCoordinate, PositionCoordinate> transform) {
        final PositionCoordinate[] positions = selectionLayer.getSelectedCellPositions();
        operation.run();
        // this has to be done separately, because it will hit locks on
        // some layers which can be already locked, so we have to schedule it
        // for later (example: adding element using key press hangs the main thread)
        SwtThread.asyncExec(new Runnable() {

            @Override
            public void run() {
                reestablishSelection(selectionLayer, positions, transform);
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
                reestablishSelection(selectionLayer, positions, Functions.<PositionCoordinate> identity());
            }
        });
    }

    public void selectElementInFirstCellAfterOperation(final Object elementToSelect, final Runnable operation) {
        operation.run();
        SwtThread.asyncExec(new Runnable() {
            @Override
            public void run() {
                selectionProvider.setSelection(new StructuredSelection(elementToSelect));
                final PositionCoordinate[] newlySelected = selectionLayer.getSelectedCellPositions();
                reestablishSelection(selectionLayer, newlySelected, new Function<PositionCoordinate, PositionCoordinate>() {
                
                            @Override
                            public PositionCoordinate apply(final PositionCoordinate coordinate) {
                                return new PositionCoordinate(selectionLayer, 0, coordinate.getRowPosition());
                            }
                        });
            }
        });
    }

    public void selectElementPreservingSelectedColumnsAfterOperation(final Object elementToSelect,
            final Runnable operation) {
        operation.run();
        SwtThread.asyncExec(new Runnable() {
            @Override
            public void run() {
                final HashSet<Integer> columns = newHashSet(Ints.asList(selectionLayer.getSelectedColumnPositions()));
                selectionProvider.setSelection(new StructuredSelection(elementToSelect));
                final PositionCoordinate[] newlySelected = selectionLayer.getSelectedCellPositions();
        
                reestablishSelection(selectionLayer, newlySelected, new Function<PositionCoordinate, PositionCoordinate>() {
                
                            @Override
                            public PositionCoordinate apply(final PositionCoordinate coordinate) {
                                if (columns.contains(coordinate.getColumnPosition())) {
                                    return new PositionCoordinate(selectionLayer, coordinate.getColumnPosition(),
                                        coordinate.getRowPosition());
                                }
                                return null;
                            }
                        });
            }
        });
    }

    private void reestablishSelection(final SelectionLayer layer, final PositionCoordinate[] positions,
            final Function<PositionCoordinate, PositionCoordinate> mapping) {
        layer.clear();

        // transform, remove nulls, remove duplicates, sort
        final List<PositionCoordinate> transformedCoordinates = newArrayList(
                newHashSet(filter(transform(newArrayList(positions), mapping), notNull())));
        Collections.sort(transformedCoordinates, new Comparator<PositionCoordinate>() {

            @Override
            public int compare(final PositionCoordinate o1, final PositionCoordinate o2) {
                return Integer.compare(o2.getColumnPosition(), o1.getColumnPosition());
            }
        });

        boolean shouldAdd = false;
        for (final PositionCoordinate coordinate : transformedCoordinates) {
            layer.doCommand(new SelectCellCommand(selectionLayer, coordinate.getColumnPosition(),
                    coordinate.getRowPosition(), false, shouldAdd));
            shouldAdd = true;
        }
    }
}
