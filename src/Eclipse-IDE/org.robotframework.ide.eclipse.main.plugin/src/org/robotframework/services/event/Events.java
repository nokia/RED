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
