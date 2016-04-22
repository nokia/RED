/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.jvmutils.process;

import java.util.List;

public interface IProcessTreeHandler {

    boolean isSupported(final Process process);

    boolean isSupportedOS();

    long getProcessPid(final Process process);

    List<Long> getChildPids(final long processPid);

    void killProcess(final ProcessInformation procInformation) throws ProcessKillException;

    void killProcessTree(final ProcessInformation procInformation) throws ProcessKillException;

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
