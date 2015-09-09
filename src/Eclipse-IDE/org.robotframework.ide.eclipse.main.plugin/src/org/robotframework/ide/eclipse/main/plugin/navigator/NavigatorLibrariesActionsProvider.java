/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.ShowLibraryDocumentationAction;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.ShowLibrarySourceAction;

public class NavigatorLibrariesActionsProvider extends CommonActionProvider {

    private ISelectionProvider selectionProvider;

    private ISelectionChangedListener listener;

    private ShowLibrarySourceAction showLibrarySourceAction;

    private ShowLibraryDocumentationAction showDocumentationAction;

    @Override
    public void init(final ICommonActionExtensionSite site) {
        final ICommonViewerSite viewSite = site.getViewSite();
        if (viewSite instanceof ICommonViewerWorkbenchSite) {
            final ICommonViewerWorkbenchSite workbenchSite = (ICommonViewerWorkbenchSite) viewSite;

            listener = createSelectionListener();
            selectionProvider = workbenchSite.getSelectionProvider();
            selectionProvider.addSelectionChangedListener(listener);

            showDocumentationAction = new ShowLibraryDocumentationAction(workbenchSite.getPage(), selectionProvider);
            showLibrarySourceAction = new ShowLibrarySourceAction(workbenchSite.getPage(), selectionProvider);
        }
    }

    private ISelectionChangedListener createSelectionListener() {
        return new ISelectionChangedListener() {
            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                showDocumentationAction.updateEnablement((IStructuredSelection) event.getSelection());
                showLibrarySourceAction.updateEnablement((IStructuredSelection) event.getSelection());
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
        actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, showDocumentationAction);
    }

    @Override
    public void fillContextMenu(final IMenuManager menu) {
        menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, showDocumentationAction);
        menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, showLibrarySourceAction);
    }
}
