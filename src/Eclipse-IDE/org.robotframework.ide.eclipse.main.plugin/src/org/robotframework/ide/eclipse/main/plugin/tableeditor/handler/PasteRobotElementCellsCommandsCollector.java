/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;

/**
 * @author mmarzec
 *
 */
public abstract class PasteRobotElementCellsCommandsCollector {

    public List<EditorCommand> collectPasteCommands(final SelectionLayerAccessor selectionLayerAccessor,
            final List<RobotElement> selectedElements, final RedClipboard clipboard) {

        final List<EditorCommand> pasteCommands = new ArrayList<>();

        if (selectionLayerAccessor.getSelectedPositions().length == 1 && clipboard.hasText()
                && !selectedElements.isEmpty()) {
            final String textToPaste = clipboard.getText();
            pasteCommands.addAll(collectPasteCommandsForSelectedElement(selectedElements.get(0),
                    newArrayList(textToPaste), selectionLayerAccessor.getSelectedPositions()[0].getColumnPosition(),
                    selectionLayerAccessor.getColumnCount()));

        } else if (!selectedElements.isEmpty() && hasRobotElementsInClipboard(clipboard)
                && hasPositionsCoordinatesInClipboard(clipboard)) {

            final RobotElement[] robotElementsFromClipboard = getRobotElementsFromClipboard(clipboard);
            final PositionCoordinateSerializer[] cellPositionsFromClipboard = getPositionsCoordinatesFromClipboard(
                    clipboard);

            if (robotElementsFromClipboard != null && robotElementsFromClipboard.length > 0
                    && cellPositionsFromClipboard != null && cellPositionsFromClipboard.length > 0) {
                final int tableColumnsCount = selectionLayerAccessor.getColumnCount();
                int clipboardElementsCounter = 0;
                int currentClipboardElementRowIndex = cellPositionsFromClipboard[0].getRowPosition();
                int currentSelectedElementRowIndex = 0;
                for (final RobotElement selectedElement : selectedElements) {
                    final RobotElement elementFromClipboard = robotElementsFromClipboard[clipboardElementsCounter];
                    if (clipboardElementsCounter + 1 < robotElementsFromClipboard.length) {
                        clipboardElementsCounter++;
                    } else {
                        clipboardElementsCounter = 0;
                    }
                    currentSelectedElementRowIndex = selectionLayerAccessor
                            .findNextSelectedElementRowIndex(currentSelectedElementRowIndex);
                    final List<Integer> selectedElementColumnsIndexes = selectionLayerAccessor
                            .findSelectedColumnsIndexesByRowIndex(currentSelectedElementRowIndex);
                    final List<Integer> clipboardElementColumnsIndexes = findCurrentClipboardElementColumnsIndexes(
                            currentClipboardElementRowIndex, cellPositionsFromClipboard);
                    currentClipboardElementRowIndex = calculateNextClipboardElementRowIndex(
                            currentClipboardElementRowIndex, cellPositionsFromClipboard);
                    if (!clipboardElementColumnsIndexes.isEmpty()) {
                        int clipboardElementColumnsCounter = 0;
                        for (int i = 0; i < selectedElementColumnsIndexes.size(); i++) {
                            final int clipboardElementColumnIndex = clipboardElementColumnsIndexes
                                    .get(clipboardElementColumnsCounter);
                            if (clipboardElementColumnsCounter + 1 < clipboardElementColumnsIndexes.size()) {
                                clipboardElementColumnsCounter++;
                            } else {
                                clipboardElementColumnsCounter = 0;
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

    protected abstract List<String> findValuesToPaste(final RobotElement elementFromClipboard,
            final int clipboardElementColumnIndex, final int tableColumnsCount);

    protected abstract List<EditorCommand> collectPasteCommandsForSelectedElement(final RobotElement selectedElement,
            final List<String> valuesToPaste, final int selectedElementColumnIndex, final int tableColumnsCount);

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
