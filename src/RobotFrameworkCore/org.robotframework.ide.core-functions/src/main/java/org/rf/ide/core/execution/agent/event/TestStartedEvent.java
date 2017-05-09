/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class TestStartedEvent {

    private final String name;

    private final String longName;

    public static TestStartedEvent from(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("start_test");
        final String name = (String) arguments.get(0);
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);
        final String longName = (String) attributes.get("longname");

        return new TestStartedEvent(name, longName);
    }

    public TestStartedEvent(final String name, final String longName) {
        this.name = name;
        this.longName = longName;
    }

    public String getName() {
        return name;
    }

    public String getLongName() {
        return longName;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == TestStartedEvent.class) {
            final TestStartedEvent that = (TestStartedEvent) obj;
            return this.name.equals(that.name) && this.longName.equals(that.longName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, longName);
    }
}