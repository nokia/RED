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

public final class LibraryImportEvent {

    public static LibraryImportEvent from(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("library_import");
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);

        final String originalName = (String) attributes.get("originalname");
        final URI importer = Events.toFileUri((String) attributes.get("importer"));
        final URI source = Events.toFileUri((String) attributes.get("source"));

        if (originalName == null) {
            throw new IllegalArgumentException("Library import event has to have original name and arguments provided");
        }
        return new LibraryImportEvent(originalName, importer, source);
    }


    private final String name;

    private final URI importer;

    private final URI source;

    public LibraryImportEvent(final String name, final URI importer, final URI source) {
        this.name = name;
        this.importer = importer;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public Optional<URI> getImporter() {
        return Optional.ofNullable(importer);
    }

    public Optional<URI> getSource() {
        return Optional.ofNullable(source);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == LibraryImportEvent.class) {
            final LibraryImportEvent that = (LibraryImportEvent) obj;
            return this.name.equals(that.name) && Objects.equals(this.importer, that.importer)
                    && Objects.equals(this.source, that.source);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, importer, source);
    }
}
