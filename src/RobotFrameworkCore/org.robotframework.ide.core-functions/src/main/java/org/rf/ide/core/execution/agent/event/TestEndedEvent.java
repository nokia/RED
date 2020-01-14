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

    public static TestEndedEvent from(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("end_test");
        final String name = (String) arguments.get(0);
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);
        final String originalName = (String) attributes.get("originalname");
        final String longName = (String) attributes.get("longname");
        final Integer elapsedTime = (Integer) attributes.get("elapsedtime");
        final String errorMessage = (String) attributes.get("message");
        final String testStatus = (String) attributes.get("status");

        if (longName == null || elapsedTime == null || errorMessage == null || testStatus == null) {
            throw new IllegalArgumentException(
                    "Test ended event should have long name, status, elapsed time and message attributes");
        }

        if (originalName == null) {
            // for RF < 3.2 this is always true
            return new TestEndedEvent(name, name, longName, elapsedTime, Status.valueOf(testStatus), errorMessage);
        } else {
            // name is always resolved in RF >= 3.2, originalname holds the name with parameters
            // (possibly)
            return new TestEndedEvent(originalName, name, longName, elapsedTime, Status.valueOf(testStatus),
                    errorMessage);
        }
    }


    private final String name;

    private final String resolvedName;

    private final String longName;

    private final int elapsedTime;

    private final Status testStatus;

    private final String errorMessage;


    public TestEndedEvent(final String name, final String resolvedName, final String longName, final int elapsedTime,
            final Status testStatus, final String errorMessage) {
        this.name = name;
        this.resolvedName = resolvedName;
        this.longName = longName;
        this.elapsedTime = elapsedTime;
        this.testStatus = testStatus;
        this.errorMessage = errorMessage;
    }

    public String getName() {
        return name;
    }

    public String getResolvedName() {
        return resolvedName;
    }

    public String getLongName() {
        return longName;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public Status getStatus() {
        return testStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == TestEndedEvent.class) {
            final TestEndedEvent that = (TestEndedEvent) obj;
            return this.name.equals(that.name) && Objects.equals(this.resolvedName, that.resolvedName)
                    && this.longName.equals(that.longName) && this.elapsedTime == that.elapsedTime
                    && this.testStatus == that.testStatus && this.errorMessage.equals(that.errorMessage);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, resolvedName, longName, elapsedTime, testStatus, errorMessage);
    }
}