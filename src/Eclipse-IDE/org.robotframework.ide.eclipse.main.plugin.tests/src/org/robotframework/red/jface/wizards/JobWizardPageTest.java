/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.wizards;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.jface.wizards.JobWizardPage.MonitoredJobFunction;
import org.robotframework.red.junit.Controls;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.swt.SwtThread;

public class JobWizardPageTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void progressBarIsCreatedButNotVisible_whenNewPageIsConstructed() {
        final Shell shell = shellProvider.getShell();
        createPage(shell);

        final ProgressBar progressBar = Controls.getControls(shell, ProgressBar.class).get(0);
        assertThat(progressBar.isVisible()).isFalse();
    }

    @Test
    public void progressBarIsVisible_whenThereIsAnOperationScheduled() throws Exception {
        final Shell shell = shellProvider.getShell();
        final JobWizardPage page = createPage(shell);

        final AtomicBoolean isVisible = new AtomicBoolean(false);

        final MonitoredJobFunction<Void> fun = (monitor) -> {
            SwtThread.syncExec(() -> {
                final ProgressBar progressBar = Controls.getControls(shell, ProgressBar.class).get(0);
                isVisible.set(progressBar.isVisible());

            });
            return null;
        };
        page.scheduleOperation(Void.class, fun, r -> {}).join();
        assertThat(isVisible.get()).isTrue();
    }

    private static JobWizardPage createPage(final Composite parent) {
        final JobWizardPage page = new JobWizardPage("wizard page", "page title", null) {

            @Override
            protected void create(final Composite parent) {
                // nothing to do display in test page
            }
        };
        page.createControl(parent);
        return page;
    }
}
