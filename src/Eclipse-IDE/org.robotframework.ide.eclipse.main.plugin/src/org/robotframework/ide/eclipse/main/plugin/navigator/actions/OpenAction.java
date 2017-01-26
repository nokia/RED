/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Optional;

public class OpenAction extends Action implements IEnablementUpdatingAction {

    private final IWorkbenchPage page;
    private final ISelectionProvider selectionProvider;

    public OpenAction(final IWorkbenchPage page, final ISelectionProvider selectionProvider) {
        super("Open");

        this.page = page;
        this.selectionProvider = selectionProvider;
    }

    @Override
    public void run() {
        final IStructuredSelection selection = (IStructuredSelection) selectionProvider.getSelection();
        final Optional<RobotFileInternalElement> element = Selections.getOptionalFirstElement(selection,
                RobotFileInternalElement.class);
        if (element.isPresent()) {
            element.get().getOpenRobotEditorStrategy().run(page);
        }
    }

    @Override
    public void updateEnablement(final IStructuredSelection selection) {
        setEnabled(selection.size() == 1
                && Selections.getOptionalFirstElement(selection, RobotFileInternalElement.class).isPresent());
    }
}
