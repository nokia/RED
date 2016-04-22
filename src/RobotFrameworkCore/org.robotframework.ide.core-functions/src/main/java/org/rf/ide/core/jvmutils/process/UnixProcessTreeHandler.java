/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.jvmutils.process;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.executor.RedSystemProperties;

public class UnixProcessTreeHandler extends AProcessTreeHandler {

    public UnixProcessTreeHandler(final OSProcessHelper helper) {
        super(helper);
    }

    @Override
    public boolean isSupported(final Process process) {
        final String procClassName = process.getClass().getName();
        return procClassName.equals("java.lang.UNIXProcess");
    }

    @Override
    public long getProcessPid(final Process process) {
        long pid = ProcessInformation.PROCESS_NOT_FOUND;
        try {
            final Field f = process.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            pid = f.getLong(process);
            f.setAccessible(false);
        } catch (Exception e) {
        }

        return pid;
    }

    @Override
    public List<String> getChildPidsCommand(final long processPid) {
        return Arrays.asList("ps", "--ppid", "" + processPid, "-o", "%U,%p,%P", "--no-header");
    }

    @Override
    public List<String> getKillProcessCommand(final ProcessInformation procInformation) {
        return Arrays.asList("kill", "-9", "" + procInformation.pid());
    }

    @Override
    public List<String> getKillProcessTreeCommand(final ProcessInformation procInformation) {
        return Arrays.asList("kill", "-9", "" + procInformation.pid());
    }

    @Override
    public boolean isSupportedOS() {
        return !RedSystemProperties.isWindowsPlatform();
    }
}
