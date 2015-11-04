/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewFolderMainPage;

public class WizardNewRobotFolderMainPage extends WizardNewFolderMainPage {

    private Button initializationFileButton;
    private Button robotFormat;
    private Button tsvFormat;

    public WizardNewRobotFolderMainPage(final String pageName, final IStructuredSelection selection) {
        super(pageName, selection);
    }

    @Override
    protected void createAdvancedControls(final Composite parent) {
        initializationFileButton = new Button(parent, SWT.CHECK);
        initializationFileButton.setText("Create suite initialization file");

        robotFormat = new Button(parent, SWT.RADIO);
        robotFormat.setText("as .robot file");
        robotFormat.setSelection(true);
        robotFormat.setEnabled(false);
        GridDataFactory.fillDefaults().indent(20, 0).applyTo(robotFormat);

        tsvFormat = new Button(parent, SWT.RADIO);
        tsvFormat.setText("as .tsv file");
        tsvFormat.setEnabled(false);
        GridDataFactory.fillDefaults().indent(20, 0).applyTo(tsvFormat);

        initializationFileButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                robotFormat.setEnabled(initializationFileButton.getSelection());
                tsvFormat.setEnabled(initializationFileButton.getSelection());
            }
        });
        super.createAdvancedControls(parent);
    }

    boolean shouldInitFileBeCreated() {
        return initializationFileButton.getSelection();
    }

    String getInitFileExtension() {
        return robotFormat.getSelection() ? "robot" : "tsv";
    }
}
