/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.red.nattable.configs;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorSite;


public class TableMenuConfiguration extends AbstractUiBindingConfiguration {

    private final Menu menu;

    public TableMenuConfiguration(final IEditorSite site, final NatTable table,
            final ISelectionProvider selectionProvider, final String menuId, final String menuText) {
        final MenuManager manager = new MenuManager(menuText, menuId);
        this.menu = manager.createContextMenu(table);
        table.setMenu(menu);

        site.registerContextMenu(menuId, manager, selectionProvider, false);
    }

    @Override
    public void configureUiBindings(final UiBindingRegistry uiBindingRegistry) {
        uiBindingRegistry.registerMouseDownBinding(new MouseEventMatcher(SWT.NONE, null, 3), new PopupMenuAction(menu));
    }

}
