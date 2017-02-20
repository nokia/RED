/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

class ProjectComposite extends Composite {

    private Text projectText;

    private final ModifyListener listener;

    ProjectComposite(final Composite parent, final ModifyListener listener) {
        super(parent, SWT.NONE);
        this.listener = listener;

        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(this);

        createProjectText();
        createBrowseButton();
    }

    private void createProjectText() {
        projectText = new Text(this, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(projectText);
        projectText.addModifyListener(listener);
    }

    private void createBrowseButton() {
        final Button button = new Button(this, SWT.PUSH);
        button.setText("Browse...");
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(),
                        new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider());
                dialog.setTitle("Select project");
                dialog.setMessage("Select the project hosting your test suites:");
                dialog.addFilter(new ViewerFilter() {

                    @Override
                    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
                        return element instanceof IProject;
                    }
                });
                dialog.setAllowMultiple(false);
                dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
                if (dialog.open() == Window.OK) {
                    final IProject project = (IProject) dialog.getFirstResult();
                    projectText.setText(project.getName());
                }
            }
        });
        GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(button);
    }

    void setInput(final String projectName) {
        if (!projectText.isDisposed()) {
            projectText.setText(projectName);
        }
    }

    public String getSelectedProjectName() {
        return projectText.getText().trim();
    }

    public boolean isDisposedOrFilled() {
        return projectText == null || projectText.isDisposed() || !getSelectedProjectName().isEmpty();
    }

    public void disposeGroup() {
        if (!this.isDisposed()) {
            final Composite group = this.getParent();
            final Composite parent = group.getParent();
            group.dispose();
            parent.layout();
        }
    }

}
