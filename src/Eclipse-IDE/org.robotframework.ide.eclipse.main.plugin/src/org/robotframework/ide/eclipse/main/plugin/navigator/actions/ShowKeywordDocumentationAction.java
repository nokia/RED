package org.robotframework.ide.eclipse.main.plugin.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.viewers.Selections;

public class ShowKeywordDocumentationAction extends Action implements IEnablementUpdatingAction {

    private final IWorkbenchPage page;
    private final ISelectionProvider selectionProvider;

    public ShowKeywordDocumentationAction(final IWorkbenchPage page, final ISelectionProvider selectionProvider) {
        super("Show keyword documentation");

        this.page = page;
        this.selectionProvider = selectionProvider;
    }

    @Override
    public void updateEnablement(final IStructuredSelection selection) {
        setEnabled(selection.size() == 1 && selection.getFirstElement() instanceof KeywordSpecification);
    }

    @Override
    public void run() {
        final KeywordSpecification spec = Selections.getSingleElement(
                (IStructuredSelection) selectionProvider.getSelection(), KeywordSpecification.class);

        new KeywordDocumentationPopup(page.getActivePart().getSite().getShell(), spec).open();
    }
}
