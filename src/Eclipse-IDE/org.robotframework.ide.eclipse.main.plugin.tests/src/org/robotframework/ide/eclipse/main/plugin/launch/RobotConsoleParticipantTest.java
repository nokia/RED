package org.robotframework.ide.eclipse.main.plugin.launch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotConsoleParticipant.DebugEventsListener;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotConsoleParticipant.PauseTestsAction;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotConsoleParticipant.ResumeTestsAction;

public class RobotConsoleParticipantTest {

    @Test
    public void nothingChangesInPage_whenTheConsoleComesFromNonRobotProcess() {
        final RobotConsoleParticipant participant = new RobotConsoleParticipant();
        
        final IDebugConsole console = mock(IDebugConsole.class);
        when(console.getProcess()).thenReturn(mock(IProcess.class));
        final IPageBookViewPage page = mock(IPageBookViewPage.class);
        participant.init(page, console);

        verifyZeroInteractions(page);
        verify(console).getProcess();
        verifyNoMoreInteractions(console);
    }

    @Test
    public void pauseResumeActionsAreAddedToToolbarAndLaunchListenerIsRegistered_whenTheConsoleComesFromRobotProcess() {
        final DebugPlugin debugPlugin = mock(DebugPlugin.class);
        final RobotConsoleParticipant participant = new RobotConsoleParticipant(debugPlugin);

        final IDebugConsole console = mock(IDebugConsole.class);
        when(console.getProcess()).thenReturn(mock(IRobotProcess.class));
        final IToolBarManager toolbarManager = mock(IToolBarManager.class);
        final IActionBars actionBars = mock(IActionBars.class);
        when(actionBars.getToolBarManager()).thenReturn(toolbarManager);
        final IPageSite site = mock(IPageSite.class);
        when(site.getActionBars()).thenReturn(actionBars);
        final IPageBookViewPage page = mock(IPageBookViewPage.class);
        when(page.getSite()).thenReturn(site);
        participant.init(page, console);

        verify(toolbarManager).appendToGroup(any(String.class), any(PauseTestsAction.class));
        verify(toolbarManager).appendToGroup(any(String.class), any(ResumeTestsAction.class));

        verify(debugPlugin).addDebugEventListener(any(DebugEventsListener.class));
    }

    @Test
    public void pauseActionPausesDebugTarget_whenThereIsOneAvailableForGivenProcess() throws Exception {
        final DebugPlugin debugPlugin = mock(DebugPlugin.class);
        final RobotConsoleParticipant participant = new RobotConsoleParticipant(debugPlugin);

        final IRobotProcess process = mock(IRobotProcess.class);
        final PauseTestsAction pauseAction = participant.new PauseTestsAction(process);

        final ILaunchManager launchManager = mock(ILaunchManager.class);
        final IDebugTarget debugTarget1 = mock(IDebugTarget.class);
        final IDebugTarget debugTarget2 = mock(IDebugTarget.class);
        when(debugPlugin.getLaunchManager()).thenReturn(launchManager);
        when(launchManager.getDebugTargets()).thenReturn(new IDebugTarget[] { debugTarget1, debugTarget2 });
        when(debugTarget1.getProcess()).thenReturn(mock(IRobotProcess.class));
        when(debugTarget2.getProcess()).thenReturn(process);

        pauseAction.run();

        verify(debugTarget2).suspend();
        verifyZeroInteractions(process);
    }

    @Test
    public void pauseActionPausesProcess_whenPausingDebugTargetThrowsAnException() throws Exception {
        final DebugPlugin debugPlugin = mock(DebugPlugin.class);
        final RobotConsoleParticipant participant = new RobotConsoleParticipant(debugPlugin);

        final IRobotProcess process = mock(IRobotProcess.class);
        final PauseTestsAction pauseAction = participant.new PauseTestsAction(process);

        final ILaunchManager launchManager = mock(ILaunchManager.class);
        final IDebugTarget debugTarget1 = mock(IDebugTarget.class);
        final IDebugTarget debugTarget2 = mock(IDebugTarget.class);
        when(debugPlugin.getLaunchManager()).thenReturn(launchManager);
        when(launchManager.getDebugTargets()).thenReturn(new IDebugTarget[] { debugTarget1, debugTarget2 });
        when(debugTarget1.getProcess()).thenReturn(mock(IRobotProcess.class));
        when(debugTarget2.getProcess()).thenReturn(process);
        doThrow(DebugException.class).when(debugTarget2).suspend();

        pauseAction.run();

        verify(process).suspend();
    }

