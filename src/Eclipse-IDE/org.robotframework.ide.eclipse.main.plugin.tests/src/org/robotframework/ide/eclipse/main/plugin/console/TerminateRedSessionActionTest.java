package org.robotframework.ide.eclipse.main.plugin.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

public class TerminateRedSessionActionTest {

    @Test
    public void whenCreatedTheActionIsEnabledHasProperNameAndIconsSet() {
        final TerminateRedSessionAction action = new TerminateRedSessionAction(mock(RedSessionConsole.class));

        assertThat(action.isEnabled()).isTrue();
        assertThat(action.getText()).isEqualTo("Terminate");
        assertThat(action.getImageDescriptor()).isEqualTo(RedImages.getStopImage());
    }
}
