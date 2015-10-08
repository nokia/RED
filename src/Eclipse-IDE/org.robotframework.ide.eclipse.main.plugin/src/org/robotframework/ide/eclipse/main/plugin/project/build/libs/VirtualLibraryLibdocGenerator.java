/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;

import com.google.common.io.Files;

/**
 * @author Michal Anglart
 *
 */
public class VirtualLibraryLibdocGenerator implements ILibdocGenerator {

    private final IPath path;

    private final IFile specFile;

    public VirtualLibraryLibdocGenerator(final IPath libPath, final IFile specFile) {
        this.path = libPath;
        this.specFile = specFile;
    }

    @Override
    public void generateLibdoc(final RobotRuntimeEnvironment runtimeEnvironment) throws RobotEnvironmentException {
        try {
            Files.copy(path.toFile(), specFile.getLocation().toFile());
        } catch (final IOException e) {
            throw new RobotEnvironmentException("Unable to create link to " + path.toOSString() + " libspec file", e);
        }
    }

    @Override
    public String getMessage() {
        return "linking libdoc for workspace-external virutal library located at " + path.toOSString();
    }

}
