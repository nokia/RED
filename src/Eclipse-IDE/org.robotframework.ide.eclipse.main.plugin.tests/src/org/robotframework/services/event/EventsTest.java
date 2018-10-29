/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.services.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.osgi.service.event.Event;

import com.google.common.collect.ImmutableMap;

public class EventsTest {

    @Test
    public void thereIsNoData_whenKeyIsNotDefinedInEvent() {
        final Event event = new Event("topic", ImmutableMap.of("a", "val1", "b", "val2"));

        assertThat(Events.get(event, "key", Object.class)).isNull();
    }

    @Test
    public void thereIsNoData_whenDataUnderKeyIsOfUnexpectedType() {
        final Event event = new Event("topic", ImmutableMap.of("key", new AType()));

        assertThat(Events.get(event, "key", CType.class)).isNull();
    }

    @Test
    public void thereIsAData_whenDataUnderKeyIsOfSameType() {
        final AType a = new AType();
        final Event event = new Event("topic", ImmutableMap.of("key", a));

        assertThat(Events.get(event, "key", AType.class)).isSameAs(a);
    }

    @Test
    public void thereIsAData_whenDataUnderKeyIsSubtypeOfGiven() {
        final BType b = new BType();
        final Event event = new Event("topic", ImmutableMap.of("key", b));

        assertThat(Events.get(event, "key", AType.class)).isSameAs(b);
    }

    private static class AType {
    }

    private static class BType extends AType {
    }

    private static class CType {
    }
}
