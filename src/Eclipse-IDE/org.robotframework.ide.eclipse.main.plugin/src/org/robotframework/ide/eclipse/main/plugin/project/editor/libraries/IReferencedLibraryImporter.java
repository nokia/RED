/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;

public interface IReferencedLibraryImporter {

    Collection<ReferencedLibrary> importPythonLib(IRuntimeEnvironment environment, IProject project,
            RobotProjectConfig config, String fullLibraryPath);

    Collection<ReferencedLibrary> importPythonLib(IRuntimeEnvironment environment, IProject project,
            RobotProjectConfig config, String fullLibraryPath, String name);

    Collection<ReferencedLibrary> importJavaLib(IRuntimeEnvironment environment, IProject project,
            RobotProjectConfig config, String fullLibraryPath);

    Collection<ReferencedLibrary> importJavaLib(IRuntimeEnvironment environment, IProject project,
            RobotProjectConfig config, String fullLibraryPath, String name);
}
