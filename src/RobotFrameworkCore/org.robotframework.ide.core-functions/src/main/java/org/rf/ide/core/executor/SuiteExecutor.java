/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


public enum SuiteExecutor {

    Python {
        @Override
        public String executableName() {
            return "python" + getExtension();
        }
    },
    Jython {
        @Override
        public String executableName() {
            return "jython" + getExtension();
        }
    },
    IronPython {
        @Override
        public String executableName() {
            return "ipy" + getExtension();
        }
    },
    PyPy {
        @Override
        public String executableName() {
            return "pypy" + getExtension();
        }
    };

    public static SuiteExecutor fromName(final String name) {
        return SuiteExecutor.valueOf(name);
    }

    public static List<String> allExecutorNames() {
        final List<String> names = new ArrayList<>();
        for (final SuiteExecutor executor : EnumSet.allOf(SuiteExecutor.class)) {
            names.add(executor.name());
        }
        return names;
    }

    public abstract String executableName();

    private static String getExtension() {
        return RedSystemProperties.isWindowsPlatform() ? ".exe" : "";
    }
}
