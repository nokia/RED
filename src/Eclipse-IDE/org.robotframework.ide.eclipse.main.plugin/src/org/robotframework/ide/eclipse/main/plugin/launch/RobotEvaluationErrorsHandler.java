/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.util.Optional;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.agent.event.ConditionEvaluatedEvent;
import org.rf.ide.core.execution.agent.event.VariablesChangedEvent;


public class RobotEvaluationErrorsHandler extends RobotDefaultAgentEventListener {

    @Override
    public void handleConditionEvaluated(final ConditionEvaluatedEvent event) {
        handleError(event.getError());
    }

    @Override
    public void handleVariablesChanged(final VariablesChangedEvent event) {
        handleError(event.getError());
    }

    private static void handleError(final Optional<String> error) {
        // replacing dollars with double dollars, as single dollar is used as mnemonic in labels
        error.ifPresent(e -> {
            final Display display = PlatformUI.getWorkbench().getDisplay();
            final String msg = e.replaceAll("&", "&&");
            display.asyncExec(() -> MessageDialog.openError(display.getActiveShell(), "Debugger Error", msg));
        });
    }

}
