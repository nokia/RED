/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import java.util.Optional;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;

public class InspectElementInDebugShellDynamicMenuItem extends CompoundContributionItem {

    private static final String INSPECT_COMMAND_ID = "org.robotframework.red.inspectElementFromSource";

    static final String INSPECT_COMMAND_MODE_PARAMETER = INSPECT_COMMAND_ID + ".mode";

    private final String id;

    public InspectElementInDebugShellDynamicMenuItem() {
        this("org.robotframework.red.menu.dynamic.source.inspect");
    }

    public InspectElementInDebugShellDynamicMenuItem(final String id) {
        this.id = id;
    }

    @Override
    protected IContributionItem[] getContributionItems() {
        final IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (!(activeWindow.getActivePage().getActiveEditor() instanceof RobotFormEditor)) {
            return new IContributionItem[0];
        }

        final ITextSelection selection = (ITextSelection) activeWindow.getSelectionService().getSelection();
        final RobotFormEditor suiteEditor = (RobotFormEditor) activeWindow.getActivePage().getActiveEditor();
        final RobotSuiteFile suiteModel = suiteEditor.provideSuiteModel();
        final int offset = selection.getOffset();

        try {
            final Optional<IRegion> variableRegion = DocumentUtilities
                    .findVariable(suiteEditor.getSourceEditor().getDocument(), suiteModel.isTsvFile(), offset);
            if (variableRegion.isPresent()) {
                return new IContributionItem[] { createInspectVariableItem(activeWindow) };
            }
        } catch (final BadLocationException e) {
            // we'll look for the element
        }

        final Optional<? extends RobotElement> element = suiteModel.findElement(offset);
        if (element.isPresent() && element.get().getClass() == RobotKeywordCall.class) {
            return new IContributionItem[] { createInspectCallItem(activeWindow) };
        }
        return new IContributionItem[0];
    }

    private IContributionItem createInspectVariableItem(final IServiceLocator serviceLocator) {
        final CommandContributionItemParameter contributionParameters = new CommandContributionItemParameter(
                serviceLocator, id, INSPECT_COMMAND_ID, SWT.PUSH);
        contributionParameters.label = "Inspect variable";
        contributionParameters.icon = null;
        return new CommandContributionItem(contributionParameters);
    }

    private IContributionItem createInspectCallItem(final IServiceLocator serviceLocator) {
        final CommandContributionItemParameter contributionParameters = new CommandContributionItemParameter(
                serviceLocator, id, INSPECT_COMMAND_ID, SWT.PUSH);
        contributionParameters.label = "Inspect call";
        contributionParameters.icon = null;
        return new CommandContributionItem(contributionParameters);
    }
}
