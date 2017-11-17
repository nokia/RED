/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;

public interface ILibraryClass {

    String getQualifiedName();

    ReferencedLibrary toReferencedLibrary(String fullLibraryPath);
}
