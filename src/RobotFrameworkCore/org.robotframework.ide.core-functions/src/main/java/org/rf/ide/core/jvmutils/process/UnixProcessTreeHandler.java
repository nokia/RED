/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.jvmutils.process;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.RedSystemProperties;

public class UnixProcessTreeHandler extends AProcessTreeHandler {

    @Override
    public boolean isSupportedOS() {
        return !RedSystemProperties.isWindowsPlatform();
    }

    @Override
    public boolean isSupported(final Process process) {
        final String procClassName = process.getClass().getName();
        return procClassName.equals("java.lang.UNIXProcess");
    }

    @Override
    public long getProcessPid(final Process process) {
        try {
            final Field f = process.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            try {
                return f.getLong(process);
            } finally {
                f.setAccessible(false);
            }
        } catch (final Exception e) {
        }
        return ProcessInformation.PROCESS_NOT_FOUND;
    }

    @Override
    protected List<String> getChildPidsCommand(final long processPid) {
        return Arrays.asList("ps", "--ppid", Long.toString(processPid), "-o", "%U,%p,%P", "--no-header");
    }

    @Override
    protected List<String> getKillProcessCommand(final ProcessInformation procInformation) {
        return Arrays.asList("kill", "-9", Long.toString(procInformation.pid()));
    }

    @Override
    protected List<String> getKillProcessTreeCommand(final ProcessInformation procInformation) {
        return Arrays.asList("kill", "-9", Long.toString(procInformation.pid()));
    }

    @Override
    protected List<String> getInterruptProcessCommand(final ProcessInformation procInformation,
            final String pythonExecutablePath) {
        return Arrays.asList("kill", "-2", Long.toString(procInformation.pid()));
    }

    @Override
    protected boolean isInterruptionOutputValid(final int returnCode, final List<String> output) {
        return OSProcessHelper.SUCCESS == returnCode;
    }
}
