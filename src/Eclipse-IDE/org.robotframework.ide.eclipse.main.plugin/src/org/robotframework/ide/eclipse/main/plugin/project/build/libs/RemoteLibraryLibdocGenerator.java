/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;

class RemoteLibraryLibdocGenerator implements ILibdocGenerator {

    private final IFile spec;
    private final URI uri;

    RemoteLibraryLibdocGenerator(final URI uri, final IFile spec) {
        this.spec = spec;
        this.uri = uri;
    }

    @Override
    public void generateLibdoc(final RobotRuntimeEnvironment runtimeEnvironment) throws RobotEnvironmentException {
        final String libName = "Remote::" + uri;
        runtimeEnvironment.createLibdocForStdLibrary(libName, spec.getLocation().toFile());
    }

    @Override
    public String getMessage() {
        return "generating libdoc for remote library located at " + uri;
    }

}
