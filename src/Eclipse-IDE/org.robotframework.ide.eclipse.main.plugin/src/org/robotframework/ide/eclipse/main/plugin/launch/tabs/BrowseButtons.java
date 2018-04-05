/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static org.robotframework.red.swt.Listeners.widgetSelectedAdapter;

import java.util.function.Consumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;
import org.rf.ide.core.executor.RedSystemProperties;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

public class BrowseButtons {

    public static String[] getSystemDependentExecutableFileExtensions() {
        return RedSystemProperties.isWindowsPlatform() ? new String[] { "*.bat;*.com;*.exe", "*.*" }
                : new String[] { "*.sh", "*.*" };
    }

    public static Button selectVariableButton(final Composite parent, final Consumer<String> selectedValueConsumer) {
        final Button button = new Button(parent, SWT.PUSH);
        GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(button);
        button.setText("Variables...");
        button.addSelectionListener(widgetSelectedAdapter(e -> {
            final StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(parent.getShell());
            final int code = dialog.open();
            if (code == IDialogConstants.OK_ID) {
                final String variable = dialog.getVariableExpression();
                if (variable != null) {
                    selectedValueConsumer.accept(variable);
                }
            }
        }));
        return button;
    }

    public static Button selectWorkspaceFileButton(final Composite parent, final Consumer<String> selectedValueConsumer,
            final String message) {
        final Button button = new Button(parent, SWT.PUSH);
        GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(button);
        button.setText("Workspace...");
        button.addSelectionListener(widgetSelectedAdapter(e -> {
            final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(parent.getShell(),
                    new WorkbenchLabelProvider(), new WorkbenchContentProvider());
            dialog.setTitle("Select file:");
            dialog.setMessage(message);
            dialog.setAllowMultiple(false);
            dialog.setValidator(selection -> selection.length == 1 && selection[0] instanceof IFile
                    ? new Status(IStatus.OK, RedPlugin.PLUGIN_ID, null)
                    : new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, "Files only allowed"));
            dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
            dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
            if (dialog.open() == IDialogConstants.OK_ID) {
                final IResource resource = (IResource) dialog.getFirstResult();
                if (resource != null) {
                    final String arg = resource.getFullPath().toString();
                    final String fileLoc = VariablesPlugin.getDefault()
                            .getStringVariableManager()
                            .generateVariableExpression("workspace_loc", arg); //$NON-NLS-1$
                    selectedValueConsumer.accept(fileLoc);
                }
            }
        }));
        return button;
    }

    public static Button selectSystemFileButton(final Composite parent, final Consumer<String> selectedValueConsumer,
            final String[] extensions) {
        final Button button = new Button(parent, SWT.PUSH);
        GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(button);
        button.setText("File system...");
        button.addSelectionListener(widgetSelectedAdapter(e -> {
            final FileDialog dialog = new FileDialog(parent.getShell(), SWT.OPEN);
            dialog.setFilterExtensions(extensions);
            final String filePath = dialog.open();
            if (filePath != null) {
                selectedValueConsumer.accept(filePath);
            }
        }));
        return button;
    }

}
