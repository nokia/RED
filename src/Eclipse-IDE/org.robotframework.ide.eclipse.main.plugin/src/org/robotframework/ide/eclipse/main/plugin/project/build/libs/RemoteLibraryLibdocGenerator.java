/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import java.net.URI;

import org.eclipse.core.resources.IFile;

class RemoteLibraryLibdocGenerator extends StandardLibraryLibdocGenerator {

    private final URI uri;

    RemoteLibraryLibdocGenerator(final URI uri, final IFile targetSpecFile) {
        super(targetSpecFile);
        this.uri = uri;
    }

    @Override
    protected String getLibraryName() {
        return "Remote::" + uri;
    }
}
