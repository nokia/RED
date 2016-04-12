/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;

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
        final String additionalLocation = libFile.isFile() ? libFile.getParent() : extractLibParent();
        runtimeEnvironment.createLibdocForPythonLibrary(libName, additionalLocation,
                targetSpecFile.getLocation().toFile());
    }

    @Override
    public void generateLibdocForcibly(final RobotRuntimeEnvironment runtimeEnvironment)
            throws RobotEnvironmentException {
        final File libFile = new File(libPath);
        final String additionalLocation = libFile.isFile() ? libFile.getParent() : extractLibParent();
        runtimeEnvironment.createLibdocForPythonLibraryForcibly(libName, additionalLocation,
                targetSpecFile.getLocation().toFile());
    }

    @Override
    public String getMessage() {
        return "generating libdoc for " + libName + " library contained in " + libPath;
    }
    
    private String extractLibParent() { //e.g. libPath=Project1/Plib/ca libName=Plib.ca.ab => parent=Project1
        String parent = libPath;
        String[] libNameElements = libName.split("\\.");
        if (libNameElements.length > 1) {
            for (int i = libNameElements.length - 2; i >= 0; i--) {
                if (libNameElements[i].equals(new Path(parent).lastSegment())) {
                    parent = new Path(parent).removeLastSegments(1).toPortableString();
                }
            }
        }
        return parent;
    }
}
