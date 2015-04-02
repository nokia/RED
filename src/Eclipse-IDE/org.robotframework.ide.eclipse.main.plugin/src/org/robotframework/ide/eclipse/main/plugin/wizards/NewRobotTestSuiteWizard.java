package org.robotframework.ide.eclipse.main.plugin.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewFolderMainPage;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.robotframework.ide.eclipse.main.plugin.nature.RobotProjectNature;

public class NewRobotTestSuiteWizard extends BasicNewResourceWizard {

    private WizardNewFolderMainPage mainPage;

    @Override
    public void init(final IWorkbench workbench, final IStructuredSelection currentSelection) {
        super.init(workbench, currentSelection);
        setNeedsProgressMonitor(true);
        setWindowTitle("New Robot test suite");
    }

    @Override
    public void addPages() {
        super.addPages();

        mainPage = new WizardNewFolderMainPage("New Robot Project", getSelection());
        mainPage.setWizard(this);
        mainPage.setTitle("Robot File");
        mainPage.setDescription("Create new Robot test cases file");

        this.addPage(mainPage);
    }

    @Override
    public boolean performFinish() {
        final IFolder newFolder = mainPage.createNewFolder();
        selectAndReveal(newFolder);

        try {
            final IFile initFile = RobotProjectNature.createRobotInitializationFile(newFolder);

            final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            final IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
                    .getDefaultEditor(initFile.getName());
            page.openEditor(new FileEditorInput(initFile), desc.getId());

        } catch (final CoreException e) {
            throw new SuiteCreatingException("Unable to create suite " + newFolder.getName(), e);
        }
        return true;
    }

    private static class SuiteCreatingException extends RuntimeException {

        public SuiteCreatingException(final String msg, final CoreException cause) {
            super(msg, cause);
        }
    }
}
