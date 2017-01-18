/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import com.google.common.collect.Maps;

class WizardNewRobotResourceFileCreationPage extends WizardNewFileCreationPage {

    private final Map<String, Button> extensionButtons;

    WizardNewRobotResourceFileCreationPage(final String pageName, final IStructuredSelection selection,
            final String firstExtension, final String... restExtensions) {
        super(pageName, selection);
        extensionButtons = Maps.<String, Button> newLinkedHashMap();
        extensionButtons.put(firstExtension, null);
        for (final String extension : restExtensions) {
            extensionButtons.put(extension, null);
        }
    }

    @Override
    protected void createAdvancedControls(final Composite parent) {
        boolean isFirst = true;
        for (final String extension : extensionButtons.keySet()) {
            final Button button = new Button(parent, SWT.RADIO);
            button.setText("as ." + extension + " file");
            button.setSelection(isFirst);
            GridDataFactory.fillDefaults().indent(20, 0).applyTo(button);

            button.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(final SelectionEvent e) {
                    if (e.getSource() == button && button.getSelection()) {
                        final String currentName = getFileName();
                        final String currentNameWithoutExtension = currentName.contains(".")
                                ? currentName.substring(0, currentName.lastIndexOf(".")) : currentName;

                        for (final Entry<String, Button> entry : extensionButtons.entrySet()) {
                            if (entry.getValue() == button) {
                                setFileName(currentNameWithoutExtension + "." + entry.getKey());
                                break;
                            }
                        }
                    }
                }
            });

            extensionButtons.put(extension, button);

            isFirst = false;
        }

        super.createAdvancedControls(parent);
    }

    public void setExtension() {
        for (final Entry<String, Button> entry : extensionButtons.entrySet()) {
            if (entry.getValue().getSelection()) {
                setFileExtension(entry.getKey());
                break;
            }
        }
    }

    @Override
    protected boolean validatePage() {
        final boolean isValid = super.validatePage();

        final String currentName = getFileName();
        final String currentNameWithoutExtension = currentName.contains(".")
                ? currentName.substring(0, currentName.lastIndexOf(".")) : currentName;

        if (currentNameWithoutExtension.isEmpty()) {
            setErrorMessage("Name cannot be empty");
            return false;
        }
        IPath resourcePath = getContainerFullPath().append(currentName.toLowerCase());
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        if (workspace.getRoot().getFolder(resourcePath).exists()
                || workspace.getRoot().getFile(resourcePath).exists()) {
            setErrorMessage(String.format("Resource  \"%s\" already exists", currentName));
            return false;
        }
        return isValid;
    }
}
