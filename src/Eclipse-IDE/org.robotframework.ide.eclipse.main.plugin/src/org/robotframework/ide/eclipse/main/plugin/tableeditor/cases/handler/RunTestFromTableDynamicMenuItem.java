/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.RunTestDynamicMenuItem;

public class RunTestFromTableDynamicMenuItem extends RunTestDynamicMenuItem {

    private static final String RUN_TEST_COMMAND_ID = "org.robotframework.red.runSingleTestFromTable";

    static final String RUN_TEST_COMMAND_MODE_PARAMETER = RUN_TEST_COMMAND_ID + ".mode";

    private final String id;

    public RunTestFromTableDynamicMenuItem() {
        this("org.robotframework.red.menu.dynamic.table.run");
    }

    public RunTestFromTableDynamicMenuItem(final String id) {
        this.id = id;
    }
    
    @Override
    protected IContributionItem[] getContributionItems() {
        final IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (!(activeWindow.getActivePage().getActiveEditor() instanceof RobotFormEditor)) {
            return new IContributionItem[0];
        }
        final List<IContributionItem> contributedItems = new ArrayList<>();
        final ISelection selection = activeWindow.getSelectionService().getSelection();
        if (selection instanceof StructuredSelection && !selection.isEmpty()) {
            final StructuredSelection structuredSelection = (StructuredSelection) selection;

            RobotCase testCase = null;
            Object firstElement = structuredSelection.getFirstElement();
            if (firstElement instanceof RobotKeywordCall) {
                firstElement = ((RobotKeywordCall) firstElement).getParent();
            }
            if (firstElement instanceof RobotCase) {
                testCase = (RobotCase) firstElement;
            }

            if (testCase != null) {
                contributeBefore(contributedItems);
                contributedItems.add(createCurrentCaseItem(activeWindow, testCase));
            }
        }
        return contributedItems.toArray(new IContributionItem[0]);
    }

    protected IContributionItem createCurrentCaseItem(final IServiceLocator serviceLocator, final RobotCase testCase) {
        final CommandContributionItemParameter contributionParameters = new CommandContributionItemParameter(
                serviceLocator, id, RUN_TEST_COMMAND_ID, SWT.PUSH);
        contributionParameters.label = getModeName() + " test: '" + testCase.getName() + "'";
        contributionParameters.icon = getImageDescriptor();
        final HashMap<String, String> parameters = new HashMap<>();
        parameters.put(RUN_TEST_COMMAND_MODE_PARAMETER, getModeName().toUpperCase());
        contributionParameters.parameters = parameters;
        return new CommandContributionItem(contributionParameters);
    }

}