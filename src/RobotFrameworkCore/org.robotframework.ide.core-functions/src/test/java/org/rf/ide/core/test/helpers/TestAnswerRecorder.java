/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.test.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.ThrowsExceptionClass;
import org.mockito.invocation.InvocationOnMock;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see AnswerRecorder
 */
@SuppressWarnings("PMD.MethodNamingConventions")
public class TestAnswerRecorder {

    @Test
    public void test_registeringParameters_andThrowsIllegalArgumentException() {
        // prepare
        final ThrowsExceptionClass otherAnswer = new ThrowsExceptionClass(
                IllegalArgumentException.class);
        final AnswerRecorder<Object> answer = new AnswerRecorder<>(otherAnswer);
        final TestHelper helper = mock(TestHelper.class);

        final String name = "name";
        final String surename = "surename";
        when(helper.setId(name, surename)).then(answer);

        // execute
        try {
            helper.setId(name, surename);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (final IllegalArgumentException iae) {
        }

        // verify
        assertThat(answer.getOtherAnswer()).isEqualTo(otherAnswer);
        assertThat(answer.getInvocations()).hasSize(1);
        final InvocationOnMock invocationOnMock = answer.getInvocations().get(0);
        assertThat(invocationOnMock).isNotNull();
        assertThat(invocationOnMock.getMock()).isEqualTo(helper);
        assertThat(invocationOnMock.getArguments()).hasSize(2);
        assertThat(invocationOnMock.getArgument(0)).isEqualTo(name);
        assertThat(invocationOnMock.getArgument(1)).isEqualTo(surename);
    }


    @Test
    public void test_registeringParameters_andCallRealMethod() {
        // prepare
        final AnswerRecorder<Object> answer = new AnswerRecorder<>(
                Mockito.CALLS_REAL_METHODS);
        final TestHelper helper = mock(TestHelper.class);

        final String name = "name";
        final String surename = "surename";
        when(helper.setId(name, surename)).then(answer);

        // execute
        final boolean result = helper.setId(name, surename);

        // verify
        assertThat(result).isTrue();
        assertThat(answer.getOtherAnswer()).isEqualTo(
                Mockito.CALLS_REAL_METHODS);
        assertThat(answer.getInvocations()).hasSize(1);
        final InvocationOnMock invocationOnMock = answer.getInvocations().get(0);
        assertThat(invocationOnMock).isNotNull();
        assertThat(invocationOnMock.getMock()).isEqualTo(helper);
        assertThat(invocationOnMock.getArguments()).hasSize(2);
        assertThat(invocationOnMock.getArgument(0)).isEqualTo(name);
        assertThat(invocationOnMock.getArgument(1)).isEqualTo(surename);
    }

    private class TestHelper {

        public boolean setId(final String name, final String surename) {
            return true;
        }
    }
}
