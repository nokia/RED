/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.jvmutils.process;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Optional;

import org.rf.ide.core.jvmutils.process.IProcessTreeHandler.ProcessInterruptException;
import org.rf.ide.core.jvmutils.process.IProcessTreeHandler.ProcessKillException;

import com.google.common.annotations.VisibleForTesting;

public class OSProcessHelper {

    public static final int SUCCESS = 0;

    private final List<IProcessTreeHandler> osPidCheckers;

    public OSProcessHelper() {
        this.osPidCheckers = newArrayList(new WindowsProcessTreeHandler(), new UnixProcessTreeHandler());
    }

    public Optional<IProcessTreeHandler> getInstanceByOS() {
        return osPidCheckers.stream().filter(IProcessTreeHandler::isSupportedOS).findFirst();
    }

    public ProcessInformation getProcessTreeInformations(final Process process) {
        final Optional<IProcessTreeHandler> processTreeProv = findProcessTreeProviderFor(process);
        if (processTreeProv.isPresent()) {
            final IProcessTreeHandler provider = processTreeProv.get();
            return fillProcessTree(provider, provider.getProcessPid(process));
        }
        return new ProcessInformation(ProcessInformation.PROCESS_NOT_FOUND);
    }

    public void interruptProcess(final Process process, final String pythonExecutablePath)
            throws ProcessHelperException {
        final ProcessInformation procInfo = getProcessTreeInformations(process);
        if (procInfo.isFound()) {
            interruptProcess(procInfo, pythonExecutablePath);
        } else {
            throw new ProcessHelperException("Couldn't find PID!");
        }
    }

    public void interruptProcess(final ProcessInformation procInfo, final String pythonExecutablePath)
            throws ProcessHelperException {
        final Optional<IProcessTreeHandler> osHandler = procInfo.findHandler();
        if (osHandler.isPresent()) {
            try {
                osHandler.get().interruptProcess(procInfo, pythonExecutablePath);
            } catch (final ProcessInterruptException e) {
                throw new ProcessHelperException(e);
            }
        } else {
            throw new ProcessHelperException("Couldn't find suiteable handler!");
        }
    }

    public void destroyProcessTree(final Process process) throws ProcessHelperException {
        final ProcessInformation procInfo = getProcessTreeInformations(process);
        if (procInfo.isFound()) {
            destroyProcessTree(procInfo);
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
    ProcessInformation fillProcessTree(final IProcessTreeHandler provider, final long pid) {
        final ProcessInformation process = new ProcessInformation(pid);
        process.setHandler(provider);

        final List<Long> childPids = provider.getChildPids(pid);
        for (final Long childPid : childPids) {
            process.addChildProcess(fillProcessTree(provider, childPid));
        }
        return process;
    }

    @VisibleForTesting
    Optional<IProcessTreeHandler> findProcessTreeProviderFor(final Process process) {
        return osPidCheckers.stream().filter(prov -> prov.isSupported(process)).findFirst();
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
