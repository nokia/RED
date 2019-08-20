/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.handlers;

import java.util.Optional;

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
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.RedXmlLibrary;
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

            final Optional<RedXmlLibrary> lib = Selections.getOptionalFirstElement(structuredSelection,
                    RedXmlLibrary.class);
            return new IContributionItem[] {
                    createMenuItem(activeWindow, lib.map(RedXmlLibrary::isDynamic).orElse(false)) };
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
