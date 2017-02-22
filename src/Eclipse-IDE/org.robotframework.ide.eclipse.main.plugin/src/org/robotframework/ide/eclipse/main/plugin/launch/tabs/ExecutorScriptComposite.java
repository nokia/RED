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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class ExecutorScriptComposite extends Composite {

    private final ModifyListener listener;

    private Text scriptPathText;

    private final Text scriptArgumentsText;

    private final Text scriptRunCommandText;

    ExecutorScriptComposite(final Composite parent, final ModifyListener listener) {
        super(parent, SWT.NONE);
        this.listener = listener;

        GridLayoutFactory.fillDefaults().numColumns(2).spacing(2, 2).margins(0, 3).applyTo(this);

        createScriptPathText();
        createBrowseButton(parent.getShell());

        scriptArgumentsText = createLabeledText("Additional script arguments:");
        scriptRunCommandText = createLabeledText("Script run command:");
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
                dialog.setFilterExtensions(getFilterExtensions());

                final String chosenFilePath = dialog.open();
                if (chosenFilePath != null) {
                    scriptPathText.setText(chosenFilePath);
                }
            }

            private String[] getFilterExtensions() {
                return System.getProperty("os.name").startsWith("Windows") ? new String[] { "*.bat", "*.*" }
                        : new String[] { "*.sh", "*.*" };
            }
        });
        GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(button);
    }

    private FileDialog createScriptFileDialog(final Shell shell) {
        final IPath startingPath = getStartingPath();
        final FileDialog dialog = new FileDialog(shell, SWT.OPEN | SWT.MULTI);
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

    private Text createLabeledText(final String label) {
        final Label lbl = new Label(this, SWT.NONE);
        lbl.setText(label);
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(lbl);

        final Text txt = new Text(this, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(txt);
        txt.addModifyListener(listener);
        return txt;
    }

    void setInput(final String scriptPath, final String scriptArguments, final String scriptRunCommand) {
        scriptPathText.setText(scriptPath);
        scriptArgumentsText.setText(scriptArguments);
        scriptRunCommandText.setText(scriptRunCommand);
    }

    String getSelectedScriptPath() {
        return scriptPathText.getText().trim();
    }

    String getScriptArguments() {
        return scriptArgumentsText.getText().trim();
    }

    String getScriptRunCommand() {
        return scriptRunCommandText.getText().trim();
    }

    boolean isDisposedOrFilled() {
        return scriptPathText == null || scriptPathText.isDisposed() || !getSelectedScriptPath().isEmpty();
    }

}
