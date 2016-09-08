/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author wypych
 */
public class IJobGroup implements IJobChangeListener {

    private final Object LOCK = new Object();

    private final Deque<Job> jobs = new LinkedBlockingDeque<>();

    private final AtomicReference<Thread> waitThread = new AtomicReference<>(new Thread());

    public void addJob(final Job job) {
        synchronized (job) {
            job.addJobChangeListener(this);
            this.jobs.offer(job);
        }
    }

    @Override
    public void aboutToRun(IJobChangeEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void awake(IJobChangeEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void done(IJobChangeEvent event) {
        // TODO Auto-generated method stub
        jobs.remove(event.getJob());
    }

    @Override
    public void running(IJobChangeEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void scheduled(IJobChangeEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void sleeping(IJobChangeEvent event) {
        // TODO Auto-generated method stub

    }

    public void join(final long timeout, final TimeUnit timeUnit) throws InterruptedException {
        synchronized (LOCK) {
            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {
                    while (!jobs.isEmpty()) {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {

                        }
                    }
                }
            });
            waitThread.set(t);
            t.start();
        }

        waitThread.get().join(timeUnit.toMillis(timeout));
    }
}
