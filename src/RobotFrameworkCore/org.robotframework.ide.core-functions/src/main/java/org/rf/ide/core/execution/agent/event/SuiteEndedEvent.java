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

public final class SuiteEndedEvent {

    private final String name;

    private final int elapsedTime;

    private final Status suiteStatus;

    private final String errorMessage;

    public static SuiteEndedEvent from(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("end_suite");
        final String name = (String) arguments.get(0);
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);
        final int elapsedTime = (Integer) attributes.get("elapsedtime");
        final String errorMessage = (String) attributes.get("message");
        final Status suiteStatus = Status.valueOf((String) attributes.get("status"));

        return new SuiteEndedEvent(name, elapsedTime, suiteStatus, errorMessage);
    }

    public SuiteEndedEvent(final String name, final int elapsedTime, final Status suiteStatus,
            final String errorMessage) {
        this.name = name;
        this.elapsedTime = elapsedTime;
        this.suiteStatus = suiteStatus;
        this.errorMessage = errorMessage;
    }

    public String getName() {
        return name;
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
        if (obj != null && obj.getClass() == SuiteEndedEvent.class) {
            final SuiteEndedEvent that = (SuiteEndedEvent) obj;
            return this.name.equals(that.name) && this.elapsedTime == that.elapsedTime
                    && this.suiteStatus == that.suiteStatus && this.errorMessage.equals(that.errorMessage);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, elapsedTime, suiteStatus, errorMessage);
    }
}