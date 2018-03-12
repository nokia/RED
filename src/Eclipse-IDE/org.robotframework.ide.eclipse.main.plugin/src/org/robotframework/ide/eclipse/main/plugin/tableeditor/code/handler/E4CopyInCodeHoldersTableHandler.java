/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.ArraysSerializerDeserializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Predicate;

public abstract class E4CopyInCodeHoldersTableHandler {

    protected final boolean copyContent(final SelectionLayerAccessor selectionLayerAccessor,
            final IStructuredSelection selection, final RedClipboard clipboard) {

        final PositionCoordinate[] selectedCellPositions = selectionLayerAccessor.getSelectedPositions();

        if (selectedCellPositions.length > 0) {

            final RobotKeywordCall[] keywordCalls = Selections.getElementsArray(selection, RobotKeywordCall.class);
            final RobotCodeHoldingElement<?>[] codeHolders = Selections.getElementsArray(selection,
                    getCodeHolderClass());

            final RobotKeywordCall[] keywordCallsCopy = ArraysSerializerDeserializer.copy(RobotKeywordCall.class,
                    keywordCalls);
            final RobotCodeHoldingElement<?>[] codeHoldersCopy = ArraysSerializerDeserializer.copy(getCodeHolderClass(),
                    codeHolders);

            final PositionCoordinateSerializer[] serializablePositions = createPositionCoordinates(
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

    protected abstract Class<? extends RobotCodeHoldingElement<?>> getCodeHolderClass();

    private PositionCoordinateSerializer[] createPositionCoordinates(
            final SelectionLayerAccessor selectionLayerAccessor, final PositionCoordinate[] selectedCellPositions) {

        // we're filtering out positions where non-robot elements are selected (e.g. AddingToken)
        final List<PositionCoordinate> selectedCells = newArrayList(selectedCellPositions);
        final List<PositionCoordinate> selectedCellsOnRobotElements = newArrayList(
                filter(selectedCells, new Predicate<PositionCoordinate>() {

                    @Override
                    public boolean apply(final PositionCoordinate coord) {
                        return selectionLayerAccessor
                                .getElementSelectedAt(coord.getRowPosition()) instanceof RobotElement;
                    }
                }));

        return PositionCoordinateSerializer.createFrom(selectedCellsOnRobotElements.toArray(new PositionCoordinate[0]));
    }
}
