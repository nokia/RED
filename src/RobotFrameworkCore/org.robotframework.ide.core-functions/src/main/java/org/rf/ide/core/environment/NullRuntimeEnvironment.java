/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.environment;

public class NullRuntimeEnvironment extends InvalidPythonRuntimeEnvironment {

    public NullRuntimeEnvironment() {
        super(null);
    }

    @Override
    public boolean isNullEnvironment() {
        return true;
    }

}
