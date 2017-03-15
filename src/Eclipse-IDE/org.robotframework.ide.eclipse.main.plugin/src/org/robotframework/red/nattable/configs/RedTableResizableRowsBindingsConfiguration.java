/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.resize.action.AutoResizeRowAction;
import org.eclipse.nebula.widgets.nattable.resize.action.RowResizeCursorAction;
import org.eclipse.nebula.widgets.nattable.resize.event.RowResizeEventMatcher;
import org.eclipse.nebula.widgets.nattable.resize.mode.RowResizeDragMode;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.swt.SWT;


/**
 * @author Michal Anglart
 *
 */
public class RedTableResizableRowsBindingsConfiguration extends AbstractUiBindingConfiguration {

    @Override
    public void configureUiBindings(final UiBindingRegistry uiBindingRegistry) {
        uiBindingRegistry.registerFirstMouseMoveBinding(
                new RowResizeEventMatcher(SWT.NONE, GridRegion.ROW_HEADER, 0), new RowResizeCursorAction());
        uiBindingRegistry.registerFirstMouseDragMode(
                new RowResizeEventMatcher(SWT.NONE, GridRegion.ROW_HEADER, 1), new RowResizeDragMode());
        uiBindingRegistry.registerDoubleClickBinding(
                new RowResizeEventMatcher(SWT.NONE, GridRegion.ROW_HEADER, 1), new AutoResizeRowAction());
    }
}
