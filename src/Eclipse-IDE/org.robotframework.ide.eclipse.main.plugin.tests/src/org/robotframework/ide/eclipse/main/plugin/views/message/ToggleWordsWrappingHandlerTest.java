/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.message;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.views.message.ToggleWordsWrappingHandler.E4ToggleWordsWrappingHandler;

public class ToggleWordsWrappingHandlerTest {

    private final E4ToggleWordsWrappingHandler handler = new E4ToggleWordsWrappingHandler();

    @SuppressWarnings("restriction")
    @Test
    public void handlerTogglesWordWrappingInMessageLogView() {
        final MessageLogViewWrapper logViewWrapper = mock(MessageLogViewWrapper.class);
        final MessageLogView logView = mock(MessageLogView.class);
        when(logViewWrapper.getComponent()).thenReturn(logView);

        handler.wrapWords(logViewWrapper);
        verify(logView).toggleWordsWrapping();
    }

}
