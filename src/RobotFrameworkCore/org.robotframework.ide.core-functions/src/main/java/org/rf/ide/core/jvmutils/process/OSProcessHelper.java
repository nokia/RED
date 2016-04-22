/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.jvmutils.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.rf.ide.core.executor.ILineHandler;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.jvmutils.process.IProcessTreeHandler.ProcessKillException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

public class OSProcessHelper {

    public static final int SUCCESS = 0;

    private final List<IProcessTreeHandler> osPidCheckers;

    @VisibleForTesting
    public OSProcessHelper(final List<IProcessTreeHandler> osPidCheckers) {
        this.osPidCheckers = osPidCheckers;
    }

    public OSProcessHelper() {
        this.osPidCheckers = new ArrayList<IProcessTreeHandler>(
                Arrays.asList(new WindowsProcessTreeHandler(this), new UnixProcessTreeHandler(this)));
    }

    public IProcessTreeHandler getInstanceByOS() {
        IProcessTreeHandler proc = null;

        for (IProcessTreeHandler handler : osPidCheckers) {
            if (handler.isSupportedOS()) {
                proc = handler;
                break;
            }
        }

        return proc;
    }

    public int execCommandAndCollectOutput(final List<String> command, final Collection<String> output)
            throws IOException {
        MyLineHandler handler = new MyLineHandler();
        final int exitCode = RobotRuntimeEnvironment.runExternalProcess(command, handler);
        output.addAll(handler.getCollectedOutput());

        return exitCode;
    }

    private class MyLineHandler implements ILineHandler {

        private Queue<String> lines = new ConcurrentLinkedQueue<>();

        @Override
        public void processLine(final String line) {
            this.lines.add(line);
        }

        public Queue<String> getCollectedOutput() {
            return lines;
        }
    }

    public ProcessInformation getProcessTreeInformations(final Process process) {
        ProcessInformation processInfo = new ProcessInformation(ProcessInformation.PROCESS_NOT_FOUND);

        final Optional<IProcessTreeHandler> processTreeProv = findProcessTreeProviderFor(process);
        if (processTreeProv.isPresent()) {
            final IProcessTreeHandler provider = processTreeProv.get();

            processInfo = fillProcessTree(provider, provider.getProcessPid(process));
        }

        return processInfo;
    }

    public void destroyProcessTree(final Process process) throws ProcessHelperException {
        final ProcessInformation procInfo = getProcessTreeInformations(process);

        if (procInfo.wasFound()) {
            this.destroyProcessTree(procInfo);
        } else {
            throw new ProcessHelperException("Couldn't find PID!");
        }
    }

    public void destroyProcessTree(final ProcessInformation procInfo) throws ProcessHelperException {
        final Optional<IProcessTreeHandler> osHandler = procInfo.findHandler();
        if (osHandler.isPresent()) {
            try {
                osHandler.get().killProcessTree(procInfo);
            } catch (final ProcessKillException e) {
                throw new ProcessHelperException(e);
            }
        } else {
            throw new ProcessHelperException("Couldn't find suiteable handler!");
        }
    }

    @VisibleForTesting
    protected ProcessInformation fillProcessTree(final IProcessTreeHandler provider, final long pid) {
        final ProcessInformation process = new ProcessInformation(pid);

        final List<Long> childPids = provider.getChildPids(pid);
        for (final Long childPid : childPids) {
            process.addChildProcess(fillProcessTree(provider, childPid));
        }

        return process;
    }

    @VisibleForTesting
    protected Optional<IProcessTreeHandler> findProcessTreeProviderFor(final Process process) {
        Optional<IProcessTreeHandler> provider = Optional.absent();

        for (IProcessTreeHandler prov : osPidCheckers) {
            if (prov.isSupported(process)) {
                provider = Optional.of(prov);
                break;
            }
        }

        return provider;
    }

    public static class ProcessHelperException extends Exception {

        private static final long serialVersionUID = 4817848076505721680L;

        public ProcessHelperException(final String errMsg) {
            super(errMsg);
        }

        public ProcessHelperException(final Exception e) {
            super(e);
        }
    }
}
