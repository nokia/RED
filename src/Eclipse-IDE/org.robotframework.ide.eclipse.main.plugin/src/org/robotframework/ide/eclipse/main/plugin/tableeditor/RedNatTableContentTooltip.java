/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.List;
import java.util.Optional;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.tooltip.NatTableContentTooltip;
import org.eclipse.swt.widgets.Event;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;

import com.google.common.base.Joiner;


public class RedNatTableContentTooltip extends NatTableContentTooltip {

    private final SuiteFileMarkersContainer markersContainer;

    protected final IRowDataProvider<?> dataProvider;

    public RedNatTableContentTooltip(final NatTable natTable, final SuiteFileMarkersContainer markersContainer,
            final IRowDataProvider<?> dataProvider) {
        super(natTable);
        this.markersContainer = markersContainer;
        this.dataProvider = dataProvider;
    }

    @Override
    protected String getText(final Event event) {
        final int col = natTable.getColumnPositionByX(event.x);
        final int row = natTable.getRowPositionByY(event.y);

        final ILayerCell cell = natTable.getCellByPosition(col, row);
        if (cell != null) {

            final LabelStack labels = cell.getConfigLabels();

            if (labels.hasLabel(MarkersLabelAccumulator.ERROR_MARKER_LABEL)
                    || labels.hasLabel(MarkersLabelAccumulator.WARNING_MARKER_LABEL)
                    || labels.hasLabel(MarkersLabelAccumulator.TASK_LABEL)) {
                // substracting -1 due to columns header row
                final Optional<RobotFileInternalElement> rowObject = getRowModelObject(row - 1);

                final List<String> markerDescriptions = markersContainer.getMarkersMessagesFor(rowObject);
                return markerDescriptions.isEmpty() ? null : createMessage(markerDescriptions);
            } else {
                return super.getText(event);
            }
        }
        return null;
    }

    protected Optional<RobotFileInternalElement> getRowModelObject(final int rowPosition) {
        final Object rowObject = dataProvider.getRowObject(rowPosition);
        return rowObject instanceof RobotFileInternalElement ? Optional.of((RobotFileInternalElement) rowObject)
                : Optional.empty();
    }

    private String createMessage(final List<String> markerDescriptions) {
        if (markerDescriptions.isEmpty()) {
            return null;
        } else if (markerDescriptions.size() == 1) {
            return Joiner.on('\n').join(markerDescriptions);
        } else {
            return "Multiple markers at this element\n- " + Joiner.on("\n- ").join(markerDescriptions);
        }
    }
}
