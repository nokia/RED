/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.mockeclipse;

import org.eclipse.core.runtime.IProgressMonitor;


/**
 * @author Michal Anglart
 *
 */
public class ProgressMonitorMock implements IProgressMonitor {

    private Runnable performOnBegin;

    private boolean done;

    private boolean isCancelled;

    private int workToBeDone;

    private int worked;


    @Override
    public void beginTask(final String name, final int totalWork) {
        this.workToBeDone = totalWork;
        if (performOnBegin != null) {
            performOnBegin.run();
        }
    }

    public void performWhenTaskBegins(final Runnable codeToRun) {
        this.performOnBegin = codeToRun;
    }

    @Override
    public void done() {
        this.done = true;
    }

    @Override
    public void internalWorked(final double work) {
        // nothing to do
    }

    @Override
    public boolean isCanceled() {
        return isCancelled;
    }

    @Override
    public void setCanceled(final boolean value) {
        isCancelled = value;
    }

    @Override
    public void setTaskName(final String name) {
        // nothing to do
    }

    @Override
    public void subTask(final String name) {
        // nothing to do
    }

    @Override
    public void worked(final int work) {
        worked++;
    }

    public boolean isDone() {
        return done;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public int getTotalWorkToBeDone() {
        return workToBeDone;
    }

    public int getWorkDone() {
        return worked;
    }
}
