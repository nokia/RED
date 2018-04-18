package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationViewInput;


public class DocumentationsBrowsingHistoryTest {

    @Test
    public void backwardIsDisabled_forNewlyCreatedHistory() {
        final DocumentationsLinksSupport linksSupport = mock(DocumentationsLinksSupport.class);
        final DocumentationsBrowsingHistory history = new DocumentationsBrowsingHistory(linksSupport);

        assertThat(history.isBackEnabled()).isFalse();
    }

    @Test
    public void backwardIsDisabled_whenSingleInputWasOpened() throws URISyntaxException {
        final DocumentationViewInput input = mock(DocumentationViewInput.class);
        when(input.getInputUri()).thenReturn(new URI("input:/destination"));

        final DocumentationsLinksSupport linksSupport = mock(DocumentationsLinksSupport.class);
        final DocumentationsBrowsingHistory history = new DocumentationsBrowsingHistory(linksSupport);

        history.newInput(input);

        assertThat(history.isBackEnabled()).isFalse();
    }

    @Test
    public void backwardIsEnabled_whenMultipleInputsWereOpened() throws URISyntaxException {
        final DocumentationViewInput input1 = mock(DocumentationViewInput.class);
        when(input1.getInputUri()).thenReturn(new URI("input:/destination"));
        final DocumentationViewInput input2 = mock(DocumentationViewInput.class);
        when(input2.getInputUri()).thenReturn(new URI("input:/other_destination"));

        final DocumentationsLinksSupport linksSupport = mock(DocumentationsLinksSupport.class);
        final DocumentationsBrowsingHistory history = new DocumentationsBrowsingHistory(linksSupport);

        history.newInput(input1);
        history.newInput(input2);

        assertThat(history.isBackEnabled()).isTrue();
    }

    @Test
    public void backwardIsDisabled_whenMultipleInputsWereOpenedButWeMovedBack() throws URISyntaxException {
        final DocumentationViewInput input1 = mock(DocumentationViewInput.class);
        when(input1.getInputUri()).thenReturn(new URI("input:/destination"));
        final DocumentationViewInput input2 = mock(DocumentationViewInput.class);
        when(input2.getInputUri()).thenReturn(new URI("input:/other_destination"));

        final DocumentationsLinksSupport linksSupport = mock(DocumentationsLinksSupport.class);
        final DocumentationsBrowsingHistory history = new DocumentationsBrowsingHistory(linksSupport);

        history.newInput(input1);
        history.newInput(input2);
        history.back();

        assertThat(history.isBackEnabled()).isFalse();
    }

    @Test
    public void forwardIsDisabled_forNewlyCreatedHistory() {
        final DocumentationsLinksSupport linksSupport = mock(DocumentationsLinksSupport.class);
        final DocumentationsBrowsingHistory history = new DocumentationsBrowsingHistory(linksSupport);

        assertThat(history.isForwardEnabled()).isFalse();
    }

    @Test
    public void forwardIsDisabled_whenMutipleInputsWereOpened() throws URISyntaxException {
        final DocumentationViewInput input1 = mock(DocumentationViewInput.class);
        when(input1.getInputUri()).thenReturn(new URI("input:/destination"));
        final DocumentationViewInput input2 = mock(DocumentationViewInput.class);
        when(input2.getInputUri()).thenReturn(new URI("input:/other_destination"));
        final DocumentationViewInput input3 = mock(DocumentationViewInput.class);
        when(input3.getInputUri()).thenReturn(new URI("input:/totally_different_destination"));

        final DocumentationsLinksSupport linksSupport = mock(DocumentationsLinksSupport.class);
        final DocumentationsBrowsingHistory history = new DocumentationsBrowsingHistory(linksSupport);

        history.newInput(input1);
        assertThat(history.isForwardEnabled()).isFalse();

        history.newInput(input2);
        assertThat(history.isForwardEnabled()).isFalse();

        history.newInput(input3);
        assertThat(history.isForwardEnabled()).isFalse();
    }

    @Test
    public void forwardIsEnabled_whenMutlipleInputsWereOpenedAndWeMovedBack() throws URISyntaxException {
        final DocumentationViewInput input1 = mock(DocumentationViewInput.class);
        when(input1.getInputUri()).thenReturn(new URI("input:/destination"));
        final DocumentationViewInput input2 = mock(DocumentationViewInput.class);
        when(input2.getInputUri()).thenReturn(new URI("input:/other_destination"));

        final DocumentationsLinksSupport linksSupport = mock(DocumentationsLinksSupport.class);
        final DocumentationsBrowsingHistory history = new DocumentationsBrowsingHistory(linksSupport);

        history.newInput(input1);
        history.newInput(input2);

        history.back();

        assertThat(history.isForwardEnabled()).isTrue();
    }

