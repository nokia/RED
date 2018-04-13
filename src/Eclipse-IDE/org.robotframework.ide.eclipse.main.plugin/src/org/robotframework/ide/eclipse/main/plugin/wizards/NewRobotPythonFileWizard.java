/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.SourceOpeningSupport;

/**
 * @author Michal Anglart
 *
 */
public class NewRobotPythonFileWizard extends BasicNewResourceWizard {

    private WizardNewRobotPythonFilePage mainPage;

    @Override
    public void init(final IWorkbench workbench, final IStructuredSelection currentSelection) {
        super.init(workbench, currentSelection);
        setNeedsProgressMonitor(true);
        setWindowTitle("New Robot python file");
    }

    @Override
    public void addPages() {
        super.addPages();

        mainPage = new WizardNewRobotPythonFilePage("New Robot python file", getSelection());
        mainPage.setWizard(this);
        mainPage.setTitle("Robot python file");
        mainPage.setDescription("Create new Robot python file (variable/library)");

        this.addPage(mainPage);
    }

    @Override
    public boolean performFinish() {
        final IFile newFile = mainPage.createNewFile();
        selectAndReveal(newFile);

        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        SourceOpeningSupport.tryToOpenInEditor(page, newFile);

        return true;
    }

}
