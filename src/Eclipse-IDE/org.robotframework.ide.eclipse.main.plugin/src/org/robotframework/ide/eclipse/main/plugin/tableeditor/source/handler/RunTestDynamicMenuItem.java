/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;

public class RunTestDynamicMenuItem extends CompoundContributionItem {

    private static final String RUN_TEST_COMMAND_ID = "org.robotframework.red.runSingleTestFromSource";

    static final String RUN_TEST_COMMAND_MODE_PARAMETER = RUN_TEST_COMMAND_ID + ".mode";

    private final String id;

    public RunTestDynamicMenuItem() {
        this("org.robotframework.red.menu.dynamic.source.run");
    }

    public RunTestDynamicMenuItem(final String id) {
        this.id = id;
    }

    @Override
    protected IContributionItem[] getContributionItems() {
        final IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        final ITextSelection selection = (ITextSelection) activeWindow.getSelectionService().getSelection();
        final RobotFormEditor suiteEditor = (RobotFormEditor) activeWindow.getActivePage().getActiveEditor();
        final RobotSuiteFile suiteModel = suiteEditor.provideSuiteModel();

        final RobotCase testCase = RunTestHandler.getTestCase(suiteModel, selection.getOffset());
        final List<IContributionItem> contributedItems = new ArrayList<>();
        if (testCase != null) {
            contributeBefore(contributedItems);
            contributedItems.add(createCurrentCaseItem(activeWindow, testCase));
        }
        return contributedItems.toArray(new IContributionItem[0]);
    }

    protected void contributeBefore(final List<IContributionItem> contributedItems) {
        contributedItems.add(new Separator());
    }

    private IContributionItem createCurrentCaseItem(final IServiceLocator serviceLocator, final RobotCase testCase) {
        final CommandContributionItemParameter contributionParameters = new CommandContributionItemParameter(
                serviceLocator, id, RUN_TEST_COMMAND_ID, SWT.PUSH);
        contributionParameters.label = getModeName() + " test: '" + testCase.getName() + "'";
        contributionParameters.icon = getImageDescriptor();
        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(RUN_TEST_COMMAND_MODE_PARAMETER, getModeName().toUpperCase());
        contributionParameters.parameters = parameters;
        return new CommandContributionItem(contributionParameters);
    }

    protected String getModeName() {
        return "Run";
    }

    protected ImageDescriptor getImageDescriptor() {
        return RedImages.getExecuteRunImage();
    }
}