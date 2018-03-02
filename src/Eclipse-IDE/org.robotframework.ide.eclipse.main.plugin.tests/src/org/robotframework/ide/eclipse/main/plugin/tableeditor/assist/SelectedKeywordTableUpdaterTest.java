/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

public class SelectedKeywordTableUpdaterTest {

    @Test
    public void valuesShouldNotBeInserted_whenArgumentsAreEmpty() throws Exception {
        final NatTableAssistantContext tableContext = new NatTableAssistantContext(0, 2);
        final IRowDataProvider<Object> dataProvider = prepareDataProvider("", "", "");
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SelectedKeywordTableUpdater updater = new SelectedKeywordTableUpdater(tableContext, dataProvider,
                eventBroker);
        final RedKeywordProposal proposedKeyword = prepareProposedKeyword("name");

        assertThat(updater.shouldInsertWithArgs(proposedKeyword, null)).isFalse();
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void valuesShouldNotBeInserted_whenArgumentsAreNotEmpty_butSucceedingColumnsHaveValues() throws Exception {
        final NatTableAssistantContext tableContext = new NatTableAssistantContext(0, 2);
        final IRowDataProvider<Object> dataProvider = prepareDataProvider("", "", "x");
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SelectedKeywordTableUpdater updater = new SelectedKeywordTableUpdater(tableContext, dataProvider,
                eventBroker);
        final RedKeywordProposal proposedKeyword = prepareProposedKeyword("name", "a1", "a2", "a3");

        assertThat(updater.shouldInsertWithArgs(proposedKeyword, null)).isFalse();
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void valuesShouldNotBeInserted_whenArgumentsAreNotEmpty_andSucceedingColumnsDoNotHaveValues_butPredicateReturnsFalse()
            throws Exception {
        final NatTableAssistantContext tableContext = new NatTableAssistantContext(0, 2);
        final IRowDataProvider<Object> dataProvider = prepareDataProvider("", "", "");
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SelectedKeywordTableUpdater updater = new SelectedKeywordTableUpdater(tableContext, dataProvider,
                eventBroker);
        final RedKeywordProposal proposedKeyword = prepareProposedKeyword("name", "a1", "a2", "a3");

        assertThat(updater.shouldInsertWithArgs(proposedKeyword, values -> values.size() < 4)).isFalse();
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void valuesShouldBeInserted_whenArgumentsAreNotEmpty_andSucceedingColumnsDoNotHaveValues_andPredicateReturnsTrue()
            throws Exception {
        final NatTableAssistantContext tableContext = new NatTableAssistantContext(0, 2);
        final IRowDataProvider<Object> dataProvider = prepareDataProvider("", "", "");
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SelectedKeywordTableUpdater updater = new SelectedKeywordTableUpdater(tableContext, dataProvider,
                eventBroker);
        final RedKeywordProposal proposedKeyword = prepareProposedKeyword("name", "a1", "a2", "a3");

        assertThat(updater.shouldInsertWithArgs(proposedKeyword, values -> values.contains("a1"))).isTrue();
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void valuesAreInsertedIntoCorrectColumns() throws Exception {
        final NatTableAssistantContext tableContext = new NatTableAssistantContext(0, 1);
        final IRowDataProvider<Object> dataProvider = prepareDataProvider("", "", "");
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SelectedKeywordTableUpdater updater = new SelectedKeywordTableUpdater(tableContext, dataProvider,
                eventBroker);
        final RedKeywordProposal proposedKeyword = prepareProposedKeyword("name", "a1", "a2");
        updater.insertCallWithArgs(proposedKeyword);

        verify(dataProvider).setDataValue(0, 1, "name");
        verify(dataProvider).setDataValue(1, 1, "a1");
        verify(dataProvider).setDataValue(2, 1, "a2");
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void emptyValuesAreNotInserted() throws Exception {
        final NatTableAssistantContext tableContext = new NatTableAssistantContext(0, 1);
        final IRowDataProvider<Object> dataProvider = prepareDataProvider("", "", "");
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SelectedKeywordTableUpdater updater = new SelectedKeywordTableUpdater(tableContext, dataProvider,
                eventBroker);
        final RedKeywordProposal proposedKeyword = prepareProposedKeyword("name", "a1", "a2", "");
        updater.insertCallWithArgs(proposedKeyword);

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
        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SelectedKeywordTableUpdater updater = new SelectedKeywordTableUpdater(tableContext, dataProvider,
                eventBroker);
        final RedKeywordProposal proposedKeyword = prepareProposedKeyword("name", "a1", "a2");
        updater.insertCallWithArgs(proposedKeyword);

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

    private static RedKeywordProposal prepareProposedKeyword(final String content, final String... args) {
        final RedKeywordProposal proposal = mock(RedKeywordProposal.class);
        when(proposal.getContent()).thenReturn(content);
        when(proposal.getArguments()).thenReturn(Arrays.asList(args));
        return proposal;
    }
}
