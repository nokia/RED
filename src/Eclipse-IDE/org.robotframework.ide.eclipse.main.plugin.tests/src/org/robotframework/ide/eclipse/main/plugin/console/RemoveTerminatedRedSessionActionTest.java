package org.robotframework.ide.eclipse.main.plugin.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsoleStream;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

public class RemoveTerminatedRedSessionActionTest {

    @Test
    public void whenCreatedTheActionIsDisabledProperNameAndIcon() {
        final MessageConsoleStream stream = mock(MessageConsoleStream.class);
        when(stream.isActivateOnWrite()).thenReturn(true);

        final RedSessionConsole console = new RedSessionConsole("console", mock(Process.class));
        final RemoveTerminatedRedSessionAction action = new RemoveTerminatedRedSessionAction(console);

        assertThat(action.isEnabled()).isFalse();
        assertThat(action.getText()).isEqualTo("Remove session");
        assertThat(action.getImageDescriptor()).isEqualTo(RedImages.getCloseImage());
        assertThat(action.getDisabledImageDescriptor()).isEqualTo(RedImages.getDisabledCloseImage());
    }

    @Test
    public void consoleIsNotRemoved_whenItIsStillAlive() {
        final IConsoleManager consoleManager = mock(IConsoleManager.class);
        final Process process = mock(Process.class);
        when(process.isAlive()).thenReturn(true);
        final RedSessionConsole console = new RedSessionConsole("c", process);

        final RemoveTerminatedRedSessionAction action = new RemoveTerminatedRedSessionAction(consoleManager, console);
        action.run();

        verifyZeroInteractions(consoleManager);
    }

    @Test
    public void consoleIsRemoved_whenItIsTerminated() {
        final IConsoleManager consoleManager = mock(IConsoleManager.class);
        final Process process = mock(Process.class);
        when(process.isAlive()).thenReturn(false);
        final RedSessionConsole console = new RedSessionConsole("c", process);

        final RemoveTerminatedRedSessionAction action = new RemoveTerminatedRedSessionAction(consoleManager, console);
        action.run();

        verify(consoleManager).removeConsoles(new IConsole[] { console });
        verifyNoMoreInteractions(consoleManager);
    }
}
