package org.robotframework.ide.eclipse.main.plugin.views.message;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.views.message.ClearLogMessageLogHandler.E4ClearLogMessageLogHandler;

@SuppressWarnings("restriction")
public class ClearLogMessageLogHandlerTest {

    @Test
    public void viewIsNotCleared_whenThereIsNoCurrentLaunch() {
        final MessageLogViewWrapper viewWrapper = mock(MessageLogViewWrapper.class);
        final MessageLogView view = mock(MessageLogView.class);

        when(viewWrapper.getComponent()).thenReturn(view);
        when(view.getCurrentlyShownLaunch()).thenReturn(Optional.empty());

        new E4ClearLogMessageLogHandler().clear(viewWrapper);

        verify(view, never()).clearView();
    }

    @Test
    public void viewIsNotCleared_whenThereIsCurrentLaunch_butThereIsNoMessagesStore() {
        final MessageLogViewWrapper viewWrapper = mock(MessageLogViewWrapper.class);
        final MessageLogView view = mock(MessageLogView.class);

        final RobotTestsLaunch launch = new RobotTestsLaunch(null);

        when(viewWrapper.getComponent()).thenReturn(view);
        when(view.getCurrentlyShownLaunch()).thenReturn(Optional.of(launch));

        new E4ClearLogMessageLogHandler().clear(viewWrapper);

        verify(view, never()).clearView();
    }

    @Test
    public void viewIsCleared_whenThereIsCurrentLaunchWithExecution() {
        final MessageLogViewWrapper viewWrapper = mock(MessageLogViewWrapper.class);
        final MessageLogView view = mock(MessageLogView.class);

        final RobotTestsLaunch launch = new RobotTestsLaunch(null);
        launch.getExecutionData(ExecutionMessagesStore.class, ExecutionMessagesStore::new);

        when(viewWrapper.getComponent()).thenReturn(view);
        when(view.getCurrentlyShownLaunch()).thenReturn(Optional.of(launch));

        new E4ClearLogMessageLogHandler().clear(viewWrapper);

        verify(view).clearView();
    }
}
