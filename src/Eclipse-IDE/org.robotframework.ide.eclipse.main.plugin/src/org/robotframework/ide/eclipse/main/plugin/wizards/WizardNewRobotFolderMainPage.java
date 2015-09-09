/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewFolderMainPage;

public class WizardNewRobotFolderMainPage extends WizardNewFolderMainPage {

    private Button initializationFileButton;

    public WizardNewRobotFolderMainPage(final String pageName, final IStructuredSelection selection) {
        super(pageName, selection);
    }

    @Override
    protected void createAdvancedControls(final Composite parent) {
        initializationFileButton = new Button(parent, SWT.CHECK);
        initializationFileButton.setText("Create suite initialization file");

        super.createAdvancedControls(parent);
    }

    boolean shouldInitFileBeCreated() {
        return initializationFileButton.getSelection();
    }
}
