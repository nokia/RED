/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.LibdocFormat;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;

class StandardLibraryLibdocGenerator implements ILibdocGenerator {

    private final String libName;
    private final IFile targetSpecFile;

    StandardLibraryLibdocGenerator(final String libName, final IFile targetSpecFile) {
        this.libName = libName;
        this.targetSpecFile = targetSpecFile;
    }

    @Override
    public void generateLibdoc(final RobotRuntimeEnvironment runtimeEnvironment,
            final EnvironmentSearchPaths additionalPaths) throws RobotEnvironmentException {
        runtimeEnvironment.createLibdoc(libName, targetSpecFile.getLocation().toFile(), LibdocFormat.XML);
    }

    @Override
    public void generateLibdocForcibly(final RobotRuntimeEnvironment runtimeEnvironment,
            final EnvironmentSearchPaths additionalPaths) throws RobotEnvironmentException {
        runtimeEnvironment.createLibdocForcibly(libName, targetSpecFile.getLocation().toFile(), LibdocFormat.XML);
    }

    @Override
    public String getMessage() {
        return "generating libdoc for " + libName + " library";
    }

    @Override
    public IFile getTargetFile() {
        return targetSpecFile;
    }
}
