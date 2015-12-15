/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.variables;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.robotframework.ide.eclipse.main.plugin.PathsConverter;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.ImportSettingFileArgumentsEditor;
import org.robotframework.red.forms.RedFormToolkit;

import com.google.common.base.Optional;

class VariableFileDialog extends Dialog {

    private final RedFormToolkit toolkit;

    private Optional<ReferencedVariableFile> variableFile;

    private Text pathText;

    private ImportSettingFileArgumentsEditor argumentsEditor;

    VariableFileDialog(final Shell parentShell, final Optional<ReferencedVariableFile> variableFile,
            final RedFormToolkit toolkit) {
        super(parentShell);
        this.variableFile = variableFile;
        this.toolkit = toolkit;
    }

    @Override
    public void create() {
        super.create();
        getShell().setText(variableFile.isPresent() ? "Edit Variable File" : "Add Variable File");
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite dialogComposite = (Composite) super.createDialogArea(parent);
        GridLayoutFactory.fillDefaults().numColumns(3).margins(3, 3).applyTo(dialogComposite);
        GridDataFactory.fillDefaults().grab(true, true).minSize(400, 50).applyTo(dialogComposite);

        final Label pathLabel = new Label(dialogComposite, SWT.WRAP);
        pathLabel.setText("Path:");

        pathText = toolkit.createText(dialogComposite, "", SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).hint(300, SWT.DEFAULT).applyTo(pathText);

        final Button browseFileBtn = toolkit.createButton(dialogComposite, "Browse...", SWT.NONE);
        browseFileBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final FileDialog dialog = new FileDialog(dialogComposite.getShell(), SWT.OPEN | SWT.MULTI);
                dialog.setFilterExtensions(new String[] { "*.py", "*.*" });
                dialog.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toPortableString());
                final String chosenFilePath = dialog.open();
                if (chosenFilePath != null) {
                    pathText.setText(chosenFilePath);
                }
            }
        });

        final ExpandableComposite section = toolkit.createExpandableComposite(dialogComposite,
                ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
        section.setBackground(null);
        section.setText("Add Arguments");
        section.addExpansionListener(new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(final ExpansionEvent e) {
                if (e.getState()) {
                    dialogComposite.getShell().setSize(dialogComposite.getShell().getSize().x, 300);
                } else {
                    dialogComposite.getShell().setSize(dialogComposite.getShell().getSize().x, 140);
                }
            }
        });
        GridDataFactory.fillDefaults().grab(true, true).span(3, 1).applyTo(section);

        final Composite sectionInternal = new Composite(section, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(sectionInternal);
        GridLayoutFactory.fillDefaults().applyTo(sectionInternal);
        section.setClient(sectionInternal);

        final List<String> arguments = newArrayList();
        if (variableFile.isPresent()) {
            pathText.setText(new Path(variableFile.get().getPath()).toOSString());
            final List<String> args = variableFile.get().getArguments();
            arguments.addAll(args);
        }
        section.setExpanded(!arguments.isEmpty());

        argumentsEditor = new ImportSettingFileArgumentsEditor();
        argumentsEditor.createArgumentsEditor(sectionInternal, arguments);

        return dialogComposite;
    }

    @Override
    protected void okPressed() {
        if (!pathText.getText().isEmpty()) {
            if (!variableFile.isPresent()) {
                variableFile = Optional.of(new ReferencedVariableFile());
            }
            final IPath path = PathsConverter.toWorkspaceRelativeIfPossible(new Path(pathText.getText()));
            variableFile.get().setPath(path.toPortableString());
            variableFile.get().setArguments(argumentsEditor.getArguments());
        }
        super.okPressed();
    }

    @Override
    public boolean close() {
        return super.close();
    }

    public ReferencedVariableFile getVariableFile() {
        return variableFile.orNull();
    }
}
