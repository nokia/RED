/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;
import org.robotframework.services.event.RedEventBroker;

class MultipleCellTableUpdater {

    private final NatTableAssistantContext tableContext;

    private final IRowDataProvider<?> dataProvider;

    private final IEventBroker eventBroker;

    MultipleCellTableUpdater(final NatTableAssistantContext tableContext, final IRowDataProvider<?> dataProvider) {
        this(tableContext, dataProvider, PlatformUI.getWorkbench().getService(IEventBroker.class));
    }

    MultipleCellTableUpdater(final NatTableAssistantContext tableContext, final IRowDataProvider<?> dataProvider,
            final IEventBroker eventBroker) {
        this.tableContext = tableContext;
        this.dataProvider = dataProvider;
        this.eventBroker = eventBroker;
    }

    boolean shouldInsertMultipleCells(final List<String> values) {
        return values.size() > 1 && !hasValuesInSucceedingColumns();
    }

    boolean shouldInsertMultipleCellsWithoutColumnExceeding(final List<String> values) {
        return values.size() > 1 && !hasValuesInSucceedingColumns()
                && tableContext.getColumn() + values.size() < dataProvider.getColumnCount();
    }

    void insertMultipleCells(final List<String> values) {
        for (int i = 0; i < values.size(); i++) {
            dataProvider.setDataValue(tableContext.getColumn() + i, tableContext.getRow(), values.get(i));
        }
        if (tableContext.getColumn() + values.size() > dataProvider.getColumnCount()) {
            RedEventBroker.using(eventBroker).send(RobotModelEvents.COLUMN_COUNT_EXCEEDED,
                    dataProvider.getRowObject(tableContext.getRow()));
        }
    }

    private boolean hasValuesInSucceedingColumns() {
        int nextColumnIndex = tableContext.getColumn() + 1;
        while (nextColumnIndex < dataProvider.getColumnCount()) {
            final String cellContent = (String) dataProvider.getDataValue(nextColumnIndex, tableContext.getRow());
            if (!cellContent.isEmpty()) {
                return true;
            }
            nextColumnIndex++;
        }
        return false;
    }

}
