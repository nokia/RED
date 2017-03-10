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

    private Optional<ProcessInformation> parent = Optional.empty();

    private final long processPid;

    private final List<ProcessInformation> childProcesses = new ArrayList<>(0);

    private Optional<IProcessTreeHandler> handler = Optional.empty();

    public ProcessInformation(final long processPid) {
        this.processPid = processPid;
    }

    public Optional<IProcessTreeHandler> findHandler() {
        Optional<IProcessTreeHandler> foundHandler = Optional.empty();

        if (handler.isPresent()) {
            foundHandler = handler;
        } else {
            if (parent().isPresent()) {
                foundHandler = parent().get().findHandler();
            }
        }

        return foundHandler;
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
        this.childProcesses.add(childProcess);
    }

    public List<ProcessInformation> childs() {
        return this.childProcesses;
    }

    public boolean wasFound() {
        return (PROCESS_NOT_FOUND != pid());
    }
}