    @Test
    public void pauseActionPausesProcess_whenThereIsNoDebugTargetAvailableForGivenProcess() throws Exception {
        final DebugPlugin debugPlugin = mock(DebugPlugin.class);
        final RobotConsoleParticipant participant = new RobotConsoleParticipant(debugPlugin);

        final IRobotProcess process = mock(IRobotProcess.class);
        final PauseTestsAction pauseAction = participant.new PauseTestsAction(process);

        final ILaunchManager launchManager = mock(ILaunchManager.class);
        final IDebugTarget debugTarget1 = mock(IDebugTarget.class);
        final IDebugTarget debugTarget2 = mock(IDebugTarget.class);
        when(debugPlugin.getLaunchManager()).thenReturn(launchManager);
        when(launchManager.getDebugTargets()).thenReturn(new IDebugTarget[] { debugTarget1, debugTarget2 });
        when(debugTarget1.getProcess()).thenReturn(mock(IRobotProcess.class));
        when(debugTarget2.getProcess()).thenReturn(mock(IRobotProcess.class));

        pauseAction.run();

        verify(process).suspend();
    }

    @Test
    public void resumeActionResumesDebugTarget_whenThereIsOneAvailableForGivenProcess() throws Exception {
        final DebugPlugin debugPlugin = mock(DebugPlugin.class);
        final RobotConsoleParticipant participant = new RobotConsoleParticipant(debugPlugin);

        final IRobotProcess process = mock(IRobotProcess.class);
        final ResumeTestsAction resumeAction = participant.new ResumeTestsAction(process);

        final ILaunchManager launchManager = mock(ILaunchManager.class);
        final IDebugTarget debugTarget1 = mock(IDebugTarget.class);
        final IDebugTarget debugTarget2 = mock(IDebugTarget.class);
        when(debugPlugin.getLaunchManager()).thenReturn(launchManager);
        when(launchManager.getDebugTargets()).thenReturn(new IDebugTarget[] { debugTarget1, debugTarget2 });
        when(debugTarget1.getProcess()).thenReturn(mock(IRobotProcess.class));
        when(debugTarget2.getProcess()).thenReturn(process);

        resumeAction.run();

        verify(debugTarget2).resume();
        verifyZeroInteractions(process);
    }

    @Test
    public void resumeActionResumesProcess_whenResumingDebugTargetThrowsAnException() throws Exception {
        final DebugPlugin debugPlugin = mock(DebugPlugin.class);
        final RobotConsoleParticipant participant = new RobotConsoleParticipant(debugPlugin);

        final IRobotProcess process = mock(IRobotProcess.class);
        final ResumeTestsAction resumeAction = participant.new ResumeTestsAction(process);

        final ILaunchManager launchManager = mock(ILaunchManager.class);
        final IDebugTarget debugTarget1 = mock(IDebugTarget.class);
        final IDebugTarget debugTarget2 = mock(IDebugTarget.class);
        when(debugPlugin.getLaunchManager()).thenReturn(launchManager);
        when(launchManager.getDebugTargets()).thenReturn(new IDebugTarget[] { debugTarget1, debugTarget2 });
        when(debugTarget1.getProcess()).thenReturn(mock(IRobotProcess.class));
        when(debugTarget2.getProcess()).thenReturn(process);
        doThrow(DebugException.class).when(debugTarget2).resume();

        resumeAction.run();

        verify(process).resume();
    }

    @Test
    public void resumeActionResumesProcess_whenThereIsNoDebugTargetAvailableForGivenProcess() throws Exception {
        final DebugPlugin debugPlugin = mock(DebugPlugin.class);
        final RobotConsoleParticipant participant = new RobotConsoleParticipant(debugPlugin);

        final IRobotProcess process = mock(IRobotProcess.class);
        final ResumeTestsAction resumeAction = participant.new ResumeTestsAction(process);

        final ILaunchManager launchManager = mock(ILaunchManager.class);
        final IDebugTarget debugTarget1 = mock(IDebugTarget.class);
        final IDebugTarget debugTarget2 = mock(IDebugTarget.class);
        when(debugPlugin.getLaunchManager()).thenReturn(launchManager);
        when(launchManager.getDebugTargets()).thenReturn(new IDebugTarget[] { debugTarget1, debugTarget2 });
        when(debugTarget1.getProcess()).thenReturn(mock(IRobotProcess.class));
        when(debugTarget2.getProcess()).thenReturn(mock(IRobotProcess.class));

        resumeAction.run();

        verify(process).resume();
    }

