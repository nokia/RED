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
import org.eclipse.swt.dnd.Clipboard;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;

/**
 * @author mmarzec
 *
 */
public abstract class PasteRobotElementCellsCommandsCollector {

    public List<EditorCommand> collectPasteCommands(final SelectionLayer selectionLayer,
            final List<RobotElement> selectedElements, final Clipboard clipboard) {

        final List<EditorCommand> pasteCommands = new ArrayList<>();

        if (!selectedElements.isEmpty() && hasRobotElementsInClipboard(clipboard)
                && PositionCoordinateTransfer.hasPositionsCoordinates(clipboard)) {

            final RobotElement[] robotElementsFromClipboard = getRobotElementsFromClipboard(clipboard);
            final PositionCoordinateSerializer[] cellPositionsFromClipboard = TableHandlersSupport
                    .getPositionsCoordinatesFromClipboard(clipboard);

            if (robotElementsFromClipboard != null && robotElementsFromClipboard.length > 0
                    && cellPositionsFromClipboard != null && cellPositionsFromClipboard.length > 0) {
                final int tableColumnsCount = selectionLayer.getColumnCount();
                int clipboardElementsIndex = 0;
                int currentClipboardElementRowIndex = cellPositionsFromClipboard[0].getRowPosition();
                for (final RobotElement selectedElement : selectedElements) {
                    RobotElement elementFromClipboard = robotElementsFromClipboard[clipboardElementsIndex];
                    if (clipboardElementsIndex + 1 < robotElementsFromClipboard.length) {
                        clipboardElementsIndex++;
                    }
                    final List<Integer> selectedElementColumnsIndexes = findSelectedColumnsIndexes(
                            findSelectedElementTableIndex(selectedElement.getParent(), selectedElement),
                            selectionLayer);
                    final List<Integer> clipboardElementColumnsIndexes = findCurrentClipboardElementColumnsIndexes(
                            currentClipboardElementRowIndex, cellPositionsFromClipboard);
                    currentClipboardElementRowIndex = calculateNextClipboardElementRowIndex(
                            currentClipboardElementRowIndex, cellPositionsFromClipboard);
                    if (!clipboardElementColumnsIndexes.isEmpty()) {
                        for (int i = 0; i < selectedElementColumnsIndexes.size(); i++) {
                            int clipboardElementColumnIndex = clipboardElementColumnsIndexes.get(0);
                            if (i > 0 && i < clipboardElementColumnsIndexes.size()) {
                                clipboardElementColumnIndex = clipboardElementColumnsIndexes.get(i);
                            }
                            final List<String> valuesToPaste = findValuesToPaste(elementFromClipboard,
                                    clipboardElementColumnIndex, tableColumnsCount);
                            collectPasteCommandsForSelectedElement(selectedElement,
                                    selectedElementColumnsIndexes.get(i), valuesToPaste, tableColumnsCount,
                                    pasteCommands);
                        }
                    }
                }
            }

        }

        return pasteCommands;

    }

    protected abstract boolean hasRobotElementsInClipboard(final Clipboard clipboard);

    protected abstract RobotElement[] getRobotElementsFromClipboard(final Clipboard clipboard);

    protected abstract int findSelectedElementTableIndex(final RobotElement section,
            final RobotElement selectedElement);

    protected abstract List<String> findValuesToPaste(final RobotElement elementFromClipboard,
            final int clipboardElementColumnIndex, final int tableColumnsCount);

    protected abstract void collectPasteCommandsForSelectedElement(final RobotElement selectedElement,
            final int selectedElementColumnIndex, final List<String> valuesToPaste, final int tableColumnsCount,
            final List<EditorCommand> pasteCommands);

    private List<Integer> findSelectedColumnsIndexes(final int selectedElementTableIndex,
            final SelectionLayer selectionLayer) {

        final List<Integer> columnsIndexes = new ArrayList<>();
        final PositionCoordinate[] selectedCellPositions = selectionLayer.getSelectedCellPositions();
        for (int i = 0; i < selectedCellPositions.length; i++) {
            if (selectedCellPositions[i].rowPosition == selectedElementTableIndex) {
                columnsIndexes.add(selectedCellPositions[i].columnPosition);
            }
        }
        return columnsIndexes;
    }

    private List<Integer> findCurrentClipboardElementColumnsIndexes(final int currentClipboardElementRowIndex,
            final PositionCoordinateSerializer[] positionsCoordinates) {

        final List<Integer> clipboardElementColumnsIndexes = new ArrayList<>();
        for (int i = 0; i < positionsCoordinates.length; i++) {
            if (positionsCoordinates[i].getRowPosition() == currentClipboardElementRowIndex) {
                clipboardElementColumnsIndexes.add(positionsCoordinates[i].getColumnPosition());
            }
        }
        return clipboardElementColumnsIndexes;
    }

    private int calculateNextClipboardElementRowIndex(final int currentRowIndex,
            final PositionCoordinateSerializer[] positionsCoordinates) {

        int nextRowIndex = currentRowIndex;
        for (int i = 0; i < positionsCoordinates.length; i++) {
            if (positionsCoordinates[i].getRowPosition() > currentRowIndex) {
                return positionsCoordinates[i].getRowPosition();
            }
        }
        return nextRowIndex;
    }
}
