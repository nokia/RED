package org.robotframework.ide.core.testHelpers;

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
public class TestAnswerRecorder {

    @Test
    public void test_registeringParameters_andThrowsIllegalArgumentException() {
        // prepare
        ThrowsExceptionClass otherAnswer = new ThrowsExceptionClass(
                IllegalArgumentException.class);
        AnswerRecorder<Object> answer = new AnswerRecorder<>(otherAnswer);
        TestHelper helper = mock(TestHelper.class);

        String name = "name";
        String surename = "surename";
        when(helper.setId(name, surename)).then(answer);

        // execute
        try {
            helper.setId(name, surename);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException iae) {
        }

        // verify
        assertThat(answer.getOtherAnswer()).isEqualTo(otherAnswer);
        assertThat(answer.getInvocations().size()).isEqualTo(1);
        InvocationOnMock invocationOnMock = answer.getInvocations().get(0);
        assertThat(invocationOnMock).isNotNull();
        assertThat(invocationOnMock.getMock()).isEqualTo(helper);
        assertThat(invocationOnMock.getArguments().length).isEqualTo(2);
        assertThat(invocationOnMock.getArgumentAt(0, String.class)).isEqualTo(
                name);
        assertThat(invocationOnMock.getArgumentAt(1, String.class)).isEqualTo(
                surename);
    }


    @Test
    public void test_registeringParameters_andCallRealMethod() {
        // prepare
        AnswerRecorder<Object> answer = new AnswerRecorder<>(
                Mockito.CALLS_REAL_METHODS);
        TestHelper helper = mock(TestHelper.class);

        String name = "name";
        String surename = "surename";
        when(helper.setId(name, surename)).then(answer);

        // execute
        boolean result = helper.setId(name, surename);

        // verify
        assertThat(result).isTrue();
        assertThat(answer.getOtherAnswer()).isEqualTo(
                Mockito.CALLS_REAL_METHODS);
        assertThat(answer.getInvocations().size()).isEqualTo(1);
        InvocationOnMock invocationOnMock = answer.getInvocations().get(0);
        assertThat(invocationOnMock).isNotNull();
        assertThat(invocationOnMock.getMock()).isEqualTo(helper);
        assertThat(invocationOnMock.getArguments().length).isEqualTo(2);
        assertThat(invocationOnMock.getArgumentAt(0, String.class)).isEqualTo(
                name);
        assertThat(invocationOnMock.getArgumentAt(1, String.class)).isEqualTo(
                surename);
    }

    private class TestHelper {

        public boolean setId(String name, String surename) {
            return true;
        }
    }
}
