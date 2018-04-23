/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;

import com.google.common.annotations.VisibleForTesting;

public class RobotConsoleParticipant implements IConsolePageParticipant {

    private final DebugPlugin debugPlugin;

    public RobotConsoleParticipant() {
        this(DebugPlugin.getDefault());
    }

    @VisibleForTesting
    RobotConsoleParticipant(final DebugPlugin debugPlugin) {
        this.debugPlugin = debugPlugin;
    }

    @Override
    public <T> T getAdapter(final Class<T> adapter) {
        return null;
    }

    @Override
    public void init(final IPageBookViewPage page, final IConsole console) {
        final org.eclipse.debug.ui.console.IConsole ioConsole = (org.eclipse.debug.ui.console.IConsole) console;
        final IProcess process = ioConsole.getProcess();
        if (process instanceof IRobotProcess) {
            final IRobotProcess robotProcess = (IRobotProcess) process;

            final InterruptTestsAction interruptAction = new InterruptTestsAction(robotProcess);
            final PauseTestsAction pauseAction = new PauseTestsAction(robotProcess);
            final ResumeTestsAction resumeAction = new ResumeTestsAction(robotProcess);
            resumeAction.setEnabled(false);
            
            final String groupName = "testsSuspsensions";
            final IActionBars actionBars = page.getSite().getActionBars();
            final IToolBarManager toolbarManager = actionBars.getToolBarManager();
            toolbarManager.insertAfter(IConsoleConstants.LAUNCH_GROUP, new GroupMarker(groupName));
            toolbarManager.appendToGroup(groupName, new Separator());
            toolbarManager.appendToGroup(groupName, resumeAction);
            toolbarManager.appendToGroup(groupName, pauseAction);
            toolbarManager.appendToGroup(groupName, interruptAction);
            
            debugPlugin.addDebugEventListener(
                    new DebugEventsListener(robotProcess, interruptAction, pauseAction, resumeAction));
        }
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
        // nothing to do now
    }

    @VisibleForTesting
    class DebugEventsListener implements IDebugEventSetListener {

        private final IRobotProcess process;

        private final PauseTestsAction pauseAction;

        private final ResumeTestsAction resumeAction;

        private final InterruptTestsAction interruptAction;

        public DebugEventsListener(final IRobotProcess process, final InterruptTestsAction interruptAction,
                final PauseTestsAction pauseAction, final ResumeTestsAction resumeAction) {
            this.process = process;
            this.interruptAction = interruptAction;
            this.pauseAction = pauseAction;
            this.resumeAction = resumeAction;
        }

        @Override
        public void handleDebugEvents(final DebugEvent[] events) {
            for (final DebugEvent event : events) {

                if (event.getSource().equals(process)) {
                    if (event.getKind() == DebugEvent.CHANGE) {
                        pauseAction.setEnabled(!process.isSuspended());
                        resumeAction.setEnabled(process.isSuspended());

                    } else if (event.getKind() == DebugEvent.TERMINATE) {
                        pauseAction.setEnabled(false);
                        resumeAction.setEnabled(false);
                        interruptAction.setEnabled(false);
                        debugPlugin.removeDebugEventListener(this);
                    }
                }
            }
        }
    }

    private abstract class ProcessAction extends Action {

        private final IRobotProcess process;

        public ProcessAction(final String text, final int style, final IRobotProcess process) {
            super(text, style);
            this.process = process;
        }

        @Override
        public void run() {
            final Optional<IDebugTarget> target = findDebugTargetFor(process);
            if (target.isPresent()) {
                try {
                    runOn((RobotDebugTarget) target.get());
                } catch (final DebugException e) {
                    runOn(process);
                }
            } else {
                runOn(process);
            }
        }

        private Optional<IDebugTarget> findDebugTargetFor(final IProcess process) {
            final IDebugTarget[] targets = debugPlugin.getLaunchManager().getDebugTargets();
            return Stream.of(targets).filter(target -> target.getProcess() == process).findFirst();
        }

        protected abstract void runOn(final RobotDebugTarget debugTarget) throws DebugException;

        protected abstract void runOn(final IRobotProcess process);
    }

    @VisibleForTesting
    class PauseTestsAction extends ProcessAction {

        PauseTestsAction(final IRobotProcess process) {
            super("Suspend tests", IAction.AS_PUSH_BUTTON, process);
            setImageDescriptor(RedImages.getSuspendImage());
        }

        @Override
        protected void runOn(final RobotDebugTarget debugTarget) throws DebugException {
            debugTarget.suspend();
        }

        @Override
        protected void runOn(final IRobotProcess process) {
            process.suspend();
        }
    }

    @VisibleForTesting
    class ResumeTestsAction extends ProcessAction {

        ResumeTestsAction(final IRobotProcess process) {
            super("Resume tests", IAction.AS_PUSH_BUTTON, process);
            setImageDescriptor(RedImages.getResumeImage());
        }

        @Override
        protected void runOn(final RobotDebugTarget debugTarget) throws DebugException {
            debugTarget.resume();
        }

        @Override
        protected void runOn(final IRobotProcess process) {
            process.resume();
        }
    }

    @VisibleForTesting
    class InterruptTestsAction extends ProcessAction {

        public InterruptTestsAction(final IRobotProcess process) {
            super("Terminate gracefully", IAction.AS_PUSH_BUTTON, process);
            setImageDescriptor(RedImages.getInterruptImage());
            setDisabledImageDescriptor(RedImages.getDisabledInterruptImage());
        }

        @Override
        protected void runOn(final RobotDebugTarget debugTarget) throws DebugException {
            debugTarget.interrupt();
        }

        @Override
        protected void runOn(final IRobotProcess process) {
            process.interrupt();
        }
    }
}
