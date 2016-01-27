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
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;

import com.google.common.base.Optional;

public class RunTestDynamicMenuItem extends CompoundContributionItem {

    private static final String RUN_TEST_COMMAND_ID = "org.robotframework.red.runSingleTestFromSource";

    static final String RUN_TEST_COMMAND_TEST_NAME_PARAMETER = RUN_TEST_COMMAND_ID + ".testName";

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
        final RobotSuiteFile suiteModel = ((RobotFormEditor) activeWindow.getActivePage().getActiveEditor())
                .provideSuiteModel();

        final Optional<RobotCasesSection> casesSection = suiteModel.findSection(RobotCasesSection.class);
        if (!casesSection.isPresent()) {
            return new IContributionItem[0];
        }

        final RobotCase testCase = getTestCase(suiteModel, selection.getOffset());
        final List<IContributionItem> contributedItems = new ArrayList<>();
        if (testCase != null) {
            contributedItems.add(createCurrentCaseItem(activeWindow, testCase, true));
            contributedItems.add(new Separator());
        }
        for (final RobotCase tc : casesSection.get().getChildren()) {
            contributedItems.add(createCurrentCaseItem(activeWindow, tc, false));
        }

        return contributedItems.toArray(new IContributionItem[0]);
    }

    private IContributionItem createCurrentCaseItem(final IServiceLocator serviceLocator, final RobotCase testCase,
            final boolean isCurrent) {
        final CommandContributionItemParameter contributionParameters = new CommandContributionItemParameter(
                serviceLocator, id, RUN_TEST_COMMAND_ID, SWT.PUSH);
        contributionParameters.label = isCurrent ? "Current: '" + testCase.getName() + "'" : testCase.getName();
        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put(RUN_TEST_COMMAND_MODE_PARAMETER, getModeName());
        parameters.put(RUN_TEST_COMMAND_TEST_NAME_PARAMETER, testCase.getName());
        contributionParameters.parameters = parameters;
        return new CommandContributionItem(contributionParameters);
    }

    protected String getModeName() {
        return "RUN";
    }

    private RobotCase getTestCase(final RobotSuiteFile suiteModel, final int caretOffset) {
        final Optional<? extends RobotElement> element = suiteModel.findElement(caretOffset);
        if (element.isPresent()) {
            final RobotElement elem = element.get();
            if (elem instanceof RobotCase) {
                return (RobotCase) elem;
            } else if (elem instanceof RobotKeywordCall && elem.getParent() instanceof RobotCase) {
                return (RobotCase) elem.getParent();
            }
        }
        return null;
    }
}