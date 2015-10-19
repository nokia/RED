/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;


public class ImportRobotProjectWizard extends Wizard implements IImportWizard {

    @Override
    public void init(final IWorkbench workbench, final IStructuredSelection selection) {
        setWindowTitle("Import folder with Robot files as project into the workspace");

    }

    @Override
    public void addPages() {
        super.addPages();

        addPage(new WizardImportRobotProjectPage());
    }

    @Override
    public boolean performFinish() {
        // TODO Auto-generated method stub
        return false;
    }

}
