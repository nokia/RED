/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
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

    private final Random random = new Random();

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
        final IConsole alive1 = sessionConsole(true);
        final IConsole alive2 = sessionConsole(true);
        final IConsole other1 = mock(IConsole.class);
        final IConsole other2 = mock(IConsole.class);
        final IConsole[] consoles = new IConsole[] { other1, alive1, other2, alive2 };
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
        final IConsole terminated1 = sessionConsole(false);
        final IConsole terminated2 = sessionConsole(false);
        final IConsole alive1 = sessionConsole(true);
        final IConsole alive2 = sessionConsole(true);
        final IConsole other1 = mock(IConsole.class);
        final IConsole other2 = mock(IConsole.class);
        final IConsole[] consoles = new IConsole[] { other1, terminated1, alive1, other2, alive2, terminated2 };
        final IConsoleManager consoleManager = mock(IConsoleManager.class);
        when(consoleManager.getConsoles()).thenReturn(consoles);

        final RemoveAllTerminatedRedSessionsAction action = new RemoveAllTerminatedRedSessionsAction(consoleManager);
        action.run();

        verify(consoleManager).getConsoles();
        verify(consoleManager).removeConsoles(new IConsole[] { terminated1, terminated2 });
        verifyNoMoreInteractions(consoleManager);
    }

    private IConsole sessionConsole(final boolean isAlive) {
        final Process process = mock(Process.class);
        when(process.isAlive()).thenReturn(isAlive);

        return new RedSessionConsole("c" + random.nextInt(), process);
    }
}
