/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.variables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;


public class ActivateStringVariablesMenuItem extends CompoundContributionItem {

    private static final String ACTIVATE_SET_COMMAND_ID = "org.robotframework.red.activateStringVariablesSet";

    static final String ACTIVATE_SET_COMMAND_PARAMETER_ID = ACTIVATE_SET_COMMAND_ID + ".setName";

    public ActivateStringVariablesMenuItem() {
        super("org.robotframework.ide.eclipse.red.activeVariablesMenuDynamic");
    }

    @Override
    protected IContributionItem[] getContributionItems() {
        final IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();

        final List<IContributionItem> sets = new ArrayList<>();
        sets.add(createActivateSetItem(activeWindow, "No active variables set", ""));
        sets.add(new Separator());
        preferences.getOverriddenVariablesSets()
                .keySet()
                .stream()
                .map(setName -> createActivateSetItem(activeWindow, setName, setName))
                .forEach(sets::add);
        return sets.toArray(new IContributionItem[0]);
    }

    private IContributionItem createActivateSetItem(final IServiceLocator serviceLocator, final String label,
            final String setName) {
        final CommandContributionItemParameter contributionParameters = new CommandContributionItemParameter(
                serviceLocator, getId(), ACTIVATE_SET_COMMAND_ID, SWT.CHECK);
        contributionParameters.label = label;

        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(ACTIVATE_SET_COMMAND_PARAMETER_ID, setName);
        contributionParameters.parameters = parameters;
        return new CommandContributionItem(contributionParameters);
    }
}
