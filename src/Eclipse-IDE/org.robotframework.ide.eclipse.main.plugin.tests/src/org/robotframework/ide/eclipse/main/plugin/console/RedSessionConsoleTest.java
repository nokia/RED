package org.robotframework.ide.eclipse.main.plugin.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.swt.graphics.RGB;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

public class RedSessionConsoleTest {

    @Test
    public void newlyCreatedConsoleHasNameImageAndProcessButNoStreams() {
        final Process process = mock(Process.class);
        final RedSessionConsole console = new RedSessionConsole("console", process);

        assertThat(console.getName()).isEqualTo("console");
        assertThat(console.getImageDescriptor()).isEqualTo(RedImages.getRobotImage());
        assertThat(console.getProcess()).isSameAs(process);
        
        assertThat(console.getStdOutStream()).isNull();
        assertThat(console.getStdErrStream()).isNull();
    }
    
    @Test
    public void initializedConsoleHasUsualOutStreamAndActivatingErrStreamInRed() {
        final RedSessionConsole console = new RedSessionConsole("console", mock(Process.class));
        console.initializeStreams();

        assertThat(console.getStdOutStream()).isNotNull();
        assertThat(console.getStdOutStream().isActivateOnWrite()).isFalse();
        assertThat(console.getStdOutStream().getColor()).isNull();

        assertThat(console.getStdErrStream()).isNotNull();
        assertThat(console.getStdErrStream().isActivateOnWrite()).isTrue();
        assertThat(console.getStdErrStream().getColor().getRGB()).isEqualTo(new RGB(255, 0, 0));
    }
    
    @Test
    public void itIsPossibleToChangeOnWriteActivationInOutAndErrStreams() {
        final RedSessionConsole console = new RedSessionConsole("console", mock(Process.class));
        console.initializeStreams();

        console.setActivateOnStdOutChange(false);
        console.setActivateOnStdErrChange(false);

        assertThat(console.isActivatingOnStdOutChange()).isFalse();
        assertThat(console.isActivatingOnStdErrChange()).isFalse();

        console.setActivateOnStdOutChange(true);
        console.setActivateOnStdErrChange(true);

        assertThat(console.isActivatingOnStdOutChange()).isTrue();
        assertThat(console.isActivatingOnStdErrChange()).isTrue();
    }

    @Test
    public void consoleNameChanges_whenProcessIsSaidToBeTerminated() {
        final RedSessionConsole console = new RedSessionConsole("console", mock(Process.class));

        assertThat(console.getName()).isEqualTo("console");
        console.processTerminated();
        assertThat(console.getName()).isEqualTo("<terminated> console");
    }

    @Test
    public void consoleIsNotTerminatedAsLongAsProcessIsAlive() {
        final Process process = mock(Process.class);
        when(process.isAlive()).thenReturn(true, true, false);

        final RedSessionConsole console = new RedSessionConsole("console", process);
        assertThat(console.isTerminated()).isFalse();
        assertThat(console.isTerminated()).isFalse();
        assertThat(console.isTerminated()).isTrue();
    }

}
