/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
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
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.OpenAction;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.RunSelectedTestCasesAction;
import org.robotframework.ide.eclipse.main.plugin.navigator.actions.RunSelectedTestCasesAction.Mode;

public class NavigatorActionsProvider extends CommonActionProvider {

    private ISelectionProvider selectionProvider;

    private OpenAction openAction;
    private RunSelectedTestCasesAction runSelectedTestCasesAction;
    private RunSelectedTestCasesAction debugTestCaseAction;

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
            runSelectedTestCasesAction = new RunSelectedTestCasesAction(selectionProvider, Mode.RUN);
            debugTestCaseAction = new RunSelectedTestCasesAction(selectionProvider, Mode.DEBUG);

            updateElements((IStructuredSelection) selectionProvider.getSelection());
        }
    }

    private void updateElements(IStructuredSelection selection) {
        openAction.updateEnablement(selection);
        runSelectedTestCasesAction.updateEnablement(selection);
        debugTestCaseAction.updateEnablement(selection);
    }

    private ISelectionChangedListener createSelectionListener() {
        return new ISelectionChangedListener() {
            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                updateElements((IStructuredSelection) event.getSelection());
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
    }

    @Override
    public void fillContextMenu(final IMenuManager menu) {
        menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openAction);
        menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, runSelectedTestCasesAction);
        menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, debugTestCaseAction);
    }
}
