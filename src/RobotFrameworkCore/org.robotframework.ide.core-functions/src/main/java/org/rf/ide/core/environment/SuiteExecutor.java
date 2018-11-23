/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.environment;

import static java.util.stream.Collectors.toList;

import java.util.EnumSet;
import java.util.List;

import org.rf.ide.core.RedSystemProperties;

public enum SuiteExecutor {

    Python("python"),
    Python2("python2"),
    Python3("python3"),
    Jython("jython"),
    IronPython("ipy"),
    IronPython64("ipy64") {

        @Override
        public String exactVersion(final String version) {
            return version != null ? version.replaceAll("IronPython", "IronPython x64") : version;
        }
    },
    PyPy("pypy");

    private String fileName;

    private SuiteExecutor(final String fileName) {
        this.fileName = fileName;
    }

    public static List<String> allExecutorNames() {
        return EnumSet.allOf(SuiteExecutor.class).stream().map(SuiteExecutor::name).collect(toList());
    }

    public String executableName() {
        return RedSystemProperties.isWindowsPlatform() ? fileName + ".exe" : fileName;
    }

    public String exactVersion(final String version) {
        return version;
    }
}
