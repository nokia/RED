/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.InOrder;
import org.rf.ide.core.testdata.model.presenter.update.variables.DictionaryVariableModelUpdater;
import org.rf.ide.core.testdata.model.presenter.update.variables.ListVariableModelUpdater;
import org.rf.ide.core.testdata.model.presenter.update.variables.ScalarVariableModelUpdater;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class VariableTableModelUpdaterTest {

    @Test
    public void test_findHandler_scalarVariable() {
        // prepare
        final AVariable var = new ScalarVariable("", new RobotToken(), VariableScope.LOCAL);

        // execute
        @SuppressWarnings("rawtypes")
        final
        IVariableTableElementOperation handler = new VariableTableModelUpdater().findHandler(var);

        // verify
        assertThat(handler).isInstanceOf(ScalarVariableModelUpdater.class);
    }

    @Test
    public void test_findHandler_listVariable() {
        // prepare
        final AVariable var = new ListVariable("", new RobotToken(), VariableScope.LOCAL);

        // execute
        @SuppressWarnings("rawtypes")
        final
        IVariableTableElementOperation handler = new VariableTableModelUpdater().findHandler(var);

        // verify
        assertThat(handler).isInstanceOf(ListVariableModelUpdater.class);
    }

    @Test
    public void test_findHandler_dictionaryVariable() {
        // prepare
        final AVariable var = new DictionaryVariable("", new RobotToken(), VariableScope.LOCAL);

        // execute
        @SuppressWarnings("rawtypes")
        final
        IVariableTableElementOperation handler = new VariableTableModelUpdater().findHandler(var);

        // verify
        assertThat(handler).isInstanceOf(DictionaryVariableModelUpdater.class);
    }

    @Test
    public void test_findHandler_null() {
        // prepare
        final AVariable var = null;

        // execute
        @SuppressWarnings("rawtypes")
        final
        IVariableTableElementOperation handler = new VariableTableModelUpdater().findHandler(var);

        // verify
        assertThat(handler).isNull();
    }

    @Test
    public void test_findHandler_unknownType() {
        // prepare
        @SuppressWarnings("serial")
        final
        AVariable var = new AVariable(null, null, null, null) {

            @Override
            public boolean isPresent() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public List<RobotToken> getElementTokens() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean removeElementToken(final int index) {
                // TODO Auto-generated method stub
                return false;
            }

        };

        // execute
        @SuppressWarnings("rawtypes")
        final
        IVariableTableElementOperation handler = new VariableTableModelUpdater().findHandler(var);

        // verify
        assertThat(handler).isNull();
    }

    @Test
    public void test_logicCheck_addOrSet() {
        // prepare
        final VariableTableModelUpdater varUpdater = new VariableTableModelUpdater();
        @SuppressWarnings("unchecked")
        final IVariableTableElementOperation<Object> mocked = mock(IVariableTableElementOperation.class);
        final AVariable var = mock(AVariable.class);
        final List<Object> toAdd = new ArrayList<>();
        when(mocked.ableToHandle(var)).thenReturn(true);
        when(mocked.convert(toAdd)).thenReturn(toAdd);

        varUpdater.getHandlers().add(0, mocked);

        // execute
        varUpdater.addOrSet(var, 0, toAdd);

        // verify
        final InOrder order = inOrder(mocked);
        order.verify(mocked, times(1)).ableToHandle(var);
        order.verify(mocked, times(1)).convert(toAdd);
        order.verify(mocked, times(1)).addOrSet(var, 0, toAdd);
        order.verifyNoMoreInteractions();
    }
}