    @Test
    public void historyDoesNotRegisterMovesToSameUriAsTheCurrentOne() throws URISyntaxException {
        final DocumentationViewInput input1 = mock(DocumentationViewInput.class);
        when(input1.getInputUri()).thenReturn(new URI("input:/destination"));
        final DocumentationViewInput input2 = mock(DocumentationViewInput.class);
        when(input2.getInputUri()).thenReturn(new URI("input:/destination"));

        final DocumentationsLinksSupport linksSupport = mock(DocumentationsLinksSupport.class);
        final DocumentationsBrowsingHistory history = new DocumentationsBrowsingHistory(linksSupport);

        history.newInput(input1);
        assertThat(history.getHistory()).hasSize(1);

        history.newInput(input2);
        assertThat(history.getHistory()).hasSize(1);
    }

    @Test
    public void forwardMovesAreCleared_whenMovingBackAndOpeningNewInput() throws URISyntaxException {
        final DocumentationViewInput input1 = mock(DocumentationViewInput.class);
        when(input1.getInputUri()).thenReturn(new URI("input:/destination"));
        final DocumentationViewInput input2 = mock(DocumentationViewInput.class);
        when(input2.getInputUri()).thenReturn(new URI("input:/other_destination"));
        final DocumentationViewInput input3 = mock(DocumentationViewInput.class);
        when(input3.getInputUri()).thenReturn(new URI("input:/totally_different_destination"));
        final DocumentationViewInput input4 = mock(DocumentationViewInput.class);
        when(input4.getInputUri()).thenReturn(new URI("input:/and_some_new_destination"));

        final DocumentationsLinksSupport linksSupport = mock(DocumentationsLinksSupport.class);
        final DocumentationsBrowsingHistory history = new DocumentationsBrowsingHistory(linksSupport);

        history.newInput(input1);
        history.newInput(input2);
        history.newInput(input3);
        assertThat(history.getHistory()).hasSize(3);

        history.back();
        history.back();
        assertThat(history.getHistory()).hasSize(3);

        history.newInput(input4);
        assertThat(history.getHistory()).hasSize(2);
        assertThat(history.getHistory()).containsExactly(new URI("input:/destination"),
                new URI("input:/and_some_new_destination"));
    }

    @Test
    public void linksSupportOpensProperUris_whenMovingForwardAndBackward() throws URISyntaxException {
        final DocumentationViewInput input1 = mock(DocumentationViewInput.class);
        when(input1.getInputUri()).thenReturn(new URI("input:/destination"));
        final DocumentationViewInput input2 = mock(DocumentationViewInput.class);
        when(input2.getInputUri()).thenReturn(new URI("input:/other_destination"));
        final DocumentationViewInput input3 = mock(DocumentationViewInput.class);
        when(input3.getInputUri()).thenReturn(new URI("input:/totally_different_destination"));
        final DocumentationViewInput input4 = mock(DocumentationViewInput.class);
        when(input4.getInputUri()).thenReturn(new URI("input:/and_some_new_destination"));

        final DocumentationsLinksSupport linksSupport = mock(DocumentationsLinksSupport.class);
        final DocumentationsBrowsingHistory history = new DocumentationsBrowsingHistory(linksSupport);

        history.newInput(input1);
        history.newInput(input2);
        history.newInput(input3);
        history.newInput(input4);
        assertThat(history.isBackEnabled()).isTrue();
        assertThat(history.isForwardEnabled()).isFalse();

        history.back();
        history.back();
        history.back();
        assertThat(history.isBackEnabled()).isFalse();
        assertThat(history.isForwardEnabled()).isTrue();

        history.forward();
        assertThat(history.isBackEnabled()).isTrue();
        assertThat(history.isForwardEnabled()).isTrue();

        history.back();
        assertThat(history.isBackEnabled()).isFalse();
        assertThat(history.isForwardEnabled()).isTrue();

        history.forward();
        history.forward();
        assertThat(history.isBackEnabled()).isTrue();
        assertThat(history.isForwardEnabled()).isTrue();

        final InOrder orderVerifier = Mockito.inOrder(linksSupport);
        orderVerifier.verify(linksSupport).changeLocationTo(new URI("input:/totally_different_destination"));
        orderVerifier.verify(linksSupport).changeLocationTo(new URI("input:/other_destination"));
        orderVerifier.verify(linksSupport).changeLocationTo(new URI("input:/destination"));
        orderVerifier.verify(linksSupport).changeLocationTo(new URI("input:/other_destination"));
        orderVerifier.verify(linksSupport).changeLocationTo(new URI("input:/destination"));
        orderVerifier.verify(linksSupport).changeLocationTo(new URI("input:/other_destination"));
        orderVerifier.verify(linksSupport).changeLocationTo(new URI("input:/totally_different_destination"));
        orderVerifier.verifyNoMoreInteractions();
    }

}
