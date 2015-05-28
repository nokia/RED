package org.robotframework.ide.core.executor;



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

    public abstract String executableName();

    private static String getExtension() {
        return isWindows() ? ".exe" : "";
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    }

}
