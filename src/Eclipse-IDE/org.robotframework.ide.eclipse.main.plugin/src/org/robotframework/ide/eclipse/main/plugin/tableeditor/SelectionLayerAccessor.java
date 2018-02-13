/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newIdentityHashSet;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.viewers.Selections;

import com.google.common.primitives.Ints;

/**
 * @author Michal Anglart
 */
public class SelectionLayerAccessor {

    private final IRowDataProvider<? extends Object> dataProvider;

    private final SelectionLayer selectionLayer;

    private final ISelectionProvider selectionProvider;

    public SelectionLayerAccessor(final IRowDataProvider<? extends Object> dataProvider,
            final SelectionLayer selectionLayer, final ISelectionProvider selectionProvider) {
        this.dataProvider = dataProvider;
        this.selectionLayer = selectionLayer;
        this.selectionProvider = selectionProvider;
    }

    public IRowDataProvider<? extends Object> getDataProvider() {
        return dataProvider;
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

    public boolean isAnyCellSelectedInColumn(final int index) {
        for (final PositionCoordinate position : getSelectedPositions()) {
            if (position.getColumnPosition() == index) {
                return true;
            }
        }
        return false;
    }

    public void selectCellContaining(final String label) {
        final PositionCoordinate[] positions = getSelectedPositions();

        for (final PositionCoordinate coord : positions) {
            final ILayerCell cell = selectionLayer.getCellByPosition(coord.getColumnPosition(), coord.getRowPosition());
            if (cell.getDataValue() instanceof String
                    && ((String) cell.getDataValue()).toLowerCase().contains(label.toLowerCase())) {
                selectionLayer.clear();
                selectionLayer.doCommand(new SelectCellCommand(selectionLayer, coord.getColumnPosition(),
                        coord.getRowPosition(), false, false));
                return;
            }
        }
    }

    public String getLabelFromCell(final int row, final int column) {
        return (String) dataProvider.getDataValue(column, row);
    }

    public void preserveSelectionWhen(final Runnable operation) {
        preserveSelectionWhen(operation, Function.identity());
    }

    public void preserveSelectionWhen(final Runnable operation,
            final Function<PositionCoordinate, PositionCoordinate> mapper) {
        final PositionCoordinate[] positions = selectionLayer.getSelectedCellPositions();
        operation.run();
        // this has to be done separately, because it will hit locks on
        // some layers which can be already locked, so we have to schedule it
        // for later (example: adding element using key press hangs the main thread)
        SwtThread.asyncExec(() -> reestablishSelection(positions, mapper));
    }

    public void preserveElementSelectionWhen(final Runnable operation) {
        preserveElementsSelection(operation, selectionProvider.getSelection());
    }

    public void preserveElementsParentSelectionWhen(final Class<? extends RobotElement> parentClass,
            final Runnable operation) {
        final ISelection oldSelection = selectionProvider.getSelection();
        final List<Object> elements = Selections.getElements((IStructuredSelection) oldSelection, Object.class)
                .stream()
                .map(element -> getAncestorOfClass(parentClass, element))
                .filter(notNull())
                .collect(Collectors.toList());
        final Set<Object> toSelect = newIdentityHashSet();
        toSelect.addAll(elements);
        preserveElementsSelection(operation, new StructuredSelection(toSelect.toArray()));
    }

    private void preserveElementsSelection(final Runnable operation, final ISelection selectionToRestore) {
        operation.run();
        SwtThread.asyncExec(() -> {
            selectionProvider.setSelection(selectionToRestore);
            reestablishSelection(selectionLayer.getSelectedCellPositions(), Function.identity());
        });
    }

    private Object getAncestorOfClass(final Class<? extends RobotElement> parentClass, final Object element) {
        if (element instanceof RobotElement && parentClass.isAssignableFrom(element.getClass())) {
            return element;
        } else if (element instanceof RobotElement) {
            return getAncestorOfClass(parentClass, ((RobotElement) element).getParent());
        } else if (element instanceof AddingToken) {
            return getAncestorOfClass(parentClass, ((AddingToken) element).getParent());
        }
        return null;
    }

    public void selectElementInFirstCellAfterOperation(final Object elementToSelect, final Runnable operation) {
        operation.run();
        SwtThread.asyncExec(() -> {
            selectionProvider.setSelection(new StructuredSelection(elementToSelect));
            reestablishSelection(selectionLayer.getSelectedCellPositions(),
                    coordinate -> new PositionCoordinate(selectionLayer, 0, coordinate.getRowPosition()));
        });
    }

    public void selectElementPreservingSelectedColumnsAfterOperation(final Object elementToSelect,
            final Runnable operation) {
        operation.run();
        SwtThread.asyncExec(() -> {
            final Set<Integer> columns = newHashSet(Ints.asList(selectionLayer.getSelectedColumnPositions()));
            selectionProvider.setSelection(new StructuredSelection(elementToSelect));
            reestablishSelection(selectionLayer.getSelectedCellPositions(),
                    coordinate -> columns.contains(coordinate.getColumnPosition())
                            ? new PositionCoordinate(selectionLayer, coordinate.getColumnPosition(),
                                    coordinate.getRowPosition())
                            : null);
        });
    }

    public void preserveSelectionIfNotEditingArgumentWhen(final Object elementToSelect, final Runnable operation) {
        final PositionCoordinate[] positions = selectionLayer.getSelectedCellPositions();
        if (positions.length == 1 && positions[0].getColumnPosition() == 0) {
            selectElementPreservingSelectedColumnsAfterOperation(elementToSelect, operation);
        } else {
            preserveSelectionWhen(operation, Function.identity());
        }
    }

    private void reestablishSelection(final PositionCoordinate[] positions,
            final Function<PositionCoordinate, PositionCoordinate> mapper) {
        final PositionCoordinate anchor = selectionLayer.getSelectionAnchor();
        final int anchorColumn = anchor.getColumnPosition();
        final int anchorRow = anchor.getRowPosition();

        // transform, remove nulls, remove duplicates, sort
        final List<PositionCoordinate> transformedCoordinates = Stream.of(positions)
                .map(mapper)
                .filter(notNull())
                .distinct()
                .sorted((o1, o2) -> Integer.compare(o2.getColumnPosition(), o1.getColumnPosition()))
                .collect(Collectors.toList());

        selectionLayer.clear();

        boolean shouldAdd = false;
        for (final PositionCoordinate coordinate : transformedCoordinates) {
            selectionLayer.doCommand(new SelectCellCommand(selectionLayer, coordinate.getColumnPosition(),
                    coordinate.getRowPosition(), false, shouldAdd));
            shouldAdd = true;
        }
        if (selectionLayer.isCellPositionSelected(anchorColumn, anchorRow)) {
            selectionLayer.moveSelectionAnchor(anchorColumn, anchorRow);
        }
    }
}
