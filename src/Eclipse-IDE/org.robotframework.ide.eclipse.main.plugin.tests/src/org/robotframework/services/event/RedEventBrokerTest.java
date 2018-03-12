/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.services.event;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;

public class RedEventBrokerTest {

    @Test
    public void dataIsSendDirectly_whenThereAreNoAdditionalObjectsToPass() {
        final IEventBroker broker = mock(IEventBroker.class);
        final Object data = new Object();
        
        RedEventBroker.using(broker).send("topic", data);
        
        verify(broker, times(1)).send(eq("topic"), same(data));
        verifyNoMoreInteractions(broker);
    }

    @Test
    public void dataIsPostedDirectly_whenThereAreNoAdditionalObjectsToPass() {
        final IEventBroker broker = mock(IEventBroker.class);
        final Object data = new Object();

        RedEventBroker.using(broker).post("topic", data);

        verify(broker, times(1)).post(eq("topic"), same(data));
        verifyNoMoreInteractions(broker);
    }

    @Test
    public void dataIsSentInsideMap_whenThereAreAdditionalObjectsToPass_1() {
        final IEventBroker broker = mock(IEventBroker.class);
        final Object data = new Object();

        RedEventBroker.using(broker).additionallyBinding("a").to("val1").send("topic", data);

        final Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(IEventBroker.DATA, data);
        expectedData.put("a", "val1");

        verify(broker, times(1)).send(eq("topic"), eq(expectedData));
        verifyNoMoreInteractions(broker);
    }

    @Test
    public void dataIsSentInsideMap_whenThereAreAdditionalObjectsToPass_2() {
        final IEventBroker broker = mock(IEventBroker.class);
        final Object data = new Object();

        RedEventBroker.using(broker)
                .additionallyBinding("a")
                .to("val1")
                .additionallyBinding("b")
                .to("val2")
                .additionallyBinding("c")
                .to("val3")
                .send("topic", data);

        final Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(IEventBroker.DATA, data);
        expectedData.put("a", "val1");
        expectedData.put("b", "val2");
        expectedData.put("c", "val3");

        verify(broker, times(1)).send(eq("topic"), eq(expectedData));
        verifyNoMoreInteractions(broker);
    }

    @Test
    public void dataIsPostedInsideMap_whenThereAreAdditionalObjectsToPass_1() {
        final IEventBroker broker = mock(IEventBroker.class);
        final Object data = new Object();

        RedEventBroker.using(broker).additionallyBinding("a").to("val1").post("topic", data);

        final Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(IEventBroker.DATA, data);
        expectedData.put("a", "val1");

        verify(broker, times(1)).post(eq("topic"), eq(expectedData));
        verifyNoMoreInteractions(broker);
    }

    @Test
    public void dataIsPostedInsideMap_whenThereAreAdditionalObjectsToPass_2() {
        final IEventBroker broker = mock(IEventBroker.class);
        final Object data = new Object();

        RedEventBroker.using(broker)
                .additionallyBinding("a").to("val1")
                .additionallyBinding("b").to("val2")
                .additionallyBinding("c").to("val3")
                .post("topic", data);

        final Map<String, Object> expectedData = new HashMap<>();
        expectedData.put(IEventBroker.DATA, data);
        expectedData.put("a", "val1");
        expectedData.put("b", "val2");
        expectedData.put("c", "val3");

        verify(broker, times(1)).post(eq("topic"), eq(expectedData));
        verifyNoMoreInteractions(broker);
    }
}
