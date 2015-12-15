/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.viewers.StructuredContentProvider;

class LibrariesAlreadyImportedContentProvider extends StructuredContentProvider {

    @Override
    public Object[] getElements(final Object inputElement) {
        final List<LibrarySpecification> libraries = ((Settings) inputElement).getImportedLibraries();
        Collections.sort(libraries, new Comparator<LibrarySpecification>() {

            @Override
            public int compare(final LibrarySpecification spec1, final LibrarySpecification spec2) {
                return spec1.getName().compareTo(spec2.getName());
            }
        });
        return libraries.toArray();
    }

}
