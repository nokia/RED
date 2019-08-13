/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.rf.ide.core.environment.EnvironmentSearchPaths;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.libraries.LibrarySpecification.LibdocFormat;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

class PythonLibraryLibdocGenerator implements ILibdocGenerator {

    static String buildNameWithArgs(final String nameOrPath, final List<String> arguments) {
        return arguments.isEmpty() ? nameOrPath : nameOrPath + "::" + String.join("::", arguments);
    }

    static String buildNameWithArgsDescription(final String nameOrPath, final List<String> arguments) {
        return arguments.isEmpty() ? nameOrPath : nameOrPath + " [" + String.join(", ", arguments) + "]";
    }

    private final String libName;

    private final List<String> arguments;

    private final String libPath;

    private final IFile targetSpecFile;

    private final LibdocFormat format;

    PythonLibraryLibdocGenerator(final String libName, final List<String> arguments, final String path,
            final IFile targetSpecFile, final LibdocFormat format) {
        this.libName = libName;
        this.arguments = arguments;
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
        final String pathOrNameWithArgs = buildNameWithArgs(getLibPathOrName(), arguments);
        final File outputFile = targetSpecFile.getLocation().toFile();
        if (RedPlugin.getDefault().getPreferences().isPythonLibrariesLibdocGenerationInSeparateProcessEnabled()) {
            environment.createLibdocInSeparateProcess(pathOrNameWithArgs, outputFile, format, additionalPaths);
        } else {
            environment.createLibdoc(pathOrNameWithArgs, outputFile, format, additionalPaths);
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
        final String pathOrNameWithArgs = buildNameWithArgsDescription(getLibPathOrName(), arguments);
        return "generating libdoc for '" + pathOrNameWithArgs + "' library located at '" + libPath + "'";
    }

    @Override
    public IFile getTargetFile() {
        return targetSpecFile;
    }
}
