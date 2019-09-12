/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

@RunWith(MockitoJUnitRunner.class)
public class MultipleCellTableUpdaterTest {

    @Mock
    private IEventBroker eventBroker;

    @Test
    public void valuesShouldNotBeInserted_whenArgumentsAreEmpty() throws Exception {
        final NatTableAssistantContext tableContext = new NatTableAssistantContext(0, 2);
        final IRowDataProvider<Object> dataProvider = prepareDataProvider("", "", "");
        final MultipleCellTableUpdater updater = new MultipleCellTableUpdater(tableContext, dataProvider,
                eventBroker);

        assertThat(updater.shouldInsertMultipleCells(newArrayList("name"))).isFalse();
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void valuesShouldNotBeInserted_whenArgumentsAreNotEmpty_butSucceedingColumnsHaveValues() throws Exception {
        final NatTableAssistantContext tableContext = new NatTableAssistantContext(0, 2);
        final IRowDataProvider<Object> dataProvider = prepareDataProvider("", "", "x");
        final MultipleCellTableUpdater updater = new MultipleCellTableUpdater(tableContext, dataProvider,
                eventBroker);

        assertThat(updater.shouldInsertMultipleCells(newArrayList("name", "a1", "a2", "a3"))).isFalse();
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void valuesShouldNotBeInserted_whenArgumentsAreNotEmpty_andSucceedingColumnsDoNotHaveValues_andColumnsCountIsExceeded()
            throws Exception {
        final NatTableAssistantContext tableContext = new NatTableAssistantContext(0, 2);
        final IRowDataProvider<Object> dataProvider = prepareDataProvider("", "", "");
        final MultipleCellTableUpdater updater = new MultipleCellTableUpdater(tableContext, dataProvider,
                eventBroker);

        assertThat(updater.shouldInsertMultipleCellsWithoutColumnExceeding(newArrayList("name", "a1", "a2", "a3")))
                .isFalse();
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void valuesShouldBeInserted_whenArgumentsAreNotEmpty_andSucceedingColumnsDoNotHaveValues_andColumnsCountIsNotExceeded()
            throws Exception {
        final NatTableAssistantContext tableContext = new NatTableAssistantContext(0, 2);
        final IRowDataProvider<Object> dataProvider = prepareDataProvider("", "", "", "", "");
        final MultipleCellTableUpdater updater = new MultipleCellTableUpdater(tableContext, dataProvider,
                eventBroker);

        assertThat(updater.shouldInsertMultipleCellsWithoutColumnExceeding(newArrayList("name", "a1", "a2", "a3")))
                .isTrue();
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void valuesAreInsertedIntoCorrectColumns() throws Exception {
        final NatTableAssistantContext tableContext = new NatTableAssistantContext(0, 1);
        final IRowDataProvider<Object> dataProvider = prepareDataProvider("", "", "");
        final MultipleCellTableUpdater updater = new MultipleCellTableUpdater(tableContext, dataProvider,
                eventBroker);
        updater.insertMultipleCells(newArrayList("name", "a1", "a2"));

        verify(dataProvider).setDataValue(0, 1, "name");
        verify(dataProvider).setDataValue(1, 1, "a1");
        verify(dataProvider).setDataValue(2, 1, "a2");
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void valuesAreInsertedIntoCorrectColumnsAndEventIsSentWithCorrectRowObject_whenColumnsCountIsExceeded()
            throws Exception {
        final NatTableAssistantContext tableContext = new NatTableAssistantContext(1, 2);
        final Object rowObject = new Object();
        final IRowDataProvider<Object> dataProvider = prepareDataProvider("", "", "");
        when(dataProvider.getRowObject(2)).thenReturn(rowObject);
        final MultipleCellTableUpdater updater = new MultipleCellTableUpdater(tableContext, dataProvider,
                eventBroker);
        updater.insertMultipleCells(newArrayList("name", "a1", "a2"));

        verify(dataProvider).setDataValue(1, 2, "name");
        verify(dataProvider).setDataValue(2, 2, "a1");
        verify(dataProvider).setDataValue(3, 2, "a2");
        verify(eventBroker).send(RobotModelEvents.COLUMN_COUNT_EXCEEDED, rowObject);
    }

    private static IRowDataProvider<Object> prepareDataProvider(final String... values) {
        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getColumnCount()).thenReturn(values.length);
        for (int i = 0; i < values.length; i++) {
            when(dataProvider.getDataValue(eq(i), anyInt())).thenReturn(values[i]);
        }
        return dataProvider;
    }
}
