/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.propertytester;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.ViewerCell;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.red.viewers.Viewers;

import com.google.common.base.Preconditions;

public class TableCellPropertyTester extends PropertyTester {

    @Override
    public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {
        Preconditions.checkArgument(receiver instanceof RobotFormEditor,
                "Property tester is unable to test properties of " + receiver.getClass().getName()
                        + ". It should be used with " + RobotFormEditor.class.getName());

        if (expectedValue instanceof Boolean) {
            return testProperty((RobotFormEditor) receiver, property, ((Boolean) expectedValue).booleanValue());
        } else if (expectedValue instanceof Integer) {
            return testProperty((RobotFormEditor) receiver, property, ((Integer) expectedValue).intValue());
        }
        return false;
    }

    private boolean testProperty(final RobotFormEditor editor, final String property, final boolean expected) {
        if ("thereIsAFocusedCell".equals(property)) {
            final FocusedViewerAccessor viewerAccessor = editor.getFocusedViewerAccessor();
            if (viewerAccessor == null) {
                return false;
            }
            return viewerAccessor.getFocusedCell() != null == expected;

        } else if ("focusedCellHasContent".equals(property)) {
            final FocusedViewerAccessor viewerAccessor = editor.getFocusedViewerAccessor();
            if (viewerAccessor == null) {
                return false;
            }
            final ViewerCell cell = viewerAccessor.getFocusedCell();

            return (cell != null && !cell.getText().isEmpty()) == expected;
        } else if ("onlyFullRowsAreSelected".equals(property)) {
            final SelectionLayerAccessor selectionLayerAccessor = editor.getSelectionLayerAccessor();
            if (selectionLayerAccessor == null) {
                return false;
            }
            return selectionLayerAccessor.onlyFullRowsAreSelected() == expected;

        } else if ("noFullRowIsSelected".equals(property)) {
            final SelectionLayerAccessor selectionLayerAccessor = editor.getSelectionLayerAccessor();
            if (selectionLayerAccessor == null) {
                return false;
            }
            return selectionLayerAccessor.noFullRowIsSelected() == expected;
        }
        return false;
    }

    private boolean testProperty(final RobotFormEditor editor, final String property, final int expected) {
        if ("focusedCellHasIndex".equals(property)) {
            final FocusedViewerAccessor viewerAccessor = editor.getFocusedViewerAccessor();
            if (viewerAccessor == null) {
                return false;
            }
            final ViewerCell focusedCell = viewerAccessor.getFocusedCell();
            final int positionIndex = Viewers.createOrderIndexToPositionIndex(viewerAccessor.getViewer(),
                    focusedCell.getColumnIndex());
            return focusedCell != null && positionIndex == expected;
        } else if ("numberOfSelectedCellEquals".equals(property)) {
            final SelectionLayerAccessor selectionLayerAccessor = editor.getSelectionLayerAccessor();
            if (selectionLayerAccessor == null) {
                return false;
            }
            return selectionLayerAccessor.getSelectionLayer().getSelectedCellPositions().length == expected;
        }
        return false;
    }
}
