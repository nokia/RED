package org.robotframework.ide.eclipse.main.plugin.navigator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;

public class OpenAction extends Action {

    private final IWorkbenchPage page;
    private final ISelectionProvider selectionProvider;

    public OpenAction(final IWorkbenchPage page, final ISelectionProvider selectionProvider) {
        super("Open");
        this.page = page;
        this.selectionProvider = selectionProvider;
    }

    @Override
    public boolean isEnabled() {
        final IStructuredSelection selection = (IStructuredSelection) selectionProvider.getSelection();

        if (selection.size() == 1 && selection.getFirstElement() instanceof RobotElement) {
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        final IStructuredSelection selection = (IStructuredSelection) selectionProvider.getSelection();
        final Object element = selection.getFirstElement();
        if (element instanceof RobotElement) {
            ((RobotElement) element).getOpenRobotEditorStrategy(page).run();
        }
    }

}
