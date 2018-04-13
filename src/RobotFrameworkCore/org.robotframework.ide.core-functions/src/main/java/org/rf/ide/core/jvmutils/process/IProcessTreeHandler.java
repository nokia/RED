/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.jvmutils.process;

import java.util.List;

public interface IProcessTreeHandler {

    boolean isSupportedOS();

    boolean isSupported(final Process process);

    long getProcessPid(final Process process);

    List<Long> getChildPids(final long processPid);

    void interruptProcess(final ProcessInformation procInfo, String pythonExecutablePath)
            throws ProcessInterruptException;

    void killProcess(final ProcessInformation procInformation) throws ProcessKillException;

    void killProcessTree(final ProcessInformation procInformation) throws ProcessKillException;

    public static class ProcessInterruptException extends Exception {

        private static final long serialVersionUID = -4580432590746826729L;

        public ProcessInterruptException(final String errorMsg) {
            super(errorMsg);
        }

        public ProcessInterruptException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    public static class ProcessKillException extends Exception {

        private static final long serialVersionUID = -8030926126509950575L;

        public ProcessKillException(final Exception e) {
            super(e);
        }

        public ProcessKillException(final String errorMsg) {
            super(errorMsg);
        }
    }
}
