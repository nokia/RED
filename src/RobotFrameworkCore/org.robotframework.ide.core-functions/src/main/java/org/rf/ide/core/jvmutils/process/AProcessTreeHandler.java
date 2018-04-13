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

import org.rf.ide.core.executor.RobotRuntimeEnvironment;

public abstract class AProcessTreeHandler implements IProcessTreeHandler {

    @Override
    public List<Long> getChildPids(final long processPid) {
        final List<Long> childPids = new ArrayList<>(0);

        try {
            final Queue<String> collectedOutput = new ConcurrentLinkedQueue<>();
            final int returnCode = RobotRuntimeEnvironment.runExternalProcess(getChildPidsCommand(processPid),
                    collectedOutput::add);

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

    protected abstract List<String> getChildPidsCommand(final long processPid);

    @Override
    public void interruptProcess(final ProcessInformation procInformation, final String pythonExecutablePath)
            throws ProcessInterruptException {
        final List<String> command = getInterruptProcessCommand(procInformation, pythonExecutablePath);
        int returnCode = 0;
        final List<String> output = new ArrayList<>();

        try {
            returnCode = RobotRuntimeEnvironment.runExternalProcess(command, output::add);
        } catch (final Exception e) {
            throw new ProcessInterruptException("Couldn't interrupt process", e);
        }
        if (!isInterruptionOutputValid(returnCode, output)) {
            throw new ProcessInterruptException(
                    "Couldn't interrupt process, exitCode=" + returnCode + "; output:\n" + String.join("\n", output));
        }
    }

    protected abstract List<String> getInterruptProcessCommand(final ProcessInformation procInformation,
            String pythonExecutablePath) throws ProcessInterruptException;

    protected abstract boolean isInterruptionOutputValid(int returnCode, List<String> output);

    @Override
    public void killProcess(final ProcessInformation procInformation) throws ProcessKillException {
        try {
            final Queue<String> collectedOutput = new ConcurrentLinkedQueue<>();
            final int returnCode = RobotRuntimeEnvironment.runExternalProcess(getKillProcessCommand(procInformation),
                    collectedOutput::add);

            if (returnCode != OSProcessHelper.SUCCESS) {
                throw new ProcessKillException("Couldn't stop process, exitCode=" + returnCode + ", output="
                        + String.join("\n", collectedOutput));
            }
        } catch (final Exception e) {
            e.printStackTrace();
            throw new ProcessKillException(e);
        }
    }

    protected abstract List<String> getKillProcessCommand(final ProcessInformation procInformation);

    @Override
    public void killProcessTree(final ProcessInformation procInformation) throws ProcessKillException {
        try {
            final Queue<String> collectedOutput = new ConcurrentLinkedQueue<>();
            final int returnCode = RobotRuntimeEnvironment
                    .runExternalProcess(getKillProcessTreeCommand(procInformation), collectedOutput::add);

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

    protected abstract List<String> getKillProcessTreeCommand(final ProcessInformation procInformation);
}
