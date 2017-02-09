/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor.RobotEditorOpeningException;

public class NewRobotResourceWizard extends BasicNewResourceWizard {

    private WizardNewRobotResourceFileCreationPage mainPage;

    @Override
    public void init(final IWorkbench workbench, final IStructuredSelection currentSelection) {
        super.init(workbench, currentSelection);
        setNeedsProgressMonitor(true);
        setWindowTitle("New Robot Resource file");
    }

    @Override
    public void addPages() {
        super.addPages();

        mainPage = new WizardNewRobotResourceFileCreationPage("New Robot Resource file", getSelection(), "robot", "txt",
                "tsv");
        mainPage.setWizard(this);
        mainPage.setTitle("Robot Resource file");
        mainPage.setDescription("Create new Robot resource file");

        this.addPage(mainPage);
    }

    @Override
    public boolean performFinish() {
        mainPage.setExtension();
        final IFile newFile = mainPage.createNewFile();
        selectAndReveal(newFile);

        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().findEditor(RobotFormEditor.ID);
        try {
            page.openEditor(new FileEditorInput(newFile), desc.getId());
        } catch (final PartInitException e) {
            throw new RobotEditorOpeningException("Unable to open editor for file: " + newFile.getName(), e);
        }
        return true;
    }

}
