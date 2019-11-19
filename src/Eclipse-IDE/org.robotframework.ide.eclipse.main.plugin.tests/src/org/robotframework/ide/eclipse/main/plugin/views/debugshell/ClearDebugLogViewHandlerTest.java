/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.views.debugshell.ClearDebugLogViewHandler.E4ClearDebugLogViewHandler;

public class ClearDebugLogViewHandlerTest {

    @Test
    public void handlerCallsClearOnViewComponent() {
        final DebugShellView view = mock(DebugShellView.class);
        final DebugShellViewWrapper viewWrapper = mock(DebugShellViewWrapper.class);
        when(viewWrapper.getView()).thenReturn(view);

        final E4ClearDebugLogViewHandler handler = new E4ClearDebugLogViewHandler();

        handler.clear(viewWrapper);

        verify(view).clear();
    }

}
