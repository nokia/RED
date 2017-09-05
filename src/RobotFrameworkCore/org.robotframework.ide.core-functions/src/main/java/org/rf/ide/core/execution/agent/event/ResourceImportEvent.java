/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ResourceImportEvent {

    public static ResourceImportEvent from(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("resource_import");
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);
        final URI path = Events.toFileUri((String) attributes.get("source"));
        final URI importerPath = Events.toFileUri((String) attributes.get("importer"));

        if (path == null) {
            throw new IllegalArgumentException("Resource import event has to contain path to imported resource");
        }
        return new ResourceImportEvent(path, importerPath);
    }


    private final URI path;

    private final URI importerPath;

    public ResourceImportEvent(final URI path, final URI importerPath) {
        this.path = path;
        this.importerPath = importerPath;
    }

    public URI getPath() {
        return path;
    }

    public boolean isDynamicallyImported() {
        // by RF user guide: null is provided under 'importer' key when resource is loaded
        // dynamically
        return importerPath == null;
    }

    public Optional<URI> getImporterPath() {
        return Optional.ofNullable(importerPath);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == ResourceImportEvent.class) {
            final ResourceImportEvent that = (ResourceImportEvent) obj;
            return this.path.equals(that.path) && Objects.equals(this.importerPath, that.importerPath);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.path, this.importerPath);
    }
}
