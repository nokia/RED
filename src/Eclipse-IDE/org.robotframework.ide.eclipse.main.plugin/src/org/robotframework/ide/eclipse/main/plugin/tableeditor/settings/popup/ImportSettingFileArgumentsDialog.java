/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class ImportSettingFileArgumentsDialog extends Dialog {

    private List<String> currentArgs;

    private ImportSettingFileArgumentsEditor argsEditor;

    public ImportSettingFileArgumentsDialog(final Shell parentShell, final List<String> currentArgs) {
        super(parentShell);
        this.currentArgs = currentArgs;
    }

    @Override
    public void create() {
        super.create();
        getShell().setText("Edit Arguments");
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite dialogComposite = (Composite) super.createDialogArea(parent);
        GridLayoutFactory.fillDefaults().numColumns(1).margins(3, 3).applyTo(dialogComposite);
        GridDataFactory.fillDefaults().grab(true, true).minSize(400, 200).applyTo(dialogComposite);

        argsEditor = new ImportSettingFileArgumentsEditor();
        argsEditor.createArgumentsEditor(dialogComposite, currentArgs);

        return dialogComposite;
    }

    @Override
    protected void okPressed() {
        currentArgs = argsEditor.getArguments();
        super.okPressed();
    }

    @Override
    public boolean close() {
        return super.close();
    }

    public List<String> getArguments() {
        return currentArgs;
    }
}
