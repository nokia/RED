package org.robotframework.ide.eclipse.main.plugin.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public class NewRobotTestSuiteWizard extends BasicNewResourceWizard {

    private WizardNewFileCreationPage mainPage;

    @Override
    public void init(final IWorkbench workbench, final IStructuredSelection currentSelection) {
        super.init(workbench, currentSelection);
        setNeedsProgressMonitor(true);
        setWindowTitle("New Robot Test Suite");
    }

    @Override
    public void addPages() {
        super.addPages();

        mainPage = new WizardNewFileCreationPage("New Robot Test Suite", getSelection());
        mainPage.setFileExtension("robot");
        mainPage.setWizard(this);
        mainPage.setTitle("Robot Test Suite");
        mainPage.setDescription("Create new Robot test suite file");

        this.addPage(mainPage);
    }

    @Override
    public boolean performFinish() {
        final IFile newFile = mainPage.createNewFile();
        selectAndReveal(newFile);

        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
                .getDefaultEditor(newFile.getName());
        try {
            page.openEditor(new FileEditorInput(newFile), desc.getId());
        } catch (final PartInitException e) {
            throw new RobotEditorOpeningException("Unable to open editor for file: " + newFile.getName(), e);
        }
        return true;
    }

    private static class RobotEditorOpeningException extends RuntimeException {

        public RobotEditorOpeningException(final String msg, final PartInitException cause) {
            super(msg, cause);
        }
    }
}
