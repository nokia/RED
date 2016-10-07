/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable;

import java.util.HashMap;
import java.util.Map;

import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.PositionCoordinateTransfer.PositionCoordinateSerializer;

import com.google.common.annotations.VisibleForTesting;

public class TableCellsStrings {

    private final Map<PositionCoordinateSerializer, TableCellStringData> tableStrings = new HashMap<>();
    
    @VisibleForTesting
    Map<PositionCoordinateSerializer, TableCellStringData> getStringsMapping() {
        return tableStrings;
    }

    public void put(final int columnPosition, final int rowPosition, final TableCellStringData data) {
        final PositionCoordinateSerializer position = new PositionCoordinateSerializer(columnPosition, rowPosition);
        final TableCellStringData textData = tableStrings.get(position);
        if (textData != null) {
            textData.rewriteFrom(data);
        } else {
            tableStrings.put(position, data);
        }
    }

    public TableCellStringData get(final int columnPosition, final int rowPosition) {
        return tableStrings.get(new PositionCoordinateSerializer(columnPosition, rowPosition));
    }
}
