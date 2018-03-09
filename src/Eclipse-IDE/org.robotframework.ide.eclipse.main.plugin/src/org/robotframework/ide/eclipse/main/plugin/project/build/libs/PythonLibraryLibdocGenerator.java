/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.LibdocFormat;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;

class PythonLibraryLibdocGenerator implements ILibdocGenerator {

    private final String libName;
    private final String libPath;
    private final IFile targetSpecFile;

    PythonLibraryLibdocGenerator(final String libName, final String path, final IFile targetSpecFile) {
        this.libName = libName;
        this.libPath = path;
        this.targetSpecFile = targetSpecFile;
    }

    @Override
    public void generateLibdoc(final RobotRuntimeEnvironment runtimeEnvironment,
            final EnvironmentSearchPaths additionalPaths) throws RobotEnvironmentException {
        final File libFile = new File(libPath);
        final String additionalLocation = libFile.isFile() ? libFile.getParent() : extractLibParent();
        additionalPaths.addPythonPath(additionalLocation);
        runtimeEnvironment.createLibdoc(libName, additionalLocation, additionalPaths,
                targetSpecFile.getLocation().toFile(), LibdocFormat.XML);
    }

    @Override
    public void generateLibdocForcibly(final RobotRuntimeEnvironment runtimeEnvironment,
            final EnvironmentSearchPaths additionalPaths)
            throws RobotEnvironmentException {
        final File libFile = new File(libPath);
        final String additionalLocation = libFile.isFile() ? libFile.getParent() : extractLibParent();
        additionalPaths.addPythonPath(additionalLocation);
        runtimeEnvironment.createLibdocForcibly(libName, additionalLocation, additionalPaths,
                targetSpecFile.getLocation().toFile(), LibdocFormat.XML);
    }

    private String extractLibParent() { //e.g. libPath=Project1/Plib/ca libName=Plib.ca.ab => parent=Project1
        String parent = libPath;
        final String[] libNameElements = libName.split("\\.");
        if (libNameElements.length > 1) {
            for (int i = libNameElements.length - 2; i >= 0; i--) {
                if (libNameElements[i].equals(new Path(parent).lastSegment())) {
                    parent = new Path(parent).removeLastSegments(1).toPortableString();
                }
            }
        }
        return parent;
    }

    @Override
    public String getMessage() {
        return "generating libdoc for " + libName + " library contained in " + libPath;
    }

    @Override
    public IFile getTargetFile() {
        return targetSpecFile;
    }
}
