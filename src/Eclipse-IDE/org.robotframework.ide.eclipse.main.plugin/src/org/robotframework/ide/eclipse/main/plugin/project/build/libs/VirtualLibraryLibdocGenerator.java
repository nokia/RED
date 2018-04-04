/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.LibdocFormat;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;

import com.google.common.io.Files;

/**
 * @author Michal Anglart
 */
class VirtualLibraryLibdocGenerator implements ILibdocGenerator {

    private final IPath path;
    private final IFile targetSpecFile;
    private final LibdocFormat format;

    public VirtualLibraryLibdocGenerator(final IPath libPath, final IFile targetSpecFile, final LibdocFormat format) {
        this.path = libPath;
        this.targetSpecFile = targetSpecFile;
        this.format = format;
    }

    @Override
    public void generateLibdoc(final RobotRuntimeEnvironment runtimeEnvironment,
            final EnvironmentSearchPaths additionalPaths) throws RobotEnvironmentException {
        final IPath pathToUse = format == LibdocFormat.XML ? path : path.removeFileExtension().addFileExtension("html");
        if (pathToUse.isAbsolute()) {
            // we only copy virtual libraries from outside of workspace; those contained inside will
            // be read directly
            try {
                Files.copy(pathToUse.toFile(), targetSpecFile.getLocation().toFile());
            } catch (final IOException e) {
                throw new RobotEnvironmentException(
                        "Unable to create link to '" + pathToUse.toOSString() + "' libspec file", e);
            }
        } else if (format == LibdocFormat.HTML) {
            final IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(pathToUse);
            if (member == null) {
                throw new RobotEnvironmentException(
                        "Unable to create link to '" + pathToUse.toOSString() + "' libspec file");
            }

            try (InputStream source = new FileInputStream(member.getLocation().toFile())) {
                if (targetSpecFile.exists()) {
                    targetSpecFile.setContents(source, true, false, new NullProgressMonitor());
                } else {
                    targetSpecFile.create(source, true, new NullProgressMonitor());
                }
            } catch (final IOException | CoreException e) {
                throw new RobotEnvironmentException(
                        "Unable to create link to '" + pathToUse.toOSString() + "' libdoc file", e);
            }

        }
    }

    @Override
    public void generateLibdocForcibly(final RobotRuntimeEnvironment runtimeEnvironment,
            final EnvironmentSearchPaths additionalPaths) {
        try {
            generateLibdoc(runtimeEnvironment, additionalPaths);
        } catch (final RobotEnvironmentException e) {
            // nothing to do
        }
    }

    @Override
    public String getMessage() {
        return "linking libdoc for workspace-external virtual library located at '" + path.toOSString() + "'";
    }

    @Override
    public IFile getTargetFile() {
        return targetSpecFile;
    }
}
