/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public enum SuiteExecutor {

    Python("python"),
    Python2("python2"),
    Python3("python3"),
    Jython("jython"),
    IronPython("ipy"),
    IronPython64("ipy64"),
    PyPy("pypy");

    private String fileName;

    private SuiteExecutor(final String fileName) {
        this.fileName = fileName;
    }

    public static SuiteExecutor fromName(final String name) {
        return SuiteExecutor.valueOf(name);
    }

    public static Optional<SuiteExecutor> fromLocation(final File location) {
        if (!location.isFile()) {
            return Optional.empty();
        }
        return EnumSet.allOf(SuiteExecutor.class)
                .stream()
                .filter(executor -> location.getName().equals(executor.executableName()))
                .findFirst();
    }

    public static List<String> allExecutorNames() {
        return EnumSet.allOf(SuiteExecutor.class).stream().map(SuiteExecutor::name).collect(toList());
    }

    public String executableName() {
        return RedSystemProperties.isWindowsPlatform() ? fileName + ".exe" : fileName;
    }
}
