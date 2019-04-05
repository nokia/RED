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
        final String additionalPath = (libPath.toLowerCase().endsWith(".jar") || libPath.toLowerCase().endsWith(".zip"))
                ? libPath
                : extractLibParent();
        additionalPaths.addPythonPath(additionalPath);
        final String libPathOrName = getLibPathOrName();
        final File outputFile = targetSpecFile.getLocation().toFile();
        if (RedPlugin.getDefault().getPreferences().isPythonLibrariesLibdocGenerationInSeparateProcessEnabled()) {
            environment.createLibdocInSeparateProcess(libPathOrName, outputFile, format, additionalPaths);
        } else {
            environment.createLibdoc(libPathOrName, outputFile, format, additionalPaths);
        }
    }

    private String getLibPathOrName() {
        if (libPath.toLowerCase().endsWith(".jar") || libPath.toLowerCase().endsWith(".zip")) {
            return libName;
        }
        if (libName.contains(".")) {
            if (!libName.toLowerCase().endsWith(".py")
                    || (!libPath.toLowerCase().endsWith(libName.toLowerCase().replaceAll("\\.", "\\\\") + ".py")
                            && !libPath.toLowerCase().endsWith(libName.toLowerCase().replaceAll("\\.", "/") + ".py"))) {
                return libName;
            }
        }
        if (libPath.toLowerCase().endsWith("__init__.py")) {
            return libPath.substring(0, libPath.length() - 12);
        }
        return libPath;
    }

    private String extractLibParent() {
        // e.g. libPath=Project1/Plib/ca libName=Plib.ca.ab => parent=Project1
        String parent = new Path(libPath).removeLastSegments(1).toPortableString();
        final String[] libNameElements = libName.split("\\.");
        if (libNameElements.length > 1) {
            for (int i = libNameElements.length - 1; i >= 0; i--) {
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
