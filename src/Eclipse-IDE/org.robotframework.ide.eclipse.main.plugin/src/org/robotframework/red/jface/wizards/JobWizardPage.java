/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.wizards;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ProgressBar;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.red.swt.SwtThread;


public abstract class JobWizardPage extends WizardPage {

    private Composite topLevelParent;

    private Composite progressParent;

    private boolean navigationBlocked;

    protected JobWizardPage(final String pageName, final String title, final ImageDescriptor titleImage) {
        super(pageName, title, titleImage);
    }

    @Override
    public final void createControl(final Composite parent) {
        initializeDialogUnits(parent);

        topLevelParent = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(topLevelParent);
        GridLayoutFactory.fillDefaults().applyTo(topLevelParent);

        final Composite innerParent = new Composite(topLevelParent, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(innerParent);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(innerParent);

        create(innerParent);

        progressParent = new Composite(topLevelParent, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(progressParent);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.END).grab(true, false).applyTo(progressParent);
        
        final ProgressBar progressBar = new ProgressBar(progressParent, SWT.SMOOTH | SWT.INDETERMINATE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.END).grab(true, false).applyTo(progressBar);

        hideProgress();
        setControl(topLevelParent);
    }
    
    protected abstract void create(Composite parent);

    protected final <T> Job scheduleOperation(final Class<T> resultClass, final MonitoredJobFunction<T> function,
            final JobFinishListener<T> finishListener) {
        showProgress();

        blockNavigation();

        final QualifiedName resultKey = new QualifiedName(RedPlugin.PLUGIN_ID, "jobResult");
        final Job job = new Job("Wizard page operation") {

            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                final T result = function.run(monitor);
                setProperty(resultKey, result);
                return Status.OK_STATUS;
            }
        };
        job.setSystem(true);
        job.addJobChangeListener(
                new JobFinishListenerToJobChangeListenerAdapter<>(resultKey, resultClass, finishListener));
        job.schedule();
        return job;
    }

    private void showProgress() {
        ((GridData) progressParent.getLayoutData()).exclude = false;
        progressParent.setVisible(true);
        topLevelParent.layout();
    }

    private void hideProgress() {
        ((GridData) progressParent.getLayoutData()).exclude = true;
        progressParent.setVisible(false);
        topLevelParent.layout();
    }

    private void blockNavigation() {
        navigationBlocked = true;
        setPageComplete(false);
    }

    private void unblockNavigation() {
        navigationBlocked = false;
        // page completion should be checked by implementing class
    }

    @Override
    public IWizardPage getPreviousPage() {
        return navigationBlocked ? null : super.getPreviousPage();
    }
    
    @FunctionalInterface
    protected static interface MonitoredJobFunction<T> {

        T run(IProgressMonitor monitor);
    }

    @FunctionalInterface
    protected static interface JobFinishListener<T> {

        void jobFinished(T result);

    }

    private class JobFinishListenerToJobChangeListenerAdapter<T> extends JobChangeAdapter {

        private final QualifiedName resultKey;

        private final Class<T> resultClass;

        private final JobFinishListener<T> listener;

        public JobFinishListenerToJobChangeListenerAdapter(final QualifiedName resultKey, final Class<T> resultClass,
                final JobFinishListener<T> listener) {
            this.resultKey = resultKey;
            this.resultClass = resultClass;
            this.listener = listener;
        }

        @Override
        public void done(final IJobChangeEvent event) {
            SwtThread.asyncExec(() -> {
                final T result = resultClass.cast(event.getJob().getProperty(resultKey));
                listener.jobFinished(result);

                if (topLevelParent != null && !topLevelParent.isDisposed()) {
                    hideProgress();
                    unblockNavigation();
                    getContainer().updateButtons();
                }
            });
        }
    }
}
