package org.robotframework.ide.eclipse.main.plugin.launch;

import java.util.Optional;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
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

            final PauseTestsAction pauseAction = new PauseTestsAction(robotProcess);
            final ResumeTestsAction resumeAction = new ResumeTestsAction(robotProcess);
            resumeAction.setEnabled(false);
            
            final String groupName = "testsSuspsensions";
            final IActionBars actionBars = page.getSite().getActionBars();
            final IToolBarManager toolbarManager = actionBars.getToolBarManager();
            toolbarManager.insertAfter(IConsoleConstants.LAUNCH_GROUP, new GroupMarker(groupName));
            toolbarManager.appendToGroup(groupName, new Separator());
            toolbarManager.appendToGroup(groupName, pauseAction);
            toolbarManager.appendToGroup(groupName, resumeAction);
            
            debugPlugin.addDebugEventListener(new DebugEventsListener(robotProcess, pauseAction, resumeAction));
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

    private Optional<IDebugTarget> findDebugTargetFor(final IProcess process) {
        final IDebugTarget[] targets = debugPlugin.getLaunchManager().getDebugTargets();
        for (final IDebugTarget target : targets) {
            if (target.getProcess() == process) {
                return Optional.of(target);
            }
        }
        return Optional.empty();
    }

    @VisibleForTesting
    class DebugEventsListener implements IDebugEventSetListener {

        private final IRobotProcess process;

        private final PauseTestsAction pauseAction;

        private final ResumeTestsAction resumeAction;

        public DebugEventsListener(final IRobotProcess process, final PauseTestsAction pauseAction,
                final ResumeTestsAction resumeAction) {
            this.process = process;
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
                        debugPlugin.removeDebugEventListener(this);
                    }
                }
            }
        }
    }

    @VisibleForTesting
    class PauseTestsAction extends Action {

        private final IRobotProcess process;

        PauseTestsAction(final IRobotProcess process) {
            super("Suspend tests", IAction.AS_PUSH_BUTTON);
            this.process = process;
            setImageDescriptor(DebugUITools.getImageDescriptor("IMG_ELCL_SUSPEND"));
        }

        @Override
        public void run() {
            final Optional<IDebugTarget> target = findDebugTargetFor(process);
            if (target.isPresent()) {
                try {
                    target.get().suspend();
                } catch (final DebugException e) {
                    process.suspend();
                }
            } else {
                process.suspend();
            }
        }
    }

    @VisibleForTesting
    class ResumeTestsAction extends Action {

        private final IRobotProcess process;

        ResumeTestsAction(final IRobotProcess process) {
            super("Resume tests", IAction.AS_PUSH_BUTTON);
            this.process = process;
            setImageDescriptor(DebugUITools.getImageDescriptor("IMG_ELCL_RESUME"));
        }

        @Override
        public void run() {
            final Optional<IDebugTarget> target = findDebugTargetFor(process);
            if (target.isPresent()) {
                try {
                    target.get().resume();
                } catch (final DebugException e) {
                    process.resume();
                }
            } else {
                process.resume();
            }
        }
    }
}
