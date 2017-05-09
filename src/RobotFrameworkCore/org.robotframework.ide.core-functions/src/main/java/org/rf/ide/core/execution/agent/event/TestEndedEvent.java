/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.rf.ide.core.execution.agent.Status;

public final class TestEndedEvent {

    private final String name;

    private final String longName;

    private final int elapsedTime;

    private final Status suiteStatus;

    private final String errorMessage;

    public static TestEndedEvent from(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("end_test");
        final String name = (String) arguments.get(0);
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);
        final String longName = (String) attributes.get("longname");
        final int elapsedTime = (Integer) attributes.get("elapsedtime");
        final String errorMessage = (String) attributes.get("message");
        final Status testStatus = Status.valueOf((String) attributes.get("status"));

        return new TestEndedEvent(name, longName, elapsedTime, testStatus, errorMessage);
    }

    public TestEndedEvent(final String name, final String longName, final int elapsedTime, final Status suiteStatus,
            final String errorMessage) {
        this.name = name;
        this.longName = longName;
        this.elapsedTime = elapsedTime;
        this.suiteStatus = suiteStatus;
        this.errorMessage = errorMessage;
    }

    public String getName() {
        return name;
    }

    public String getLongName() {
        return longName;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public Status getStatus() {
        return suiteStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == TestEndedEvent.class) {
            final TestEndedEvent that = (TestEndedEvent) obj;
            return this.name.equals(that.name) && this.longName.equals(that.longName)
                    && this.elapsedTime == that.elapsedTime && this.suiteStatus == that.suiteStatus
                    && this.errorMessage.equals(that.errorMessage);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, longName, elapsedTime, suiteStatus, errorMessage);
    }
}