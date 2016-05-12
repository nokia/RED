/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.action.MoveSelectionAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.swt.SWT;

/**
 * @author Michal Anglart
 *
 */
public class LeftRightMoveOnEnterConfiguration extends AbstractUiBindingConfiguration {

    @Override
    public void configureUiBindings(final UiBindingRegistry uiBindingRegistry) {
        uiBindingRegistry.registerFirstKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.CR),
                new MoveSelectionAction(MoveDirectionEnum.LEFT, false, false));
        uiBindingRegistry.registerFirstKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.CR),
                new MoveSelectionAction(MoveDirectionEnum.RIGHT));
    }
}