/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.services.event;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.core.services.events.IEventBroker;

import com.google.common.base.Preconditions;

public class RedEventBroker {

    public static interface EventBroadcastingStepOne {

        EventBroadcastingStepTwo additionallyBinding(String additionalDataKey);

        <T> void send(String topic, T data);

        <T> void post(String topic, T data);
    }

    public static interface EventBroadcastingStepTwo {

        <T> EventBroadcastingStepOne to(T additionalData);
    }

    private RedEventBroker() {
        // hiding constructor
    }

    public static EventBroadcastingStepOne using(final IEventBroker broker) {
        return new BuildingSteps(broker);
    }

    private static class BuildingSteps implements EventBroadcastingStepOne, EventBroadcastingStepTwo {

        private final IEventBroker broker;

        private final Map<String, Object> eventData = new HashMap<>();

        private String additionalDataKey;

        public BuildingSteps(final IEventBroker broker) {
            Preconditions.checkNotNull(broker);
            this.broker = broker;
        }

        @Override
        public EventBroadcastingStepTwo additionallyBinding(final String additionalDataKey) {
            Preconditions.checkNotNull(additionalDataKey);
            this.additionalDataKey = additionalDataKey;
            return this;
        }

        @Override
        public <T> EventBroadcastingStepOne to(final T additionalData) {
            eventData.put(additionalDataKey, additionalData);
            additionalDataKey = null;
            return this;
        }


        @Override
        public <T> void send(final String topic, final T data) {
            if (eventData.isEmpty()) {
                broker.send(topic, data);
            } else {
                eventData.put(IEventBroker.DATA, data);
                broker.send(topic, eventData);
            }
        }

        @Override
        public <T> void post(final String topic, final T data) {
            if (eventData.isEmpty()) {
                broker.post(topic, data);
            } else {
                eventData.put(IEventBroker.DATA, data);
                broker.post(topic, eventData);
            }
        }
    }
}
