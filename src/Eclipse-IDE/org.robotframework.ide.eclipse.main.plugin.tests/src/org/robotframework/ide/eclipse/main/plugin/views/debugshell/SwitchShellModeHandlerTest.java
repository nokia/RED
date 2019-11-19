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
import org.robotframework.ide.eclipse.main.plugin.views.debugshell.SwitchShellModeHandler.E4SwitchShellModeHandler;

public class SwitchShellModeHandlerTest {

    @Test
    public void handlerCallsSwitchOnViewComponent() {
        final DebugShellView view = mock(DebugShellView.class);
        final DebugShellViewWrapper viewWrapper = mock(DebugShellViewWrapper.class);
        when(viewWrapper.getView()).thenReturn(view);

        final E4SwitchShellModeHandler handler = new E4SwitchShellModeHandler();

        handler.switchMode(viewWrapper);

        verify(view).switchToNextMode();
    }
}
