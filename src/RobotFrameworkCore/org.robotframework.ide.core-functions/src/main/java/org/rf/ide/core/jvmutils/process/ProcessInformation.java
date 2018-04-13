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

    private final long processPid;

    private Optional<ProcessInformation> parent = Optional.empty();
    private final List<ProcessInformation> childProcesses = new ArrayList<>(0);

    private Optional<IProcessTreeHandler> handler = Optional.empty();

    public ProcessInformation(final long processPid) {
        this.processPid = processPid;
    }

    public Optional<IProcessTreeHandler> findHandler() {
        if (handler.isPresent()) {
            return handler;
        } else if (parent.isPresent()) {
            return parent.get().findHandler();
        }
        return Optional.empty();
    }

    public void setHandler(final IProcessTreeHandler handler) {
        this.handler = Optional.ofNullable(handler);
    }

    public Optional<ProcessInformation> parent() {
        return parent;
    }

    public long pid() {
        return processPid;
    }

    void setParent(final ProcessInformation parent) {
        this.parent = Optional.of(parent);
    }

    void addChildProcess(final ProcessInformation childProcess) {
        childProcess.setParent(this);
        childProcesses.add(childProcess);
    }

    public List<ProcessInformation> childs() {
        return childProcesses;
    }

    public boolean isFound() {
        return PROCESS_NOT_FOUND != processPid;
    }
}
