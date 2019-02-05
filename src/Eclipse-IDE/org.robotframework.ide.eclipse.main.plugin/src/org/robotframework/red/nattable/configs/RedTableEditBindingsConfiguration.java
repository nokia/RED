/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.edit.action.KeyEditAction;
import org.eclipse.nebula.widgets.nattable.edit.action.MouseEditAction;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.resize.action.AutoResizeColumnAction;
import org.eclipse.nebula.widgets.nattable.resize.action.ColumnResizeCursorAction;
import org.eclipse.nebula.widgets.nattable.resize.event.ColumnResizeEventMatcher;
import org.eclipse.nebula.widgets.nattable.resize.mode.ColumnResizeDragMode;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.CellEditorMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.IKeyEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;


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
        uiBindingRegistry.registerKeyBinding(new RedKeyEventMatcher(), new RedKeyEditAction());
        uiBindingRegistry.registerKeyBinding(new RedKeyEventMatcher(SWT.SHIFT), new RedKeyEditAction());

        uiBindingRegistry.registerDoubleClickBinding(new CellEditorMouseEventMatcher(GridRegion.BODY),
                new MouseEditAction());

        uiBindingRegistry.registerFirstMouseMoveBinding(
                new ColumnResizeEventMatcher(SWT.NONE, GridRegion.COLUMN_HEADER, 0), new ColumnResizeCursorAction());
        uiBindingRegistry.registerFirstMouseDragMode(
                new ColumnResizeEventMatcher(SWT.NONE, GridRegion.COLUMN_HEADER, 1), new ColumnResizeDragMode());
        uiBindingRegistry.registerDoubleClickBinding(
                new ColumnResizeEventMatcher(SWT.NONE, GridRegion.COLUMN_HEADER, 1), new AutoResizeColumnAction());
    }

    /**
     * This is customized version of
     * org.eclipse.nebula.widgets.nattable.ui.matcher.LetterOrDigitKeyEventMatcher for our needs.
     * All variable identificator triggers are supported.
     */
    static class RedKeyEventMatcher implements IKeyEventMatcher {

        private static final String EDIT_TRIGGERS = "[\\.:,;\\-_#\'+*~!?Â§$@%^&/()\\[\\]\\{\\}=\\\\\"]"; //$NON-NLS-1$

        private final int stateMask;

        public RedKeyEventMatcher() {
            this(SWT.NONE);
        }

        public RedKeyEventMatcher(final int stateMask) {
            this.stateMask = stateMask;
        }

        @Override
        public boolean matches(final KeyEvent event) {
            return event.stateMask == this.stateMask && isEditTrigger(event.character);
        }

        static boolean isEditTrigger(final char character) {
            return Character.isLetterOrDigit(character) || String.valueOf(character).matches(EDIT_TRIGGERS);
        }
    }

    /**
     * This is customized version of
     * org.eclipse.nebula.widgets.nattable.edit.action.KeyEditAction for our needs.
     * All variable identificator triggers are supported.
     */
    static class RedKeyEditAction extends KeyEditAction {

        @Override
        protected Character convertCharToCharacterObject(final KeyEvent event) {
            return RedKeyEventMatcher.isEditTrigger(event.character) ? Character.valueOf(event.character) : null;
        }
    }

}
