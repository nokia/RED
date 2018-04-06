/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

class ExecutableFileComposite extends Composite {

    private Text executableFilePathText;

    ExecutableFileComposite(final Composite parent, final ModifyListener listener) {
        super(parent, SWT.NONE);

        GridLayoutFactory.fillDefaults().numColumns(2).margins(0, 5).applyTo(this);

        createExecutableFilePathText(listener);
        createSelectionButtons();
    }

    private void createExecutableFilePathText(final ModifyListener listener) {
        executableFilePathText = new Text(this, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(executableFilePathText);
        executableFilePathText.addModifyListener(listener);
    }

    private void createSelectionButtons() {
        final Composite buttonsParent = new Composite(this, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(3).applyTo(buttonsParent);
        GridDataFactory.fillDefaults().span(2, 1).align(SWT.END, SWT.FILL).applyTo(buttonsParent);
        BrowseButtons.selectWorkspaceFileButton(buttonsParent, executableFilePathText::setText,
                "Select executor file to run Robot Framework tests:");
        BrowseButtons.selectSystemFileButton(buttonsParent, executableFilePathText::setText,
                BrowseButtons.getSystemDependentExecutableFileExtensions());
        BrowseButtons.selectVariableButton(buttonsParent, executableFilePathText::insert);
    }

    void setInput(final String executableFilePath) {
        executableFilePathText.setText(executableFilePath);
    }

    String getSelectedExecutableFilePath() {
        return executableFilePathText.getText().trim();
    }

}
