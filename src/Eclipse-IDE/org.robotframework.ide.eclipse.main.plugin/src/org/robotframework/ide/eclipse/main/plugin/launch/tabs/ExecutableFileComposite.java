/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class ExecutableFileComposite extends Composite {

    private Text filePathText;

    private final List<Button> buttons = new ArrayList<>();

    public ExecutableFileComposite(final Composite parent, final String fileChooserTitle) {
        this(parent, fileChooserTitle, BrowseButtons.getSystemDependentExecutableFileExtensions());
    }

    public ExecutableFileComposite(final Composite parent, final String fileChooserTitle,
            final String[] allowedExtensions) {
        super(parent, SWT.NONE);

        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);

        createFilePathText();
        createSelectionButtons(fileChooserTitle, allowedExtensions);
    }

    private void createFilePathText() {
        filePathText = new Text(this, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(filePathText);
    }

    private void createSelectionButtons(final String fileChooserTitle, final String[] extensions) {
        final Composite buttonsParent = new Composite(this, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(3).applyTo(buttonsParent);
        GridDataFactory.fillDefaults().span(2, 1).align(SWT.END, SWT.FILL).applyTo(buttonsParent);
        buttons.add(BrowseButtons.selectWorkspaceFileButton(buttonsParent, filePathText::setText, fileChooserTitle));
        buttons.add(BrowseButtons.selectSystemFileButton(buttonsParent, filePathText::setText, extensions));
        buttons.add(BrowseButtons.selectVariableButton(buttonsParent, filePathText::insert));
    }

    public void addModifyListener(final ModifyListener listener) {
        filePathText.addModifyListener(listener);
    }

    public void removeModifyListener(final ModifyListener listener) {
        filePathText.removeModifyListener(listener);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        filePathText.setEnabled(enabled);
        buttons.forEach(c -> c.setEnabled(enabled));
    }

    public void setFilePath(final String filePath) {
        filePathText.setText(filePath);
    }

    public String getFilePath() {
        return filePathText.getText().trim();
    }
}
