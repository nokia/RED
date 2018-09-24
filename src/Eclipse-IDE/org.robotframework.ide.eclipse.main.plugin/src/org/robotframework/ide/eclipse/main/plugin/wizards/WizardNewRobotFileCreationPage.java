/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.robotframework.red.swt.Listeners;

abstract class WizardNewRobotFileCreationPage extends WizardNewFileCreationPage {

    private final Map<String, Button> extensionButtons;

    private final IStructuredSelection currentSelection;

    WizardNewRobotFileCreationPage(final String pageName, final IStructuredSelection selection) {
        super(pageName, selection);
        currentSelection = selection;
        extensionButtons = new LinkedHashMap<>();
    }

    @Override
    protected void createAdvancedControls(final Composite parent) {
        boolean isFirst = true;
        for (final String extension : getPossibleExtensions()) {
            final Button button = new Button(parent, SWT.RADIO);
            button.setText("as ." + extension + " file");
            button.setSelection(isFirst);
            GridDataFactory.fillDefaults().indent(20, 0).applyTo(button);

            button.addSelectionListener(Listeners.widgetSelectedAdapter(e -> {
                if (e.getSource() == button && button.getSelection()) {
                    final String currentName = getFileName();
                    final String currentNameWithoutExtension = currentName.contains(".")
                            ? currentName.substring(0, currentName.lastIndexOf("."))
                            : currentName;

                    for (final Entry<String, Button> entry : extensionButtons.entrySet()) {
                        if (entry.getValue() == button) {
                            setFileName(currentNameWithoutExtension + "." + entry.getKey());
                            break;
                        }
                    }
                }
            }));

            extensionButtons.put(extension, button);

            isFirst = false;
        }

        super.createAdvancedControls(parent);
    }

    protected abstract Iterable<String> getPossibleExtensions();

    public void setExtension() {
        setFileExtension(getSelectedExtension());
    }

    protected String getSelectedExtension() {
        return extensionButtons.entrySet()
                .stream()
                .filter(entry -> entry.getValue().getSelection())
                .map(Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("There has to be a single button selected"));
    }

    @Override
    protected boolean validatePage() {
        boolean isProjectAvailable = false;
        final Object[] selection = currentSelection.toArray();
        final boolean isValid = super.validatePage();
        if (!(selection.length == 0)) {
            for (Object project : selection) {
                while (project instanceof IFolder || project instanceof IFile) {
                    project = ((IResource) project).getParent();
                }
                if (project instanceof IProject) {
                    if (((IProject) project).isOpen()) {
                        isProjectAvailable = true;
                        break;
                    }
                }
            }
        }
        if (!isProjectAvailable && !isValid) {
            setErrorMessage("Action impossible to finish: No project available");
            return false;
        }

        if (!isValid) {
            return false;
        }

        final String currentName = getFileName();
        final String currentNameWithoutExtension = currentName.contains(".")
                ? currentName.substring(0, currentName.lastIndexOf("."))
                : currentName;

        if (currentNameWithoutExtension.isEmpty()) {
            setErrorMessage("Name cannot be empty.");
            return false;
        }

        final String name = currentName.contains(".") ? currentName : currentName + ".robot";
        final IPath resourcePath = getContainerFullPath().append(name);
        final IFile file = createFileHandle(resourcePath);

        final IContainer container = file.getParent();
        if (!container.exists()) {
            setErrorMessage("Folder '" + container.getFullPath().toString() + "' does not exists.");
            return false;
        }

        try {
            for (final IResource resource : container.members()) {
                if (name.equalsIgnoreCase(resource.getName())) {
                    setErrorMessage("'" + name + "' already exists.");
                    return false;
                }
            }
        } catch (final CoreException e) {
            ErrorDialog.openError(getShell(), "Problem occurred", "Error when validating wizard page", e.getStatus());
        }
        return true;
    }
}
