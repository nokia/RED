/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;

public class JavaLibraryLibdocGenerator implements ILibdocGenerator {

    private final String libName;
    private final String jarPath;
    private final IFile targetSpecFile;

    public JavaLibraryLibdocGenerator(final String libName, final String path, final IFile targetSpecFile) {
        this.libName = libName;
        this.jarPath = path;
        this.targetSpecFile = targetSpecFile;
    }

    @Override
    public void generateLibdoc(final RobotRuntimeEnvironment runtimeEnvironment,
            final EnvironmentSearchPaths additionalPaths) throws RobotEnvironmentException {
        additionalPaths.addClassPath(jarPath);
        runtimeEnvironment.createLibdocForThirdPartyLibrary(libName, jarPath, additionalPaths,
                targetSpecFile.getLocation().toFile());
    }

    @Override
    public void generateLibdocForcibly(final RobotRuntimeEnvironment runtimeEnvironment,
            final EnvironmentSearchPaths additionalPaths)
            throws RobotEnvironmentException {
        additionalPaths.addClassPath(jarPath);
        runtimeEnvironment.createLibdocForThirdPartyLibraryForcibly(libName, jarPath, additionalPaths,
                targetSpecFile.getLocation().toFile());
    }

    @Override
    public String getMessage() {
        return "generating libdoc for " + libName + " library contained in " + jarPath;
    }
}
