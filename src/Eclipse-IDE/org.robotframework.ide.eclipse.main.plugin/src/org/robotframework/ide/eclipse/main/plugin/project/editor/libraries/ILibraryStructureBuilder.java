/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.net.URI;
import java.util.Collection;

import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;

public interface ILibraryStructureBuilder {

    Collection<ILibraryClass> provideEntriesFromFile(final URI uri) throws RobotEnvironmentException;
}
