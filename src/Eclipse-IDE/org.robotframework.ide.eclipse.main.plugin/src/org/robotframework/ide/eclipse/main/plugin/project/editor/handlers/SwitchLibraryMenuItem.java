/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.handlers;

import java.util.function.Predicate;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibrariesContentProvider.RemoteLibraryViewItem;
import org.robotframework.red.viewers.Selections;

import com.google.common.annotations.VisibleForTesting;

public class SwitchLibraryMenuItem extends CompoundContributionItem {

    private static final String SWITCH_LIB_COMMAND_ID = "org.robotframework.red.switchLibraryMode";

    private static final String DYNAMIC_ID = "org.robotframework.red.redxml.dynamic.switchLibrary";

    @Override
    protected IContributionItem[] getContributionItems() {
        return getContributionItems(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
    }

    @VisibleForTesting
    IContributionItem[] getContributionItems(final IWorkbenchWindow activeWindow) {
        final ISelection selection = activeWindow.getSelectionService().getSelection();

        if (selection instanceof IStructuredSelection) {
            final IStructuredSelection structuredSelection = (IStructuredSelection) selection;

            final Predicate<Object> isLib = elem -> elem instanceof RemoteLibraryViewItem || elem instanceof ReferencedLibrary;
            final boolean isDynamic = Selections.getOptionalFirstElement(structuredSelection, isLib)
                    .map(o -> o instanceof RemoteLibraryViewItem ? true : ((ReferencedLibrary) o).isDynamic())
                    .orElse(false);
            
            return new IContributionItem[] { createMenuItem(activeWindow, isDynamic) };
        }
        return new IContributionItem[] { createMenuItem(activeWindow, false) };
    }

    private IContributionItem createMenuItem(final IServiceLocator serviceLocator, final boolean isDynamic) {
        final CommandContributionItemParameter contributionParameters = new CommandContributionItemParameter(
                serviceLocator, DYNAMIC_ID, SWITCH_LIB_COMMAND_ID, SWT.PUSH);

        contributionParameters.serviceLocator = serviceLocator;
        contributionParameters.label = "Mark as " + (isDynamic ? "static" : "dynamic") + " library";
        return new CommandContributionItem(contributionParameters);
    }

}
