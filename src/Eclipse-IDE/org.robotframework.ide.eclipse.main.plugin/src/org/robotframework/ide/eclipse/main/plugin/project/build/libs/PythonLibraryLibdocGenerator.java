/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;

public class PythonLibraryLibdocGenerator implements ILibdocGenerator {

    private final String libName;
    private final String libPath;
    private final IFile targetSpecFile;

    public PythonLibraryLibdocGenerator(final String libName, final String path, final IFile targetSpecFile) {
        this.libName = libName;
        this.libPath = path;
        this.targetSpecFile = targetSpecFile;
    }

    @Override
    public void generateLibdoc(final RobotRuntimeEnvironment runtimeEnvironment) throws RobotEnvironmentException {
        final File libFile = new File(libPath);
        final String additionalLocation = libFile.isFile() ? libFile.getParent() : libPath;
        runtimeEnvironment.createLibdocForPythonLibrary(libName, additionalLocation,
                targetSpecFile.getLocation().toFile());
    }

    @Override
    public String getMessage() {
        return "generating libdoc for " + libName + " library contained in " + libPath;
    }
}
