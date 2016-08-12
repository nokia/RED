/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static com.google.common.collect.Lists.newArrayList;

import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.edit.action.KeyEditAction;
import org.eclipse.nebula.widgets.nattable.edit.action.MouseEditAction;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.resize.action.AutoResizeColumnAction;
import org.eclipse.nebula.widgets.nattable.resize.action.ColumnResizeCursorAction;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEventMatcher;
import org.eclipse.nebula.widgets.nattable.resize.mode.ColumnResizeDragMode;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellEditorMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.LetterOrDigitKeyEventMatcher;
import org.eclipse.swt.SWT;


/**
 * @author Michal Anglart
 *
 */
public class RedTableEditBindingsConfiguration extends AbstractUiBindingConfiguration {

    @Override
    public void configureUiBindings(final UiBindingRegistry uiBindingRegistry) {
        uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, ' '), new KeyEditAction());
        uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.F2), new KeyEditAction());
        uiBindingRegistry.registerFirstKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.CR), new KeyEditAction());
        uiBindingRegistry.registerFirstKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.CR), new KeyEditAction());
        uiBindingRegistry.registerFirstKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.KEYPAD_CR), new KeyEditAction());
        uiBindingRegistry.registerKeyBinding(new LetterOrDigitKeyEventMatcher(), new KeyEditAction());
        uiBindingRegistry.registerKeyBinding(new LetterOrDigitKeyEventMatcher(SWT.SHIFT), new KeyEditAction());

        uiBindingRegistry.registerFirstMouseDownBinding(new TableCellEditorMouseEventMatcher(GridRegion.BODY,
                newArrayList(DisplayMode.SELECT, DisplayMode.SELECT_HOVER)), new MouseEditAction());
        uiBindingRegistry.registerDoubleClickBinding(new CellEditorMouseEventMatcher(GridRegion.BODY),
                new MouseEditAction());
        
        uiBindingRegistry.registerFirstMouseMoveBinding(
                new ColumnResizeEventMatcher(SWT.NONE, GridRegion.COLUMN_HEADER, 0), new ColumnResizeCursorAction());
        uiBindingRegistry.registerFirstMouseDragMode(
                new ColumnResizeEventMatcher(SWT.NONE, GridRegion.COLUMN_HEADER, 1), new ColumnResizeDragMode());
        
        uiBindingRegistry.registerDoubleClickBinding(new ColumnResizeEventMatcher(SWT.NONE, GridRegion.COLUMN_HEADER, 1),
                new AutoResizeColumnAction());

        // uiBindingRegistry.registerMouseDragMode(new CellEditorMouseEventMatcher(GridRegion.BODY),
        // new CellEditDragMode());
    }
}
