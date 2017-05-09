/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import java.net.URI;
import java.util.List;
import java.util.Map;

public final class ResourceImportEvent {

    private final URI path;

    public static ResourceImportEvent from(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("resource_import");
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);
        final URI path = Events.toFileUri((String) attributes.get("source"));

        return new ResourceImportEvent(path);
    }

    public ResourceImportEvent(final URI path) {
        this.path = path;
    }

    public URI getPath() {
        return path;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == ResourceImportEvent.class) {
            final ResourceImportEvent that = (ResourceImportEvent) obj;
            return this.path.equals(that.path);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
