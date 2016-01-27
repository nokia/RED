package org.robotframework.red.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.actions.QuickMenuCreator;

public class QuickMenuRegistrator extends QuickMenuCreator {

    private final IEditorSite site;

    private final ISelectionProvider selectionProvider;

    private final String menuId;

    public QuickMenuRegistrator(final IEditorSite site, final ISelectionProvider selectionProvider, final String menuId) {
        this.site = site;
        this.selectionProvider = selectionProvider;
        this.menuId = menuId;
    }

    @Override
    protected void fillMenu(final IMenuManager menu) {
        menu.setRemoveAllWhenShown(true);
        site.registerContextMenu(menuId, (MenuManager) menu, selectionProvider, false);
    }
}
