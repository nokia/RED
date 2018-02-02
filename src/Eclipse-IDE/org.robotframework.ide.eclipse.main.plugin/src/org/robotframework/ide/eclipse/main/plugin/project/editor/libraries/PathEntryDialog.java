/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;

/**
 * @author Michal Anglart
 */
class PathEntryDialog extends Dialog {

    private List<SearchPath> searchPath;

    private StyledText pathText;

    PathEntryDialog(final Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        getShell().setText("Add new search path");
        getShell().setMinimumSize(400, 200);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite dialogComposite = (Composite) super.createDialogArea(parent);
        GridLayoutFactory.fillDefaults().margins(10, 10).applyTo(dialogComposite);

        final Label infoLabel = new Label(dialogComposite, SWT.WRAP);
        infoLabel.setText("Provide search paths to be added. Each path should be specified in separate line.");
        GridDataFactory.fillDefaults().hint(350, SWT.DEFAULT).applyTo(infoLabel);

        pathText = new StyledText(dialogComposite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        GridDataFactory.fillDefaults().grab(true, true).hint(350, 100).applyTo(pathText);

        return dialogComposite;
    }

    @Override
    protected void okPressed() {
        searchPath = Splitter.on('\n')
                .splitToList(pathText.getText())
                .stream()
                .map(singlePath -> singlePath.trim().replaceAll("\t", " "))
                .filter(trimmedPath -> !trimmedPath.isEmpty())
                .map(SearchPath::create)
                .collect(Collectors.toList());
        super.okPressed();
    }

    @VisibleForTesting
    Button getOkButton() {
        return getButton(IDialogConstants.OK_ID);
    }

    @VisibleForTesting
    StyledText getSearchPathsText() {
        return pathText;
    }

    List<SearchPath> getSearchPath() {
        return searchPath;
    }
}
