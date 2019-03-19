/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.robotframework.red.swt.SwtThread;

public class ProgressMonitorDialogWithConsole extends ProgressMonitorDialog {

    private StyledText console;

    private boolean doNotClose = false;

    public ProgressMonitorDialogWithConsole(final Shell parent) {
        super(parent);
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);

        console = new StyledText(composite, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        console.setFont(JFaceResources.getTextFont());
        GridDataFactory.fillDefaults().span(2, 1).grab(true, true).minSize(120, 200).applyTo(console);
        return composite;
    }

    public void run(final IRunnableWithProgressAndOutput runnable)
            throws InvocationTargetException, InterruptedException {
        final StringBuilder out = new StringBuilder();
        final Consumer<String> outputWriter = line -> SwtThread
                .syncExec(() -> {
                    if (out.length() > 0) {
                        out.append(System.lineSeparator());
                    }
                    out.append(line);
                    console.setText(out.toString());
                    console.setTopIndex(console.getLineCount() - 1);
                });
        run(true, true, monitor -> runnable.run(monitor, outputWriter));
    }

    public final void markDoNotClose() {
        doNotClose = true;
    }

    @Override
    public boolean close() {
        if (doNotClose) {
            clearCursors();
            doNotClose = false;
            return false;
        }
        return super.close();
    }

    @FunctionalInterface
    public static interface IRunnableWithProgressAndOutput {

        public void run(IProgressMonitor monitor, Consumer<String> outputConsumer)
                throws InvocationTargetException,
                InterruptedException;
    }
}
