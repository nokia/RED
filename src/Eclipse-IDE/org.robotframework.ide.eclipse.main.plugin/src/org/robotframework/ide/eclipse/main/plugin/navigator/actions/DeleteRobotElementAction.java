/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.actions;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class DeleteRobotElementAction extends Action implements IEnablementUpdatingAction {

    private final IWorkbenchPage page;
    private final ISelectionProvider selectionProvider;

    public DeleteRobotElementAction(final IWorkbenchPage page, final ISelectionProvider selectionProvider) {
        super("Delete", PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));

        this.page = page;
        this.selectionProvider = selectionProvider;
    }

    @Override
    public void run() {
        SafeRunner.run(new SafeRunnable() {
            @Override
            public void run() {
                throw new RuntimeException("Not yet implemented!");
            }
        });
    }

    @Override
    public void updateEnablement(final IStructuredSelection selection) {
        setEnabled(true);
    }

}
