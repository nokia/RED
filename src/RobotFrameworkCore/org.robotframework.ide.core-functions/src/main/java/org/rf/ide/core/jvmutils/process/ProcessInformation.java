/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.jvmutils.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProcessInformation {

    public static final long PROCESS_NOT_FOUND = -1;

    private final long processId;

    private Optional<ProcessInformation> parent = Optional.empty();
    private final List<ProcessInformation> childProcesses = new ArrayList<>(0);

    private final Optional<IProcessTreeHandler> handler;

    public static ProcessInformation unknown() {
        return new ProcessInformation(PROCESS_NOT_FOUND, null);
    }

    public static ProcessInformation of(final long processId, final IProcessTreeHandler handler) {
        return new ProcessInformation(processId, handler);
    }

    private ProcessInformation(final long processId, final IProcessTreeHandler handler) {
        this.processId = processId;
        this.handler = Optional.ofNullable(handler);
    }

    public long pid() {
        return processId;
    }

    public boolean isFound() {
        return PROCESS_NOT_FOUND != processId;
    }

    void setParent(final ProcessInformation parent) {
        this.parent = Optional.of(parent);
    }

    public Optional<ProcessInformation> getParent() {
        return parent;
    }

    void addChildProcess(final ProcessInformation childProcess) {
        childProcess.setParent(this);
        childProcesses.add(childProcess);
    }

    public List<ProcessInformation> getChildren() {
        return childProcesses;
    }

    public Optional<IProcessTreeHandler> findHandler() {
        if (handler.isPresent()) {
            return handler;
        } else if (parent.isPresent()) {
            return parent.get().findHandler();
        }
        return Optional.empty();
    }
}
