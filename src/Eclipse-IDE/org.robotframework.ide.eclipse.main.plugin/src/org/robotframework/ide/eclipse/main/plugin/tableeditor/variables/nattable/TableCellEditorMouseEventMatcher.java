/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.matcher.IMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;

/**
 * @author Michal Anglart
 *
 */
public class TableCellEditorMouseEventMatcher implements IMouseEventMatcher {

    private final String regionLabel;

    private final Collection<String> displayModes;

    private final int button;

    public TableCellEditorMouseEventMatcher(final String regionLabel) {
        this(regionLabel, new ArrayList<String>());
    }

    public TableCellEditorMouseEventMatcher(final String regionLabel, final Collection<String> displayModes) {
        this.regionLabel = regionLabel;
        this.displayModes = displayModes;
        this.button = MouseEventMatcher.LEFT_BUTTON;
    }

    @Override
    public boolean matches(final NatTable natTable, final MouseEvent event, final LabelStack regionLabels) {
        if ((this.regionLabel == null || (regionLabels != null && regionLabels.hasLabel(this.regionLabel)))
                && event.button == this.button) {

            if (event.stateMask == SWT.NONE) {
                final ILayerCell cell = natTable.getCellByPosition(natTable.getColumnPositionByX(event.x),
                        natTable.getRowPositionByY(event.y));

                if (cell != null && (displayModes.isEmpty() || displayModes.contains(cell.getDisplayMode()))) {
                    final ICellEditor cellEditor = natTable.getConfigRegistry().getConfigAttribute(
                            EditConfigAttributes.CELL_EDITOR, DisplayMode.EDIT, cell.getConfigLabels().getLabels());

                    if (cellEditor != null && cellEditor.activateAtAnyPosition()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