    @Test
    public void debugEventsListenerDisablesBothActionsAndDeregistersItself_whenProcessTerminates() {
        final DebugPlugin debugPlugin = mock(DebugPlugin.class);
        final RobotConsoleParticipant participant = new RobotConsoleParticipant(debugPlugin);

        final IRobotProcess process = mock(IRobotProcess.class);
        final PauseTestsAction pauseAction = participant.new PauseTestsAction(process);
        final ResumeTestsAction resumeAction = participant.new ResumeTestsAction(process);
        final DebugEventsListener listener = participant.new DebugEventsListener(process, pauseAction, resumeAction);

        assertThat(pauseAction.isEnabled()).isTrue();
        assertThat(resumeAction.isEnabled()).isTrue();

        listener.handleDebugEvents(new DebugEvent[] { new DebugEvent(process, DebugEvent.TERMINATE) });

        assertThat(pauseAction.isEnabled()).isFalse();
        assertThat(resumeAction.isEnabled()).isFalse();
        verify(debugPlugin).removeDebugEventListener(listener);
    }

    @Test
    public void debugEventsListenerChangesNothing_whenDifferentProcessTerminates() {
        final DebugPlugin debugPlugin = mock(DebugPlugin.class);
        final RobotConsoleParticipant participant = new RobotConsoleParticipant(debugPlugin);

        final IRobotProcess process = mock(IRobotProcess.class);
        final PauseTestsAction pauseAction = participant.new PauseTestsAction(process);
        final ResumeTestsAction resumeAction = participant.new ResumeTestsAction(process);
        final DebugEventsListener listener = participant.new DebugEventsListener(process, pauseAction, resumeAction);

        assertThat(pauseAction.isEnabled()).isTrue();
        assertThat(resumeAction.isEnabled()).isTrue();

        listener.handleDebugEvents(
                new DebugEvent[] { new DebugEvent(mock(IRobotProcess.class), DebugEvent.TERMINATE) });

        assertThat(pauseAction.isEnabled()).isTrue();
        assertThat(resumeAction.isEnabled()).isTrue();
        verifyZeroInteractions(debugPlugin);
    }

    @Test
    public void debugEventsListenerDisablesPauseAndEnablesResume_whenProcessChangesAndIsSuspended() {
        final DebugPlugin debugPlugin = mock(DebugPlugin.class);
        final RobotConsoleParticipant participant = new RobotConsoleParticipant(debugPlugin);

        final IRobotProcess process = mock(IRobotProcess.class);
        when(process.isSuspended()).thenReturn(true);
        final PauseTestsAction pauseAction = participant.new PauseTestsAction(process);
        final ResumeTestsAction resumeAction = participant.new ResumeTestsAction(process);
        final DebugEventsListener listener = participant.new DebugEventsListener(process, pauseAction, resumeAction);

        assertThat(pauseAction.isEnabled()).isTrue();
        assertThat(resumeAction.isEnabled()).isTrue();

        listener.handleDebugEvents(new DebugEvent[] { new DebugEvent(process, DebugEvent.CHANGE) });

        assertThat(pauseAction.isEnabled()).isFalse();
        assertThat(resumeAction.isEnabled()).isTrue();
        verifyZeroInteractions(debugPlugin);
    }

    @Test
    public void debugEventsListenerDisablesResumeAndEnablesPause_whenProcessChangesAndIsNotSuspended() {
        final DebugPlugin debugPlugin = mock(DebugPlugin.class);
        final RobotConsoleParticipant participant = new RobotConsoleParticipant(debugPlugin);

        final IRobotProcess process = mock(IRobotProcess.class);
        when(process.isSuspended()).thenReturn(false);
        final PauseTestsAction pauseAction = participant.new PauseTestsAction(process);
        final ResumeTestsAction resumeAction = participant.new ResumeTestsAction(process);
        final DebugEventsListener listener = participant.new DebugEventsListener(process, pauseAction, resumeAction);

        assertThat(pauseAction.isEnabled()).isTrue();
        assertThat(resumeAction.isEnabled()).isTrue();

        listener.handleDebugEvents(new DebugEvent[] { new DebugEvent(process, DebugEvent.CHANGE) });

        assertThat(pauseAction.isEnabled()).isTrue();
        assertThat(resumeAction.isEnabled()).isFalse();
        verifyZeroInteractions(debugPlugin);
    }

    public static interface IDebugConsole extends IConsole, org.eclipse.debug.ui.console.IConsole { }
}
