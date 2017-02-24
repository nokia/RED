/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class ExecutorScriptComposite extends Composite {

    private final ModifyListener listener;

    private final String[] filterExtensions;

    private Text scriptPathText;

    ExecutorScriptComposite(final Composite parent, final ModifyListener listener, final String[] filterExtensions) {
        super(parent, SWT.NONE);
        this.listener = listener;
        this.filterExtensions = filterExtensions;

        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);

        createScriptPathText();
        createBrowseButton(parent.getShell());
    }

    private void createScriptPathText() {
        scriptPathText = new Text(this, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(scriptPathText);
        scriptPathText.addModifyListener(listener);
    }

    private void createBrowseButton(final Shell shell) {
        final Button button = new Button(this, SWT.PUSH);
        button.setText("Browse...");
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent event) {
                final FileDialog dialog = createScriptFileDialog(shell);
                dialog.setFilterExtensions(filterExtensions);

                final String chosenFilePath = dialog.open();
                if (chosenFilePath != null) {
                    scriptPathText.setText(chosenFilePath);
                }
            }
        });
        GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(button);
    }

    private FileDialog createScriptFileDialog(final Shell shell) {
        final IPath startingPath = getStartingPath();
        final FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterPath(startingPath.toOSString());
        return dialog;
    }

    private IPath getStartingPath() {
        final String selectedPath = scriptPathText.getText().trim();
        if (selectedPath.isEmpty()) {
            return ResourcesPlugin.getWorkspace().getRoot().getLocation();
        }
        final Path path = new Path(selectedPath);
        if (path.toFile().isFile()) {
            return path.removeLastSegments(1);
        }
        return path;
    }

    void setInput(final String scriptPath) {
        scriptPathText.setText(scriptPath);
    }

    String getSelectedScriptPath() {
        return scriptPathText.getText().trim();
    }

}
