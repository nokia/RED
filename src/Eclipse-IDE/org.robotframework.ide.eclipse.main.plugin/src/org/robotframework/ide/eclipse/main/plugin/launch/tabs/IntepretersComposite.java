/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static com.google.common.collect.Lists.newArrayList;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.statushandlers.StatusManager;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

import com.google.common.base.Optional;


/**
 * @author Michal Anglart
 *
 */
class IntepretersComposite extends Composite {

    private final InterpreterListener listener;

    private Button useProjectExecutorButton;
    private Button useSystemExecutorButton;
    private Combo comboExecutorName;
    private Button checkEnvironmentBtn;

    IntepretersComposite(final Composite parent, final InterpreterListener listener) {
        super(parent, SWT.NONE);
        this.listener = listener;

        GridLayoutFactory.fillDefaults().numColumns(4).spacing(2, 2).margins(5, 5).applyTo(this);

        createProjectInterpreterButton();
        createSystemInterpreterButton();
        createCheckEnvironmentButton();
    }

    private void createProjectInterpreterButton() {
        useProjectExecutorButton = new Button(this, SWT.RADIO);
        useProjectExecutorButton.setText("Use interpreter as defined in project configuration");
        GridDataFactory.fillDefaults().grab(true, false).span(4, 1).applyTo(useProjectExecutorButton);
        useProjectExecutorButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                checkEnvironmentBtn.setEnabled(false);
                comboExecutorName.setEnabled(false);
                listener.interpreterChanged(Optional.<SuiteExecutor> absent());
            }
        });
    }

    private void createSystemInterpreterButton() {
        useSystemExecutorButton = new Button(this, SWT.RADIO);
        useSystemExecutorButton.setText("Use");
        GridDataFactory.fillDefaults().grab(false, false).applyTo(useSystemExecutorButton);
        useSystemExecutorButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                checkEnvironmentBtn.setEnabled(true);
                comboExecutorName.setEnabled(true);
                listener.interpreterChanged(Optional
                        .of(SuiteExecutor.fromName(comboExecutorName.getItem(comboExecutorName.getSelectionIndex()))));
            }
        });
        comboExecutorName = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboExecutorName.setItems(SuiteExecutor.allExecutorNames().toArray(new String[0]));
        comboExecutorName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                listener.interpreterChanged(Optional
                        .of(SuiteExecutor.fromName(comboExecutorName.getItem(comboExecutorName.getSelectionIndex()))));
            }
        });
        GridDataFactory.fillDefaults().applyTo(comboExecutorName);
        final Label systemExecutorLbl = new Label(this, SWT.NONE);
        systemExecutorLbl.setText("interpreter taken from sytem PATH environment variable");
    }

    private void createCheckEnvironmentButton() {
        checkEnvironmentBtn = new Button(this, SWT.PUSH);
        checkEnvironmentBtn.setText("Check interpreter");
        GridDataFactory.fillDefaults().grab(false, false).align(SWT.END, SWT.FILL).applyTo(checkEnvironmentBtn);
        checkEnvironmentBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent event) {
                try {
                    final Shell shell = getShell();
                    final String chosenExecutorName = comboExecutorName.getItem(comboExecutorName.getSelectionIndex());
                    new ProgressMonitorDialog(shell).run(false, false,
                            new CheckEnvironmentRunnable(shell, chosenExecutorName));
                } catch (InvocationTargetException | InterruptedException e) {
                    StatusManager.getManager().handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, e.getMessage()),
                            StatusManager.BLOCK);
                }
            }
        });
    }

    void setInput(final boolean usesProjectInterpreter, final SuiteExecutor executor) {
        useProjectExecutorButton.setSelection(usesProjectInterpreter);
        useSystemExecutorButton.setSelection(!usesProjectInterpreter);
        comboExecutorName.setEnabled(!usesProjectInterpreter);
        checkEnvironmentBtn.setEnabled(!usesProjectInterpreter);

        comboExecutorName.select(newArrayList(comboExecutorName.getItems()).indexOf(executor.name()));
    }

    boolean isUsingProjectInterpreter() {
        return useProjectExecutorButton.getSelection();
    }

    SuiteExecutor getChosenSystemExecutor() {
        return SuiteExecutor.fromName(comboExecutorName.getItem(comboExecutorName.getSelectionIndex()));
    }

    private static final class CheckEnvironmentRunnable implements IRunnableWithProgress {

        private final Shell shell;

        private final String chosenExecutorName;

        private CheckEnvironmentRunnable(final Shell shell, final String chosenExecutorName) {
            this.shell = shell;
            this.chosenExecutorName = chosenExecutorName;
        }

        @Override
        public void run(final IProgressMonitor monitor)
                throws InvocationTargetException, InterruptedException {
            final SuiteExecutor executor = SuiteExecutor.fromName(chosenExecutorName);
            final String version = RobotRuntimeEnvironment.getVersion(executor);
            if (version == null) {
                throw new IllegalStateException(
                        "The " + executor.name() + " interpreter has no Robot installed");
            } else {
                MessageDialog.openInformation(shell, "Interpreter checked", "The "
                        + executor.name() + " interpreter has " + version + " installed");
            }
        }
    }

    public interface InterpreterListener {

        void interpreterChanged(Optional<SuiteExecutor> newExecutor);
    }
}
