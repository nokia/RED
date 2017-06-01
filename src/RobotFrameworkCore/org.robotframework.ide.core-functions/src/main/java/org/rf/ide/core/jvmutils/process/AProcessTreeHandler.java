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
            final Queue<String> collectedOutput = new ConcurrentLinkedQueue<>();
            final int returnCode = getHelper().execCommandAndCollectOutput(getChildPidsCommand(processPid),
                    collectedOutput);

            if (returnCode == OSProcessHelper.SUCCESS) {
                for (final String line : collectedOutput) {
                    if (!line.isEmpty()) {
                        final String[] columns = line.split(",");
                        if (columns.length > 1) {
                            try {
                                final long childProcessPid = Long.parseLong(columns[1]);
                                if (childProcessPid != processPid) {
                                    childPids.add(childProcessPid);
                                }
                            } catch (final NumberFormatException nfe) {
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
                        + String.join("\n", collectedOutput));
            }
        } catch (final Exception e) {
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
                        + ", exitCode=" + returnCode + ", output=" + String.join("\n", collectedOutput));
            }
        } catch (final Exception e) {
            e.printStackTrace();
            throw new ProcessKillException(e);
        }
    }

    protected OSProcessHelper getHelper() {
        return this.helper;
    }
}
