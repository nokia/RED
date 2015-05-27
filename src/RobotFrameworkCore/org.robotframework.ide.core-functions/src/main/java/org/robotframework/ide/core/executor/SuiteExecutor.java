package org.robotframework.ide.core.executor;


public enum SuiteExecutor {

    PYTHON {
        @Override
        public String getName() {
            return "pybot";
        }

        @Override
        public String executableName() {
            return isWindows() ? "python.exe" : "python";
        }
    },
    JYTHON {
        @Override
        public String getName() {
            return "jybot";
        }

        @Override
        public String executableName() {
            return isWindows() ? "jython.exe" : "jython";
        }
    },
    IRONPYTHON {
        @Override
        public String getName() {
            return "ipybot";
        }

        @Override
        public String executableName() {
            return isWindows() ? "ipy.exe" : "ipy";
        }
    };

    public static SuiteExecutor fromName(final String name) {
        if ("pybot".equals(name)) {
            return PYTHON;
        } else if ("jybot".equals(name)) {
            return JYTHON;
        } else if ("ipybot".equals(name)) {
            return IRONPYTHON;
        }
        throw new IllegalArgumentException("Unrecognized executor: name");
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    }

    public abstract String getName();

    public abstract String executableName();

}
