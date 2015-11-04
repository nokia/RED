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
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;


class WizardNewRobotResourceFileCreationPage extends WizardNewFileCreationPage {

    private Button robotFormat;

    private Button tsvFormat;

    WizardNewRobotResourceFileCreationPage(final String pageName, final IStructuredSelection selection) {
        super(pageName, selection);
    }

    @Override
    protected void createAdvancedControls(final Composite parent) {
        robotFormat = new Button(parent, SWT.RADIO);
        robotFormat.setText("as .robot file");
        robotFormat.setSelection(true);
        GridDataFactory.fillDefaults().indent(20, 0).applyTo(robotFormat);

        tsvFormat = new Button(parent, SWT.RADIO);
        tsvFormat.setText("as .tsv file");
        GridDataFactory.fillDefaults().indent(20, 0).applyTo(tsvFormat);

        robotFormat.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final String currentName = getFileName();
                final String currentExtension = currentName.contains(".")
                        ? currentName.substring(currentName.lastIndexOf('.') + 1) : "";
                final String currentNameWithoutExtension = currentName.contains(".") 
                        ? currentName.substring(0, currentName.lastIndexOf(".")) : currentName;
                        
                if (robotFormat.getSelection() && !"robot".equals(currentExtension)) {
                    setFileName(currentNameWithoutExtension + ".robot");
                } else if (tsvFormat.getSelection() && !"tsv".equals(currentExtension)) {
                    setFileName(currentNameWithoutExtension + ".tsv");
                }
            }
        });

        super.createAdvancedControls(parent);
    }

    public void setExtension() {
        if (robotFormat.getSelection()) {
            setFileExtension("robot");
        } else {
            setFileExtension("tsv");
        }
    }
}
