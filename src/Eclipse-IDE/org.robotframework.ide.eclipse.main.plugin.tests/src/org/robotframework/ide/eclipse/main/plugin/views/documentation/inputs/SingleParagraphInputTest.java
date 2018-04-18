package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.eclipse.ui.IWorkbenchPage;
import org.junit.Test;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.libraries.Documentation.DocFormat;


public class SingleParagraphInputTest {

    @Test
    public void singleParagraphInputDoesNotContainAnything() {
        final Supplier<String> supplier = () -> "paragraph";
        final SingleParagraphInput input = new SingleParagraphInput(supplier);

        assertThat(input.contains(new Object())).isFalse();
        assertThat(input.contains(supplier)).isFalse();
        assertThat(input.contains("paragraph")).isFalse();
    }

    @Test
    public void illegalStateExceptionIsThrown_whenTryingToGetInputUri() {
        final SingleParagraphInput input = new SingleParagraphInput(() -> "paragraph");
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> input.getInputUri());
    }

    @Test
    public void illegalStateExceptionIsThrown_whenTryingToShowInput() {
        final SingleParagraphInput input = new SingleParagraphInput(() -> "paragraph");
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> input.showInput(mock(IWorkbenchPage.class)));
    }

    @Test
    public void illegalStateExceptionIsThrown_whenTryingToGenerateHtmlLibdoc() {
        final SingleParagraphInput input = new SingleParagraphInput(() -> "paragraph");
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> input.generateHtmlLibdoc());
    }

    @Test
    public void theSuppliedParagraphIsProperlyHtmlified() {
        final RobotRuntimeEnvironment env = mock(RobotRuntimeEnvironment.class);
        when(env.createHtmlDoc(any(String.class), eq(DocFormat.ROBOT))).thenReturn("paragraph");

        final SingleParagraphInput input = new SingleParagraphInput(() -> "paragraph");
        final String html = input.provideHtml(env);

        assertThat(html).isNotEqualTo("paragraph");
        assertThat(html).contains("<html>");
        assertThat(html).contains("<body>");
        assertThat(html).contains("paragraph");
    }

    @Test
    public void rawTextIsReturnedByTheGivenSupplier() {
        final SingleParagraphInput input = new SingleParagraphInput(() -> "paragraph");
        assertThat(input.provideRawText()).isEqualTo("paragraph");
    }
}
