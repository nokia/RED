/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.libs;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.environment.EnvironmentSearchPaths;
import org.rf.ide.core.environment.IRuntimeEnvironment;

interface ILibdocGenerator {

    void generateLibdoc(IRuntimeEnvironment environment, EnvironmentSearchPaths additionalPaths);

    String getMessage();

    IFile getTargetFile();

}
