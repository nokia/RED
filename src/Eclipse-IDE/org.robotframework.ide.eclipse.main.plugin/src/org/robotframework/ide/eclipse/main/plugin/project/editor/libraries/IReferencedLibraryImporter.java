/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;

public interface IReferencedLibraryImporter {

    Collection<ReferencedLibrary> importPythonLib(RobotRuntimeEnvironment environment, IProject project,
            RobotProjectConfig config, String fullLibraryPath);

    Collection<ReferencedLibrary> importPythonLib(RobotRuntimeEnvironment environment, IProject project,
            RobotProjectConfig config, String fullLibraryPath, String name);

    Collection<ReferencedLibrary> importJavaLib(RobotRuntimeEnvironment environment, IProject project,
            RobotProjectConfig config, String fullLibraryPath);

    Collection<ReferencedLibrary> importJavaLib(RobotRuntimeEnvironment environment, IProject project,
            RobotProjectConfig config, String fullLibraryPath, String name);
}
