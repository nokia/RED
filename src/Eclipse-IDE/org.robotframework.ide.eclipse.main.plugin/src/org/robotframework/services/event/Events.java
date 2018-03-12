/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.services.event;

import org.osgi.service.event.Event;

public class Events {

    public static <T> T get(final Event event, final String key, final Class<T> expectedDataClass) {
        final Object data = event.getProperty(key);
        if (data != null && expectedDataClass.isAssignableFrom(data.getClass())) {
            return expectedDataClass.cast(data);
        }
        return null;
    }
    
}
