/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.rf.ide.core.RedURI;

@FunctionalInterface
public interface ILibraryStructureBuilder {

    Collection<ILibraryClass> provideEntriesFromFile(URI uri);

    default Collection<ILibraryClass> provideEntriesFromFile(final File file,
            final Predicate<ILibraryClass> shouldInclude) throws URISyntaxException {
        final URI uri = RedURI.fromString(file.getAbsolutePath());
        final Collection<ILibraryClass> entriesFromFile = provideEntriesFromFile(uri);
        return entriesFromFile.stream().filter(shouldInclude).collect(Collectors.toList());
    }
}
