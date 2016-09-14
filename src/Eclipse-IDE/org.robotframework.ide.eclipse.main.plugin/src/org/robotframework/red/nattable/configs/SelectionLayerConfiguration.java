/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.selection.action.SelectCellAction;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionBindings;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.action.NoOpMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.IMouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;

/**
 * @author Michal Anglart
 *
 */
public class SelectionLayerConfiguration extends DefaultSelectionLayerConfiguration {

    @Override
    protected void addSelectionUIBindings() {
        addConfiguration(new SelectionBindings());
    }

    private static class SelectionBindings extends DefaultSelectionBindings {

        @Override
        public void configureUiBindings(final UiBindingRegistry uiBindingRegistry) {
            super.configureUiBindings(uiBindingRegistry);

            // we're having handlers for cut/copy/paste; we don't want this
            // action to run when our handler is not enabled
            uiBindingRegistry.unregisterKeyBinding(new KeyEventMatcher(SWT.MOD1, 'c'));
        }

        @Override
        protected void configureBodyMouseClickBindings(final UiBindingRegistry uiBindingRegistry) {
            final IMouseAction action = new SelectCellAction();
            uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), action);
            uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyLeftClick(SWT.MOD2), action);
            uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyLeftClick(SWT.MOD1), action);
            uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyLeftClick(SWT.MOD2 | SWT.MOD1), action);

            // following alters default behavior: when user clicks with RMP the selection will also
            // be set (if it was not clicked on already selected cell)
            uiBindingRegistry.registerMouseDownBinding(bodyRightClickOnSelected(SWT.NONE), new NoOpMouseAction());
            uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyRightClick(SWT.NONE), action);
            uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyRightClick(SWT.MOD2), action);
            uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyRightClick(SWT.MOD1), action);
            uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyRightClick(SWT.MOD2 | SWT.MOD1), action);
        }
    }

    private static IMouseEventMatcher bodyRightClickOnSelected(final int mask) {
        return new CellSelectedMouseEventMatcher(mask, GridRegion.BODY, MouseEventMatcher.RIGHT_BUTTON);
    }

    private static class CellSelectedMouseEventMatcher extends MouseEventMatcher {

        public CellSelectedMouseEventMatcher(final int stateMask, final String regionName,
                final int button) {
            super(stateMask, regionName, button);
        }

        @Override
        public boolean matches(final NatTable natTable, final MouseEvent event, final LabelStack regionLabels) {
            final NatEventData eventData = NatEventData.createInstanceFromEvent(event);
            final String displayMode = natTable.getDisplayModeByPosition(eventData.getColumnPosition(),
                    eventData.getRowPosition());

            return super.matches(natTable, event, regionLabels)
                    && (displayMode.equals(DisplayMode.SELECT) || displayMode.equals(DisplayMode.SELECT_HOVER));
        }
    }
}
