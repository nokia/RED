/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.eclipse.nebula.widgets.nattable.resize.action.AutoResizeRowAction;
import org.eclipse.nebula.widgets.nattable.resize.action.RowResizeCursorAction;
import org.eclipse.nebula.widgets.nattable.resize.event.RowResizeEventMatcher;
import org.eclipse.nebula.widgets.nattable.resize.mode.RowResizeDragMode;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.junit.Test;

public class RedTableResizableRowsBindingsConfigurationTest {

    @Test
    public void configurationCheck() {
        final UiBindingRegistry configRegistry = mock(UiBindingRegistry.class);

        final RedTableResizableRowsBindingsConfiguration configuration = new RedTableResizableRowsBindingsConfiguration();
        configuration.configureUiBindings(configRegistry);

        verify(configRegistry).registerFirstMouseMoveBinding(isA(RowResizeEventMatcher.class),
                isA(RowResizeCursorAction.class));
        verify(configRegistry).registerFirstMouseDragMode(isA(RowResizeEventMatcher.class),
                isA(RowResizeDragMode.class));
        verify(configRegistry).registerDoubleClickBinding(isA(RowResizeEventMatcher.class),
                isA(AutoResizeRowAction.class));
        verifyNoMoreInteractions(configRegistry);
    }
}
