/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.environment.EnvironmentSearchPaths;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.libraries.LibrarySpecification.LibdocFormat;

class StandardLibraryLibdocGenerator implements ILibdocGenerator {

    private final String libName;
    private final IFile targetSpecFile;
    private final LibdocFormat format;

    StandardLibraryLibdocGenerator(final String libName, final IFile targetSpecFile, final LibdocFormat format) {
        this.libName = libName;
        this.targetSpecFile = targetSpecFile;
        this.format = format;
    }

    @Override
    public void generateLibdoc(final IRuntimeEnvironment environment, final EnvironmentSearchPaths additionalPaths) {
        // no need to pass additional paths, because standard Robot libraries are already in sys.path
        environment.createLibdoc(libName, targetSpecFile.getLocation().toFile(), format, new EnvironmentSearchPaths());
    }

    @Override
    public String getMessage() {
        return "generating libdoc for '" + libName + "' library";
    }

    @Override
    public IFile getTargetFile() {
        return targetSpecFile;
    }
}
