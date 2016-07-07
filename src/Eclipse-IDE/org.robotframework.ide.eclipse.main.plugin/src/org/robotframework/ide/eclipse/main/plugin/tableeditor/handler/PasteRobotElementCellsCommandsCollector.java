/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;

/**
 * @author mmarzec
 *
 */
public abstract class PasteRobotElementCellsCommandsCollector {

    public List<EditorCommand> collectPasteCommands(final SelectionLayer selectionLayer,
            final List<RobotElement> selectedElements, final RedClipboard clipboard) {

        final List<EditorCommand> pasteCommands = new ArrayList<>();

        if (selectionLayer.getSelectedCellPositions().length == 1 && clipboard.hasText()) {
            final String textToPaste = clipboard.getText();
            pasteCommands.addAll(collectPasteCommandsForSelectedElement(selectedElements.get(0),
                    newArrayList(textToPaste), selectionLayer.getSelectedCellPositions()[0].getColumnPosition(),
                    selectionLayer.getColumnCount()));

        } else if (!selectedElements.isEmpty() && hasRobotElementsInClipboard(clipboard)
                && hasPositionsCoordinatesInClipboard(clipboard)) {

            final RobotElement[] robotElementsFromClipboard = getRobotElementsFromClipboard(clipboard);
            final PositionCoordinateSerializer[] cellPositionsFromClipboard = getPositionsCoordinatesFromClipboard(
                    clipboard);

            if (robotElementsFromClipboard != null && robotElementsFromClipboard.length > 0
                    && cellPositionsFromClipboard != null && cellPositionsFromClipboard.length > 0) {
                final int tableColumnsCount = selectionLayer.getColumnCount();
                int clipboardElementsIndex = 0;
                int currentClipboardElementRowIndex = cellPositionsFromClipboard[0].getRowPosition();
                for (final RobotElement selectedElement : selectedElements) {
                    final RobotElement elementFromClipboard = robotElementsFromClipboard[clipboardElementsIndex];
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
                            pasteCommands.addAll(collectPasteCommandsForSelectedElement(selectedElement,
                                    valuesToPaste, selectedElementColumnsIndexes.get(i), tableColumnsCount));
                        }
                    }
                }
            }
        }
        return pasteCommands;
    }

    protected boolean hasPositionsCoordinatesInClipboard(final RedClipboard clipboard) {
        return clipboard.hasPositionsCoordinates();
    }

    protected PositionCoordinateSerializer[] getPositionsCoordinatesFromClipboard(final RedClipboard clipboard) {
        return clipboard.getPositionsCoordinates();
    }

    protected abstract boolean hasRobotElementsInClipboard(final RedClipboard clipboard);

    protected abstract RobotElement[] getRobotElementsFromClipboard(final RedClipboard clipboard);

    protected abstract int findSelectedElementTableIndex(final RobotElement section,
            final RobotElement selectedElement);

    protected abstract List<String> findValuesToPaste(final RobotElement elementFromClipboard,
            final int clipboardElementColumnIndex, final int tableColumnsCount);

    protected abstract List<EditorCommand> collectPasteCommandsForSelectedElement(final RobotElement selectedElement,
            final List<String> valuesToPaste, final int selectedElementColumnIndex, final int tableColumnsCount);

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

        final int nextRowIndex = currentRowIndex;
        for (int i = 0; i < positionsCoordinates.length; i++) {
            if (positionsCoordinates[i].getRowPosition() > currentRowIndex) {
                return positionsCoordinates[i].getRowPosition();
            }
        }
        return nextRowIndex;
    }
}
