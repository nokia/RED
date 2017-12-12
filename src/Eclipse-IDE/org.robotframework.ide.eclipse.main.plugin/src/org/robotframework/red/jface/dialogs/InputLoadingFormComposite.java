/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.annotations.VisibleForTesting;

public abstract class InputLoadingFormComposite extends Composite {

    private final String title;

    private FormToolkit formToolkit = new RedFormToolkit(Display.getCurrent());
    private Form form;
    private Composite innerComposite;

    private StackLayout stackLayout;
    private Control loadingControl;
    private Control control;

    public InputLoadingFormComposite(final Composite parent, final int style, final String title) {
        super(parent, style);
        this.title = title;
    }

    @VisibleForTesting
    Form getForm() {
        return form;
    }

    protected Control getControl() {
        return control;
    }

    protected final FormToolkit getToolkit() {
        return formToolkit;
    }

    protected final IMessageManager getMessageManager() {
        return form.getMessageManager();
    }

    protected final void createComposite() {
        addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(final DisposeEvent e) {
                formToolkit.dispose();
                formToolkit = null;
            }
        });
        setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        setLayout(new FillLayout(SWT.VERTICAL));

        form = createForm(this);
        final Control headClient = createHeadClient(form.getHead());
        if (headClient != null) {
            form.setHeadClient(headClient);
        }
        innerComposite = createBodyComposite(form.getBody());
        loadingControl = createLoadingInfoControl(innerComposite);
        control = createControl(innerComposite);

        createActions();

        switchLoadingControl();
        scheduleInputCollectingJob();
    }

    private Form createForm(final Composite parent) {
        final Form form = formToolkit.createForm(parent);
        form.setMessage("");
        formToolkit.decorateFormHeading(form);
        formToolkit.paintBordersFor(form);
        form.setText(title);
        form.getBody().setLayout(new FillLayout(SWT.VERTICAL));

        final PopupDialogMoveController moveController = new PopupDialogMoveController(getShell());
        form.getHead().addMouseMoveListener(moveController);
        form.getHead().addDragDetectListener(moveController);
        form.getHead().addMouseListener(moveController);
        return form;
    }

    protected final void setFormImage(final ImageDescriptor image) {
        form.setImage(ImagesManager.getImage(image));
    }

    protected Control createHeadClient(final Composite head) {
        // nothing to do, override if needed
        return null;
    }

    private Composite createBodyComposite(final Composite parent) {
        final Composite innerComposite = formToolkit.createComposite(parent, SWT.NONE);
        stackLayout = new StackLayout();
        innerComposite.setLayout(stackLayout);
        formToolkit.paintBordersFor(innerComposite);
        return innerComposite;
    }

    protected Label createLoadingInfoControl(final Composite parent) {
        final Label loadingLabel = formToolkit.createLabel(parent, "loading...", SWT.NONE);
        loadingLabel.setAlignment(SWT.CENTER);
        return loadingLabel;
    }

    protected abstract Control createControl(Composite parent);

    protected void createActions() {
        addAction(new CloseDialogAction(getShell(), RedImages.getCloseImage()));
    }

    protected final void addAction(final Action action) {
        form.getToolBarManager().add(action);
        form.getToolBarManager().update(true);
    }

    protected final void addAction(final IContributionItem contribution) {
        form.getToolBarManager().add(contribution);
        form.getToolBarManager().update(true);
    }

    private void switchLoadingControl() {
        stackLayout.topControl = loadingControl;
    }

    private void switchControl() {
        stackLayout.topControl = control.getParent() == innerComposite ? control : control.getParent();
    }

    private void scheduleInputCollectingJob() {
        final Job job = provideInputCollectingJob();
        job.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(final IJobChangeEvent event) {
                if (isDisposed()) {
                    return;
                }
                getDisplay().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        fillControl(((InputJob) event.getJob()).input);
                        if (!innerComposite.isDisposed()) {
                            switchControl();
                            innerComposite.layout();
                            getControl().setFocus();
                        }
                        form.setBusy(false);
                    }
                });
            }
        });
        job.schedule();
        form.setBusy(true);
    }

    protected abstract InputJob provideInputCollectingJob();

    protected abstract void fillControl(final Object jobResult);

    public Control getFocusControl() {
        return control;
    }

    private class PopupDialogMoveController extends MouseAdapter implements MouseMoveListener, DragDetectListener {

        private final Shell shell;

        private Point relativeCursor;

        public PopupDialogMoveController(final Shell shell) {
            this.shell = shell;
            this.relativeCursor = null;
        }

        @Override
        public void mouseUp(final MouseEvent e) {
            relativeCursor = null;
        }

        @Override
        public void dragDetected(final DragDetectEvent e) {
            relativeCursor = new Point(e.x, e.y);
        }

        @Override
        public void mouseMove(final MouseEvent e) {
            if (relativeCursor != null) {
                final Point cursor = Display.getCurrent().getCursorLocation();
                shell.setLocation(cursor.x - relativeCursor.x, cursor.y - relativeCursor.y);
            }
        }
    }

    public static abstract class InputJob extends Job {

        private Object input;
        private IStatus status;

        public InputJob(final String name) {
            super(name);
        }

        @Override
        public final IStatus run(final IProgressMonitor monitor) {
            input = createInput(monitor);
            return status;
        }

        protected abstract Object createInput(final IProgressMonitor monitor);

        protected final void setStatus(final IStatus status) {
            this.status = status;
        }
    }
}
