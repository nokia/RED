/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.Objects;

public final class OutputFileEvent {

    public static OutputFileEvent from(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("output_file");
        final URI path = Events.toFileUri((String) arguments.get(0));

        return new OutputFileEvent(path);
    }


    private final URI path;

    public OutputFileEvent(final URI path) {
        this.path = path;
    }

    public Optional<URI> getPath() {
        return Optional.ofNullable(path);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == OutputFileEvent.class) {
            final OutputFileEvent that = (OutputFileEvent) obj;
            return Objects.equal(this.path, that.path);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.path);
    }
}
