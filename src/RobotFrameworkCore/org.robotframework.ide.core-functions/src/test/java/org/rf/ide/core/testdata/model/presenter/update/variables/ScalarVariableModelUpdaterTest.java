/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.variables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.rf.ide.core.test.helpers.AnswerRecorder;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class ScalarVariableModelUpdaterTest {

    @Test
    public void test_ableToHandle_scalarVariable_shouldReturn_TRUE() {
        assertThat(new ScalarVariableModelUpdater()
                .ableToHandle(new ScalarVariable("", RobotToken.create(""), VariableScope.GLOBAL))).isTrue();
    }

    @Test
    public void test_ableToHandle_dictionaryVariable_shouldReturn_FALSE() {
        assertThat(new ScalarVariableModelUpdater()
                .ableToHandle(new DictionaryVariable("", RobotToken.create(""), VariableScope.GLOBAL))).isFalse();
    }

    @Test
    public void test_ableToHandle_NULL_shouldReturn_FALSE() {
        assertThat(new ScalarVariableModelUpdater().ableToHandle(null)).isFalse();
    }

    @Test
    public void test_addOrSet_logicalTest() {
        // prepare
        final ScalarVariable mocked = mock(ScalarVariable.class);
        final AnswerRecorder<Object> answer = new AnswerRecorder<>(Mockito.RETURNS_DEEP_STUBS);
        final RobotToken one = RobotToken.create("ok");
        doAnswer(answer).when(mocked).addValue(any(RobotToken.class), anyInt());
        @SuppressWarnings("unchecked")
        final List<RobotToken> toks = mock(List.class);
        when(toks.size()).thenReturn(1);
        when(toks.get(0)).thenReturn(one);

        // execute
        new ScalarVariableModelUpdater().addOrSet(mocked, 1, toks);

        // verify
        InOrder order = inOrder(mocked, toks);
        order.verify(toks, times(1)).size();
        order.verify(toks, times(1)).get(0);
        order.verify(mocked, times(1)).addValue(one, 1);
        order.verifyNoMoreInteractions();
    }
}
