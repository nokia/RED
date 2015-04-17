package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.DeleteRobotElementAction;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.OpenAction;

public class NavigatorActionsProvider extends CommonActionProvider {

    private ISelectionProvider selectionProvider;

    private OpenAction openAction;
    private DeleteRobotElementAction deleteAction;

    private ISelectionChangedListener listener;

    @Override
    public void init(final ICommonActionExtensionSite site) {
        final ICommonViewerSite viewSite = site.getViewSite();
        if (viewSite instanceof ICommonViewerWorkbenchSite) {
            final ICommonViewerWorkbenchSite workbenchSite = (ICommonViewerWorkbenchSite) viewSite;

            listener = createSelectionListener();
            selectionProvider = workbenchSite.getSelectionProvider();
            selectionProvider.addSelectionChangedListener(listener);

            openAction = new OpenAction(workbenchSite.getPage(), selectionProvider);
            deleteAction = new DeleteRobotElementAction(workbenchSite.getPage(), selectionProvider);
        }
    }

    private ISelectionChangedListener createSelectionListener() {
        return new ISelectionChangedListener() {
            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                openAction.updateEnablement((IStructuredSelection) event.getSelection());
                deleteAction.updateEnablement((IStructuredSelection) event.getSelection());
            }
        };
    }

    @Override
    public void dispose() {
        super.dispose();
        selectionProvider.removeSelectionChangedListener(listener);
    }

    @Override
    public void fillActionBars(final IActionBars actionBars) {
        actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openAction);
        actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteAction);
    }

    @Override
    public void fillContextMenu(final IMenuManager menu) {
        menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openAction);
        menu.appendToGroup(ICommonMenuConstants.GROUP_EDIT, deleteAction);
    }
}
