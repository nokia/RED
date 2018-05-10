/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.function.Consumer;

import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.widgets.Widget;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationsLinksSupport.UnableToOpenUriException;

public class DocumentationsLinksListenerTest {

    @Test
    public void exceptionIsThrown_whenLocationHasUriSyntaxErrors() {
        final DocumentationsLinksSupport linksSupport = mock(DocumentationsLinksSupport.class);
        @SuppressWarnings("unchecked")
        final Consumer<UnableToOpenUriException> exceptionHandler = mock(Consumer.class);
        final DocumentationsLinksListener listener = new DocumentationsLinksListener(linksSupport, exceptionHandler);
        final LocationEvent event = newLocationEvent("file:/path with spaces");
        listener.changing(event);

        verify(exceptionHandler)
                .accept(argThat(isExceptionWithMessage("Syntax error in uri 'file:/path with spaces'")));
        verifyZeroInteractions(linksSupport);
        assertThat(event.doit).isFalse();
    }

    @Test
    public void linksSupportHandlesLocationAndDisablesNavigation_whenLocationIsValidAndSupported() {
        final DocumentationsLinksSupport linksSupport = mock(DocumentationsLinksSupport.class);
        when(linksSupport.changeLocationTo(any(URI.class))).thenReturn(true);

        final DocumentationsLinksListener listener = new DocumentationsLinksListener(linksSupport);
        final LocationEvent event = newLocationEvent("file:/path");
        listener.changing(event);

        verify(linksSupport).changeLocationTo(URI.create("file:/path"));
        assertThat(event.doit).isFalse();
    }

    @Test
    public void linksSupportDoesNotHandleLocationAndEnablesNavigation_whenLocationIsValidAndNotSupported() {
        final DocumentationsLinksSupport linksSupport = mock(DocumentationsLinksSupport.class);
        when(linksSupport.changeLocationTo(any(URI.class))).thenReturn(false);

        final DocumentationsLinksListener listener = new DocumentationsLinksListener(linksSupport);
        final LocationEvent event = newLocationEvent("file:/path");
        listener.changing(event);

        verify(linksSupport).changeLocationTo(URI.create("file:/path"));
        assertThat(event.doit).isTrue();
    }

    @Test
    public void nothingHappens_whenLocationChanged() {
        final DocumentationsLinksSupport linksSupport = mock(DocumentationsLinksSupport.class);
        final LocationEvent event = mock(LocationEvent.class);

        final DocumentationsLinksListener listener = new DocumentationsLinksListener(linksSupport);

        listener.changed(event);

        verifyZeroInteractions(linksSupport, event);
    }

    private static <E extends Exception> ArgumentMatcher<E> isExceptionWithMessage(final String message) {
        return exception -> exception.getMessage().equals(message);
    }

    private static LocationEvent newLocationEvent(final String location) {
        final LocationEvent event = new LocationEvent(mock(Widget.class));
        event.location = location;
        return event;
    }
}
