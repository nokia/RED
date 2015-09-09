/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testHelpers;

import java.util.LinkedList;
import java.util.List;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


/**
 * Records all {@link InvocationOnMock} parameters put in
 * {@link Answer#answer(InvocationOnMock)} method.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @param <T>
 *            type of answer to invoke
 */
public class AnswerRecorder<T> implements Answer<T> {

    private List<InvocationOnMock> invocations = new LinkedList<>();
    private final Answer<T> otherAnswer;


    public AnswerRecorder(final Answer<T> otherAnswer) {
        this.otherAnswer = otherAnswer;
    }


    @Override
    public T answer(InvocationOnMock invocation) throws Throwable {
        invocations.add(invocation);

        return otherAnswer.answer(invocation);
    }


    public List<InvocationOnMock> getInvocations() {
        return invocations;
    }


    public Answer<T> getOtherAnswer() {
        return otherAnswer;
    }
}
