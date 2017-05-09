/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import java.net.URI;
import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

public final class LibraryImportEvent {

    private final String name;

    private final URI importer;

    private final URI source;

    private final List<String> args;

    public static LibraryImportEvent from(final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("library_import");
        final String libraryName = (String) arguments.get(0);
        final Map<?, ?> attributes = (Map<?, ?>) arguments.get(1);
        final String originalName = (String) attributes.get("originalname");
        final String name = Strings.isNullOrEmpty(originalName) ? libraryName : originalName;
        final URI importer = Events.toFileUri((String) attributes.get("importer"));
        final URI source = Events.toFileUri((String) attributes.get("source"));
        final List<String> args = Events.ensureListOfStrings((List<?>) attributes.get("args"));

        return new LibraryImportEvent(name, importer, source, args);
    }

    public LibraryImportEvent(final String name, final URI importer, final URI source, final List<String> args) {
        this.name = name;
        this.importer = importer;
        this.source = source;
        this.args = args;
    }

    public String getName() {
        return name;
    }

    public URI getImporter() {
        return importer;
    }

    public URI getSource() {
        return source;
    }

    public ImmutableList<String> getArguments() {
        return ImmutableList.copyOf(args);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == LibraryImportEvent.class) {
            final LibraryImportEvent that = (LibraryImportEvent) obj;
            return this.name.equals(that.name) && this.importer.equals(that.importer) && this.source.equals(that.source)
                    && this.args.equals(that.args);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, importer, source, args);
    }
}
