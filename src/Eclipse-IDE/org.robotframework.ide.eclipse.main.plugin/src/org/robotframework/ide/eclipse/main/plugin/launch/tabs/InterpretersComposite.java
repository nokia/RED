/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.statushandlers.StatusManager;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

/**
 * @author Michal Anglart
 */
class InterpretersComposite extends Composite {

    private final InterpreterListener listener;

    private Button useProjectExecutorButton;

    private Button useSystemExecutorButton;

    private Combo comboExecutorName;

    private Button checkEnvironmentBtn;

    InterpretersComposite(final Composite parent, final InterpreterListener listener) {
        super(parent, SWT.NONE);
        this.listener = listener;

        GridLayoutFactory.fillDefaults().numColumns(4).spacing(2, 2).margins(0, 5).applyTo(this);

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
                listener.interpreterChanged(Optional.empty());
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
        comboExecutorName.addModifyListener(e -> listener.interpreterChanged(
                Optional.of(SuiteExecutor.fromName(comboExecutorName.getItem(comboExecutorName.getSelectionIndex())))));
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
                final String chosenExecutorName = comboExecutorName.getItem(comboExecutorName.getSelectionIndex());
                try {
                    new ProgressMonitorDialog(getShell()).run(false, false, monitor -> {
                        final SuiteExecutor executor = SuiteExecutor.fromName(chosenExecutorName);
                        final String version = RobotRuntimeEnvironment.getVersion(executor);
                        if (version == null) {
                            MessageDialog.openError(null, "Interpreter checked",
                                    "The " + executor.name() + " interpreter has no Robot installed");
                        } else {
                            MessageDialog.openInformation(null, "Interpreter checked",
                                    "The " + executor.name() + " interpreter has " + version + " installed");
                        }
                    });
                } catch (final InterruptedException e) {
                    StatusManager.getManager().handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, e.getMessage(), e),
                            StatusManager.BLOCK);
                } catch (final InvocationTargetException e) {
                    StatusManager.getManager().handle(
                            new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID,
                                    "Unable to find " + SuiteExecutor.fromName(chosenExecutorName).executableName()
                                            + " executable in the system.",
                                    e.getTargetException()),
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

        final String executorName = executor != null ? executor.name() : SuiteExecutor.Python.name();
        comboExecutorName.select(Arrays.asList(comboExecutorName.getItems()).indexOf(executorName));
    }

    boolean isUsingProjectInterpreter() {
        return useProjectExecutorButton.getSelection();
    }

    SuiteExecutor getChosenSystemExecutor() {
        return SuiteExecutor.fromName(comboExecutorName.getItem(comboExecutorName.getSelectionIndex()));
    }

    @FunctionalInterface
    public interface InterpreterListener {

        void interpreterChanged(Optional<SuiteExecutor> newExecutor);
    }
}
