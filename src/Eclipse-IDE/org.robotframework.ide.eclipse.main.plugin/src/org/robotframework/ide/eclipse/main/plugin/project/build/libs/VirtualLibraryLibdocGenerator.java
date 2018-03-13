/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;

import com.google.common.io.Files;

/**
 * @author Michal Anglart
 */
class VirtualLibraryLibdocGenerator implements ILibdocGenerator {

    private final IPath path;

    private final IFile targetSpecFile;

    VirtualLibraryLibdocGenerator(final IPath libPath, final IFile targetSpecFile) {
        this.path = libPath;
        this.targetSpecFile = targetSpecFile;
    }

    @Override
    public void generateLibdoc(final RobotRuntimeEnvironment runtimeEnvironment,
            final EnvironmentSearchPaths additionalPaths) throws RobotEnvironmentException {
        if (path.isAbsolute()) {
            // we only copy virtual libraries from outside of workspace; those contained inside will
            // be read directly
            try {
                Files.copy(path.toFile(), targetSpecFile.getLocation().toFile());
            } catch (final IOException e) {
                throw new RobotEnvironmentException("Unable to create link to " + path.toOSString() + " libspec file",
                        e);
            }
        }
    }

    @Override
    public void generateLibdocForcibly(final RobotRuntimeEnvironment runtimeEnvironment,
            final EnvironmentSearchPaths additionalPaths) {
        try {
            generateLibdoc(runtimeEnvironment, additionalPaths);
        } catch (final RobotEnvironmentException e) {
            // nothing to do
        }
    }

    @Override
    public String getMessage() {
        return "linking libdoc for workspace-external virtual library located at '" + path.toOSString() + "'";
    }

    @Override
    public IFile getTargetFile() {
        return targetSpecFile;
    }
}
