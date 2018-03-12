/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.util.function.Supplier;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.IssuesStrategy;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.swt.SwtThread.Evaluation;

public class DebuggerErrorDecider implements Supplier<Boolean> {

    // TODO : this class have UI dependencies and is being used by non-UI code, so this should be
    // addressed when splitting plugin into more plugins

    private final RedPreferences preferences;

    private IssuesStrategy debuggerShouldPauseOnError;

    public DebuggerErrorDecider(final RedPreferences preferences) {
        this.preferences = preferences;
        this.debuggerShouldPauseOnError = preferences.getDebuggerShouldPauseOnError();
    }

    @Override
    public Boolean get() {
        if (debuggerShouldPauseOnError == IssuesStrategy.PROMPT) {
            return SwtThread.syncEval(new Evaluation<Boolean>() {

                @Override
                public Boolean runCalculation() {
                    final MessageDialogWithToggle dialog = new MessageDialogWithToggle(
                            RedPlugin.getDefault().getWorkbench().getModalDialogShellProvider().getShell(),
                            "Debugger error", null,
                            "RED debugger have entered into an erroneous state.\n\nDo you want the debugger to suspend the execution now?",
                            MessageDialog.QUESTION, new String[] { "Suspend", "Continue" }, 0, "Remember my decision",
                            false);

                    final int returnedCode = dialog.open();

                    if (dialog.getToggleState()) {
                        debuggerShouldPauseOnError = returnedCode == IDialogConstants.INTERNAL_ID
                                ? IssuesStrategy.ALWAYS
                                : IssuesStrategy.NEVER;
                        preferences.setDebuggerShouldPauseOnError(debuggerShouldPauseOnError);
                    }
                    return returnedCode == IDialogConstants.INTERNAL_ID;
                }
            });
        } else {
            return debuggerShouldPauseOnError == IssuesStrategy.ALWAYS;
        }
    }

}
