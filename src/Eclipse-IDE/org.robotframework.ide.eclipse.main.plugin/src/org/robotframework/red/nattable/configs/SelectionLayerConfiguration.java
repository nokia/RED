/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.selection.action.SelectCellAction;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionBindings;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.swt.SWT;

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
        protected void configureBodyMouseClickBindings(final UiBindingRegistry uiBindingRegistry) {
            final IMouseAction action = new SelectCellAction();
            uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), action);
            uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyLeftClick(SWT.MOD2), action);
            uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyLeftClick(SWT.MOD1), action);
            uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyLeftClick(SWT.MOD2 | SWT.MOD1), action);

            // following alters default behavior: when user clicks with RMP the selection will also
            // be set
            uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyRightClick(SWT.NONE), action);
            uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyRightClick(SWT.MOD2), action);
            uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyRightClick(SWT.MOD1), action);
            uiBindingRegistry.registerMouseDownBinding(MouseEventMatcher.bodyRightClick(SWT.MOD2 | SWT.MOD1), action);
        }
    }
}
