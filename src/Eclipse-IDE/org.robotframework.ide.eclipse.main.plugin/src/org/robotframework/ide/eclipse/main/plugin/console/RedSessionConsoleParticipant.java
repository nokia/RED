/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.console;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;
import org.rf.ide.core.executor.PythonProcessListener;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RedImages;


public class RedSessionConsoleParticipant implements IConsolePageParticipant {

    private TerminateRedSessionAction terminateAction;
    private RemoveTerminatedRedSessionAction removeTerminatedAction;
    private RemoveAllTerminatedRedSessionsAction removeAllTerminatedAction;
    private SaveRedSessionAction saveSessionAction;
    private ActivateOnInputChangeAction activateOnStdOutChangeAction;
    private ActivateOnInputChangeAction activateOnStdErrChangeAction;

    private RedSessionConsole sessionConsole;

    private ActionsUpdater actionsUpdater;

    @Override
    public Object getAdapter(final Class adapter) {
        return null;
    }

    @Override
    public void init(final IPageBookViewPage page, final IConsole console) {
        sessionConsole = (RedSessionConsole) console;

        terminateAction = new TerminateRedSessionAction(sessionConsole);
        removeTerminatedAction = new RemoveTerminatedRedSessionAction(sessionConsole);
        removeAllTerminatedAction = new RemoveAllTerminatedRedSessionsAction();
        saveSessionAction = new SaveRedSessionAction(page.getSite().getShell(), sessionConsole);
        activateOnStdOutChangeAction = new ActivateOnInputChangeAction(sessionConsole.getStdOutStream(), "standard out",
                RedImages.getActivateOnStdOutImage());
        activateOnStdErrChangeAction = new ActivateOnInputChangeAction(sessionConsole.getStdErrStream(),
                "standard error", RedImages.getActivateOnStdErrImage());
        
        final IActionBars actionBars = page.getSite().getActionBars();
        configureToolBar(actionBars.getToolBarManager());

        actionsUpdater = new ActionsUpdater();
        RobotRuntimeEnvironment.addProcessListener(actionsUpdater);
    }

    private void configureToolBar(final IToolBarManager mgr) {
        mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, terminateAction);
        mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, removeTerminatedAction);
        mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, removeAllTerminatedAction);
        mgr.appendToGroup(IConsoleConstants.LAUNCH_GROUP, saveSessionAction);
        mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, activateOnStdOutChangeAction);
        mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, activateOnStdErrChangeAction);
    }

    @Override
    public void activated() {
        // nothing to do now
    }

    @Override
    public void deactivated() {
        // nothing to do now
    }

    @Override
    public void dispose() {
        RobotRuntimeEnvironment.removeProcessListener(actionsUpdater);
        if (terminateAction != null) {
            terminateAction.dispose();
            terminateAction = null;
        }
        if (removeTerminatedAction != null) {
            removeTerminatedAction.dispose();
            removeTerminatedAction = null;
        }
        if (saveSessionAction != null) {
            saveSessionAction.dispose();
            saveSessionAction = null;
        }
        if (activateOnStdOutChangeAction != null) {
            activateOnStdOutChangeAction.dispose();
            activateOnStdOutChangeAction = null;
        }
        if (activateOnStdErrChangeAction != null) {
            activateOnStdErrChangeAction.dispose();
            activateOnStdErrChangeAction = null;
        }
        removeAllTerminatedAction = null;
    }

    private class ActionsUpdater implements PythonProcessListener {

        @Override
        public void processStarted(final String name, final Process process) {
            // nothing to do
        }

        @Override
        public void processEnded(final Process process) {
            if (process == sessionConsole.getProcess()) {
                terminateAction.setEnabled(false);
                removeTerminatedAction.setEnabled(true);
            }
            removeAllTerminatedAction.setEnabled(true);
        }

        @Override
        public void lineRead(final Process serverProcess, final String line) {
            // nothing to do
        }

        @Override
        public void errorLineRead(final Process serverProcess, final String line) {
            // nothing to do
        }
    }
}
