/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.jvmutils.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;

public abstract class AProcessTreeHandler implements IProcessTreeHandler {

    private final OSProcessHelper helper;

    public AProcessTreeHandler(final OSProcessHelper helper) {
        this.helper = helper;
    }

    public abstract List<String> getChildPidsCommand(final long processPid);

    @Override
    public List<Long> getChildPids(final long processPid) {
        final List<Long> childPids = new ArrayList<>(0);

        try {
            final Pattern columnSeparator = Pattern.compile(",");

            final Queue<String> collectedOutput = new ConcurrentLinkedQueue<>();
            final int returnCode = getHelper().execCommandAndCollectOutput(getChildPidsCommand(processPid),
                    collectedOutput);

            if (returnCode == OSProcessHelper.SUCCESS) {
                for (String line : collectedOutput) {
                    if (line != null && !line.isEmpty()) {
                        String[] columns = columnSeparator.split(line);
                        if (columns.length > 1) {
                            try {
                                childPids.add(Long.parseLong(columns[1]));
                            } catch (NumberFormatException nfe) {
                            }
                        }
                    }
                }
            } else {
                childPids.clear();
            }
        } catch (final Exception e) {
            e.printStackTrace();
            childPids.clear();
        }

        return childPids;
    }

    public abstract List<String> getKillProcessCommand(final ProcessInformation procInformation);

    @Override
    public void killProcess(final ProcessInformation procInformation) throws ProcessKillException {
        try {
            final Queue<String> collectedOutput = new ConcurrentLinkedQueue<>();
            final int returnCode = getHelper().execCommandAndCollectOutput(getKillProcessCommand(procInformation),
                    collectedOutput);

            if (returnCode != OSProcessHelper.SUCCESS) {
                throw new ProcessKillException("Couldn't stop process, exitCode=" + returnCode + ", output="
                        + Joiner.on('\n').join(collectedOutput));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ProcessKillException(e);
        }
    }

    public abstract List<String> getKillProcessTreeCommand(final ProcessInformation procInformation);

    @Override
    public void killProcessTree(final ProcessInformation procInformation) throws ProcessKillException {
        try {
            final Queue<String> collectedOutput = new ConcurrentLinkedQueue<>();
            final int returnCode = getHelper().execCommandAndCollectOutput(getKillProcessTreeCommand(procInformation),
                    collectedOutput);

            if (returnCode == OSProcessHelper.SUCCESS) {
                final List<ProcessInformation> childs = procInformation.childs();
                for (final ProcessInformation pi : childs) {
                    killProcessTree(pi);
                }
            } else {
                throw new ProcessKillException("Couldn't stop process tree for PID=" + procInformation.pid()
                        + ", exitCode=" + returnCode + ", output=" + Joiner.on('\n').join(collectedOutput));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ProcessKillException(e);
        }
    }

    protected OSProcessHelper getHelper() {
        return this.helper;
    }
}
