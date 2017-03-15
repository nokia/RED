/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.eclipse.nebula.widgets.nattable.edit.action.KeyEditAction;
import org.eclipse.nebula.widgets.nattable.edit.action.MouseEditAction;
import org.eclipse.nebula.widgets.nattable.resize.action.AutoResizeColumnAction;
import org.eclipse.nebula.widgets.nattable.resize.action.ColumnResizeCursorAction;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEventMatcher;
import org.eclipse.nebula.widgets.nattable.resize.mode.ColumnResizeDragMode;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellEditorMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.LetterOrDigitKeyEventMatcher;
import org.junit.Test;

public class RedTableEditBindingsConfigurationTest {

    @Test
    public void configurationCheck() {
        final UiBindingRegistry configRegistry = mock(UiBindingRegistry.class);

        final RedTableEditBindingsConfiguration configuration = new RedTableEditBindingsConfiguration();
        configuration.configureUiBindings(configRegistry);

        verify(configRegistry, times(2)).registerKeyBinding(isA(KeyEventMatcher.class), isA(KeyEditAction.class));
        verify(configRegistry, times(3)).registerFirstKeyBinding(isA(KeyEventMatcher.class), isA(KeyEditAction.class));
        verify(configRegistry, times(2)).registerKeyBinding(isA(LetterOrDigitKeyEventMatcher.class),
                isA(KeyEditAction.class));

        verify(configRegistry).registerDoubleClickBinding(isA(CellEditorMouseEventMatcher.class),
                isA(MouseEditAction.class));

        verify(configRegistry).registerFirstMouseMoveBinding(isA(ColumnResizeEventMatcher.class),
                isA(ColumnResizeCursorAction.class));
        verify(configRegistry).registerFirstMouseDragMode(isA(ColumnResizeEventMatcher.class),
                isA(ColumnResizeDragMode.class));
        verify(configRegistry).registerDoubleClickBinding(isA(ColumnResizeEventMatcher.class),
                isA(AutoResizeColumnAction.class));
        verifyNoMoreInteractions(configRegistry);
    }

}
