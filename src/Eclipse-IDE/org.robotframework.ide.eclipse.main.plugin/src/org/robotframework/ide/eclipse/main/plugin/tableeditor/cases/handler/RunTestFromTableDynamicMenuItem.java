/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;
import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.red.viewers.Selections;

public class RunTestFromTableDynamicMenuItem extends CompoundContributionItem {

    private static final String RUN_TEST_COMMAND_ID = "org.robotframework.red.runSelectedTestsFromTable";

    static final String RUN_TEST_COMMAND_MODE_PARAMETER = RUN_TEST_COMMAND_ID + ".mode";

    private final String id;

    public RunTestFromTableDynamicMenuItem() {
        this("org.robotframework.red.menu.dynamic.table.run");
    }

    public RunTestFromTableDynamicMenuItem(final String id) {
        this.id = id;
    }

    protected String getModeName() {
        return "Run";
    }

    protected ImageDescriptor getImageDescriptor() {
        return RedImages.getExecuteRunImage();
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

            final Set<RobotCodeHoldingElement<?>> firstCases = findFirstCases(structuredSelection);
            if (!firstCases.isEmpty()) {
                final RobotCodeHoldingElement<?> testCase = firstCases.stream().findFirst().get();

                final String elemTypeLabel = isTask(testCase) ? "task" : "test";
                final String label = firstCases.size() == 1 ? elemTypeLabel + ": '" + testCase.getName() + "'"
                        : "selected " + elemTypeLabel + "s";
                contributeBefore(contributedItems);
                contributedItems.add(createCurrentCaseItem(activeWindow, label));

            }
        }
        return contributedItems.toArray(new IContributionItem[0]);
    }

    protected void contributeBefore(final List<IContributionItem> contributedItems) {
        contributedItems.add(new Separator());
    }

    private Set<RobotCodeHoldingElement<?>> findFirstCases(final IStructuredSelection selection) {
        final List<RobotElement> selected = Selections.getElements(selection, RobotElement.class);

        final Set<RobotCodeHoldingElement<?>> firstCases = new HashSet<>();
        for (final RobotElement element : selected) {
            if (element instanceof RobotKeywordCall && element.getParent() instanceof RobotCodeHoldingElement<?>) {
                firstCases.add((RobotCodeHoldingElement<?>) element.getParent());

            } else if (element instanceof RobotCodeHoldingElement<?>) {
                firstCases.add((RobotCodeHoldingElement<?>) element);
            }
            if (firstCases.size() > 1) {
                // This method should never return set bigger than 2 elements
                break;
            }
        }
        return firstCases;
    }

    private boolean isTask(final RobotCodeHoldingElement<?> testCase) {
        return testCase.getLinkedElement().getModelType() == ModelType.TASK;
    }

    private IContributionItem createCurrentCaseItem(final IServiceLocator serviceLocator, final String label) {
        final CommandContributionItemParameter contributionParameters = new CommandContributionItemParameter(
                serviceLocator, id, RUN_TEST_COMMAND_ID, SWT.PUSH);
        contributionParameters.label = getModeName() + " " + label;
        contributionParameters.icon = getImageDescriptor();

        final HashMap<String, String> parameters = new HashMap<>();
        parameters.put(RUN_TEST_COMMAND_MODE_PARAMETER, getModeName().toUpperCase());
        contributionParameters.parameters = parameters;
        return new CommandContributionItem(contributionParameters);
    }
}