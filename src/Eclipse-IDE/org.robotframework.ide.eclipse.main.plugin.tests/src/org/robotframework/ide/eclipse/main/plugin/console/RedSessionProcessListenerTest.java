/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.junit.Test;

public class RedSessionProcessListenerTest {

    @Test
    public void onceProcessStartedTheConsoleIsCreatedAndInitializedForIt_afterTerminationItIsRemoved() {
        final IConsoleManager consoleManager = mock(IConsoleManager.class);
        when(consoleManager.getConsoles()).thenReturn(new IConsole[0]);
        final Process process = mock(Process.class);

        final RedSessionProcessListener listener = new RedSessionProcessListener(consoleManager);
        assertThat(listener.getCurrentProcesses()).isEmpty();
        listener.processStarted("myProcess", process);

        assertThat(listener.getCurrentProcesses()).hasSize(1);
        final RedSessionConsole console = listener.getCurrentProcesses().get(process);
        assertThat(console.getName()).isEqualTo("RED session [myProcess]");
        assertThat(console.getProcess()).isSameAs(process);
        assertThat(console.getStdOutStream()).isNotNull();
        assertThat(console.getStdErrStream()).isNotNull();

        listener.processEnded(process);

        while (Display.getCurrent().readAndDispatch()) {
            // handle all the events which came to UI thread after the job has finished
        }

        assertThat(listener.getCurrentProcesses()).isEmpty();
        assertThat(console.getName()).isEqualTo("<terminated> RED session [myProcess]");
    }

}
