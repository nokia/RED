/*
* Copyright 2018 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

public class RevalidateSelectionDynamicMenuItem extends CompoundContributionItem {

    private static final String REVALIDATE_SELECTION_COMMAND_ID = "org.robotframework.red.revalidateSelection";

    private static final String DYNAMIC_ID = "org.robotframework.red.menu.dynamic.selection.revalidate";

    @Override
    protected IContributionItem[] getContributionItems() {
        final IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        final List<IContributionItem> contributedItems = new ArrayList<>();
        contributedItems.add(createItemByPreference(activeWindow));
        return contributedItems.toArray(new IContributionItem[0]);
    }

    private IContributionItem createItemByPreference(final IServiceLocator serviceLocator) {
        final CommandContributionItemParameter contributionParameters = new CommandContributionItemParameter(
                serviceLocator, DYNAMIC_ID, REVALIDATE_SELECTION_COMMAND_ID, SWT.PUSH);
        contributionParameters.label = "Revalidate"
                + (RedPlugin.getDefault().getPreferences().isValidationTurnedOff()
                        ? " (disabled by preference)"
                        : "");
        return new CommandContributionItem(contributionParameters);
    }

}
