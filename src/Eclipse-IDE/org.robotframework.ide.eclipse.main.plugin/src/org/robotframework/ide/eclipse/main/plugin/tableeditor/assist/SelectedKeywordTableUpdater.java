/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;
import org.robotframework.services.event.RedEventBroker;

class SelectedKeywordTableUpdater {

    private final NatTableAssistantContext tableContext;

    private final IRowDataProvider<?> dataProvider;

    private final IEventBroker eventBroker;

    SelectedKeywordTableUpdater(final NatTableAssistantContext tableContext, final IRowDataProvider<?> dataProvider) {
        this(tableContext, dataProvider, PlatformUI.getWorkbench().getService(IEventBroker.class));
    }

    SelectedKeywordTableUpdater(final NatTableAssistantContext tableContext, final IRowDataProvider<?> dataProvider,
            final IEventBroker eventBroker) {
        this.tableContext = tableContext;
        this.dataProvider = dataProvider;
        this.eventBroker = eventBroker;
    }

    boolean shouldInsertWithArgs(final RedKeywordProposal proposedKeyword,
            final Predicate<List<String>> additionalInsertPredicate) {
        final List<String> values = getValuesToInsert(proposedKeyword);
        return values.size() > 1 && !hasValuesInSucceedingColumns() && additionalInsertPredicate.test(values);
    }

    void insertCallWithArgs(final RedKeywordProposal proposedKeyword) {
        final List<String> values = getValuesToInsert(proposedKeyword);
        for (int i = 0; i < values.size(); i++) {
            dataProvider.setDataValue(tableContext.getColumn() + i, tableContext.getRow(), values.get(i));
        }
        if (tableContext.getColumn() + values.size() > dataProvider.getColumnCount()) {
            RedEventBroker.using(eventBroker).send(RobotModelEvents.COLUMN_COUNT_EXCEEDED,
                    dataProvider.getRowObject(tableContext.getRow()));
        }
    }

    private List<String> getValuesToInsert(final RedKeywordProposal proposedKeyword) {
        final List<String> values = new ArrayList<>(proposedKeyword.getArguments());
        values.add(0, proposedKeyword.getContent());
        values.remove("");
        return values;
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
