/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.services.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.osgi.service.event.Event;

public class EventsTest {

    @Test
    public void thereIsNoData_whenKeyIsNotDefinedInEvent() {
        final Map<String, Object> data = new HashMap<>();
        data.put("a", "val1");
        data.put("b", "val2");
        final Event event = new Event("topic", data);

        assertThat(Events.get(event, "key", Object.class)).isNull();
    }

    @Test
    public void thereIsNoData_whenDataUnderKeyIsOfUnexpectedType() {
        final Map<String, Object> data = new HashMap<>();
        data.put("key", new A());
        final Event event = new Event("topic", data);

        assertThat(Events.get(event, "key", C.class)).isNull();
    }

    @Test
    public void thereIsAData_whenDataUnderKeyIsOfSameType() {
        final Map<String, Object> data = new HashMap<>();
        final A a = new A();
        data.put("key", a);
        final Event event = new Event("topic", data);

        assertThat(Events.get(event, "key", A.class)).isSameAs(a);
    }

    @Test
    public void thereIsAData_whenDataUnderKeyIsSubtypeOfGiven() {
        final Map<String, Object> data = new HashMap<>();
        final B b = new B();
        data.put("key", b);
        final Event event = new Event("topic", data);

        assertThat(Events.get(event, "key", A.class)).isSameAs(b);
    }
    
    private static class A {}
    private static class B extends A {}
    private static class C {}
}
