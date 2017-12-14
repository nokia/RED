package org.robotframework.ide.eclipse.main.plugin.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.ui.console.MessageConsoleStream;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

public class ActivateOnInputChangeActionTest {

    @Test
    public void whenCreatedTheActionIsEnabledHasGivenIconProperNameAndIsCheckedIfStreamIsActivating() {
        final MessageConsoleStream stream = mock(MessageConsoleStream.class);
        when(stream.isActivateOnWrite()).thenReturn(true);

        final ActivateOnInputChangeAction action = new ActivateOnInputChangeAction(stream, "stream",
                RedImages.getElementImage());

        assertThat(action.isEnabled()).isTrue();
        assertThat(action.isChecked()).isTrue();
        assertThat(action.getText()).isEqualTo("Activate console when stream changes");
        assertThat(action.getImageDescriptor()).isEqualTo(RedImages.getElementImage());
    }

    @Test
    public void whenCreatedTheActionIsEnabledHasGivenIconProperNameAndIsNotCheckedIfStreamIsNotActivating() {
        final MessageConsoleStream stream = mock(MessageConsoleStream.class);
        when(stream.isActivateOnWrite()).thenReturn(false);

        final ActivateOnInputChangeAction action = new ActivateOnInputChangeAction(stream, "stream",
                RedImages.getElementImage());

        assertThat(action.isEnabled()).isTrue();
        assertThat(action.isChecked()).isFalse();
        assertThat(action.getText()).isEqualTo("Activate console when stream changes");
        assertThat(action.getImageDescriptor()).isEqualTo(RedImages.getElementImage());
    }

    @Test
    public void whenActionIsCheckedTheStreamIsSetToBeActivating() {
        final MessageConsoleStream stream = mock(MessageConsoleStream.class);

        final ActivateOnInputChangeAction action = new ActivateOnInputChangeAction(stream, "stream",
                RedImages.getElementImage());
        action.setChecked(true);
        action.run();

        verify(stream).setActivateOnWrite(true);
    }

    @Test
    public void whenActionIsNotCheckedTheStreamIsSetToBeNonActivating() {
        final MessageConsoleStream stream = mock(MessageConsoleStream.class);

        final ActivateOnInputChangeAction action = new ActivateOnInputChangeAction(stream, "stream",
                RedImages.getElementImage());
        action.setChecked(false);
        action.run();

        verify(stream).setActivateOnWrite(false);
    }
}
