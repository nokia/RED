package org.robotframework.ide.eclipse.main.plugin.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Random;

import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

public class RemoveAllTerminatedRedSessionsActionTest {

    @Test
    public void whenCreatedTheActionIsDisabledHasProperNameAndIconsSet() {
        final RemoveAllTerminatedRedSessionsAction action = new RemoveAllTerminatedRedSessionsAction();

        assertThat(action.isEnabled()).isFalse();
        assertThat(action.getText()).isEqualTo("Remove all terminated RED sessions");
        assertThat(action.getImageDescriptor()).isEqualTo(RedImages.getCloseAllImage());
        assertThat(action.getDisabledImageDescriptor()).isEqualTo(RedImages.getDisabledCloseAllImage());
    }

    @Test
    public void nothingIsRemovedWhenActionIsRunButThereAreNoSessionConsoles() {
        final IConsole[] consoles = new IConsole[] { mock(IConsole.class), mock(IConsole.class) };
        final IConsoleManager consoleManager = mock(IConsoleManager.class);
        when(consoleManager.getConsoles()).thenReturn(consoles);
        
        final RemoveAllTerminatedRedSessionsAction action = new RemoveAllTerminatedRedSessionsAction(consoleManager);
        action.run();

        verify(consoleManager).getConsoles();
        verify(consoleManager).removeConsoles(new IConsole[0]);
        verifyNoMoreInteractions(consoleManager);
    }

    @Test
    public void nothingIsRemovedWhenActionIsRunButAllSessionConsolesAreAlive() {
        final IConsole[] consoles = new IConsole[] { mock(IConsole.class), aliveSessionConsole(),
                mock(IConsole.class), aliveSessionConsole() };
        final IConsoleManager consoleManager = mock(IConsoleManager.class);
        when(consoleManager.getConsoles()).thenReturn(consoles);

        final RemoveAllTerminatedRedSessionsAction action = new RemoveAllTerminatedRedSessionsAction(consoleManager);
        action.run();

        verify(consoleManager).getConsoles();
        verify(consoleManager).removeConsoles(new IConsole[0]);
        verifyNoMoreInteractions(consoleManager);
    }

    @Test
    public void allTerminatedSessionConsolesAreRemovedWhenActionIsRun() {
        final IConsole terminated1 = terminatedSessionConsole();
        final IConsole terminated2 = terminatedSessionConsole();
        final IConsole[] consoles = new IConsole[] { mock(IConsole.class), terminated1,
                aliveSessionConsole(), mock(IConsole.class), aliveSessionConsole(), terminated2 };
        final IConsoleManager consoleManager = mock(IConsoleManager.class);
        when(consoleManager.getConsoles()).thenReturn(consoles);

        final RemoveAllTerminatedRedSessionsAction action = new RemoveAllTerminatedRedSessionsAction(consoleManager);
        action.run();

        verify(consoleManager).getConsoles();
        verify(consoleManager).removeConsoles(new IConsole[] { terminated1, terminated2 });
        verifyNoMoreInteractions(consoleManager);
    }

    private static IConsole aliveSessionConsole() {
        final Process process = mock(Process.class);
        when(process.isAlive()).thenReturn(true);

        return new RedSessionConsole("c" + new Random().nextInt(), process);
    }

    private static IConsole terminatedSessionConsole() {
        final Process process = mock(Process.class);
        when(process.isAlive()).thenReturn(false);

        return new RedSessionConsole("c" + new Random().nextInt(), process);
    }
}
