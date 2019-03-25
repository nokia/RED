/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.environment.EnvironmentSearchPaths;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.libraries.LibrarySpecification.LibdocFormat;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

class PythonLibraryLibdocGenerator implements ILibdocGenerator {

    private final String libName;

    private final String libPath;

    private final IFile targetSpecFile;

    private final LibdocFormat format;

    PythonLibraryLibdocGenerator(final String libName, final String path, final IFile targetSpecFile,
            final LibdocFormat format) {
        this.libName = libName;
        this.libPath = path;
        this.targetSpecFile = targetSpecFile;
        this.format = format;
    }

    @Override
    public void generateLibdoc(final IRuntimeEnvironment environment, final EnvironmentSearchPaths additionalPaths) {
        final File libFile = new File(libPath);
        if (libPath.toLowerCase().endsWith(".jar") || libPath.toLowerCase().endsWith(".zip")) {
            additionalPaths.addPythonPath(libPath);
        } else {
            final String additionalLocation = libFile.isFile() ? libFile.getParent() : extractLibParent();
            additionalPaths.addPythonPath(additionalLocation);
        }
        final File outputFile = targetSpecFile.getLocation().toFile();
        if (RedPlugin.getDefault().getPreferences().isPythonLibrariesLibdocGenerationInSeparateProcessEnabled()) {
            environment.createLibdocInSeparateProcess(libName, outputFile, format, additionalPaths);
        } else {
            environment.createLibdoc(libName, outputFile, format, additionalPaths);
        }
    }

    private String extractLibParent() {
        // e.g. libPath=Project1/Plib/ca libName=Plib.ca.ab => parent=Project1
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
        return "generating libdoc for '" + libName + "' library located at '" + libPath + "'";
    }

    @Override
    public IFile getTargetFile() {
        return targetSpecFile;
    }
}
