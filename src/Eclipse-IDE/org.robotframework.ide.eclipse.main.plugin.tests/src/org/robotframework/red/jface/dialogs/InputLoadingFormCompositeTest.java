/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.dialogs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Semaphore;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.jface.dialogs.InputLoadingFormComposite.InputJob;
import org.robotframework.red.junit.ShellProvider;

public class InputLoadingFormCompositeTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void whenCompositeIsCreated_theInputLoadingJobStartsAndLoadingLabelIsVisible() throws InterruptedException {
        final Semaphore semaphore = new Semaphore(0);
        final InputJob inputJob = createWaitingJob(semaphore);
        final InputLoadingFormComposite composite = prepareCompositeToTest(shellProvider.getShell(), "title", inputJob);
        composite.createComposite();

        assertThat(composite.getForm().isBusy()).isTrue();
        final Composite internalComposite = (Composite) composite.getForm().getBody().getChildren()[0];
        final StackLayout layout = (StackLayout) internalComposite.getLayout();
        
        assertThat(layout.topControl).isInstanceOf(Label.class);
        assertThat(layout.topControl.isVisible()).isTrue();
        assertThat(((Label) layout.topControl).getText()).isEqualTo("loading...");

        semaphore.release();
        inputJob.join();
    }

    @Test
    public void whenCompositeIsCreatedAndInputIsLoaded_theLoadingLabelGetsInvisibleAndOtherIsFilledAndShown()
            throws InterruptedException {
        final InputJob inputJob = createSimpleJob();

        final InputLoadingFormComposite composite = prepareCompositeToTest(shellProvider.getShell(), "title", inputJob);
        composite.createComposite();
        inputJob.join();

        // handle all the events which came to UI thread after the job has finished
        while (composite.getForm().isBusy()) {
            Display.getCurrent().readAndDispatch();
        }

        assertThat(composite.getForm().isBusy()).isFalse();
        final Composite internalComposite = (Composite) composite.getForm().getBody().getChildren()[0];
        final StackLayout layout = (StackLayout) internalComposite.getLayout();

        assertThat(layout.topControl).isInstanceOf(CLabel.class);
        assertThat(layout.topControl.isVisible()).isTrue();
        assertThat(composite.getFocusControl()).isSameAs(layout.topControl);
        assertThat(((CLabel) layout.topControl).getText()).isEqualTo("done creating input");
    }

    @Test
    public void thereIsAClosingActionCreated() throws InterruptedException {
        final InputJob inputJob = createSimpleJob();

        final InputLoadingFormComposite composite = prepareCompositeToTest(shellProvider.getShell(), "title", inputJob);
        composite.createComposite();
        inputJob.join();

        // handle all the events which came to UI thread after the job has finished
        while (composite.getForm().isBusy()) {
            Display.getCurrent().readAndDispatch();
        }

        final IContributionItem[] formActions = composite.getForm().getToolBarManager().getItems();
        assertThat(formActions).hasSize(1);
        final ActionContributionItem item = (ActionContributionItem) formActions[0];
        final IAction action = item.getAction();
        assertThat(action).isInstanceOf(CloseDialogAction.class);

        action.run();

        assertThat(composite.isDisposed()).isTrue();
        assertThat(composite.getToolkit()).isNull();
    }

    private static InputLoadingFormComposite prepareCompositeToTest(final Composite parent, final String title,
            final InputJob inputJob) {
        return new InputLoadingFormComposite(parent, SWT.NONE, title) {

            private CLabel label;

            @Override
            protected Control createControl(final Composite parent) {
                label = new CLabel(parent, SWT.NONE);
                label.setText("test label");
                return label;
            }

            @Override
            protected InputJob provideInputCollectingJob() {
                return inputJob;
            }

            @Override
            protected void fillControl(final Object jobResult) {
                label.setText((String) jobResult);
            }
        };
    }

    private InputJob createSimpleJob() {
        return new InputJob("test job") {

            @Override
            protected Object createInput(final IProgressMonitor monitor) {
                setStatus(Status.OK_STATUS);
                return "done creating input";
            }
        };
    }

    private static InputJob createWaitingJob(final Semaphore semaphore) {
        return new InputJob("test job") {
            @Override
            protected Object createInput(final IProgressMonitor monitor) {
                try {
                    semaphore.acquire();
                } catch (final InterruptedException e) {
                    throw new IllegalStateException(e);
                }
                setStatus(Status.OK_STATUS);
                return "done creating input";
            }
        };
    }
}
