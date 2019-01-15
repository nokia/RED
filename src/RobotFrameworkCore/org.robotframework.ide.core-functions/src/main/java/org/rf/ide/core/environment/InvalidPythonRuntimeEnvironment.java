/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.environment;

import java.io.File;

public class InvalidPythonRuntimeEnvironment extends MissingRobotRuntimeEnvironment {

    public InvalidPythonRuntimeEnvironment(final File location) {
        super(location);
    }

    @Override
    public boolean isValidPythonInstallation() {
        return false;
    }

    @Override
    public SuiteExecutor getInterpreter() {
        return null;
    }

    @Override
    public String getPythonExecutablePath() {
        return null;
    }

}
