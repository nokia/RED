/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.general;

import java.util.ArrayList;
import java.util.List;

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
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;

import com.google.common.annotations.VisibleForTesting;

public class VariableMappingDialog extends Dialog {

    private boolean isVariablePredefined;

    private VariableMapping mapping;

    private Label exceptionLabel;

    private StyledText nameText;

    private StyledText valueText;

    private final String initialVariableName;

    public VariableMappingDialog(final Shell parentShell) {
        this(parentShell, "${var}");
        this.isVariablePredefined = false;
    }

    public VariableMappingDialog(final Shell parentShell, final String variableName) {
        super(parentShell);
        this.isVariablePredefined = true;
        this.initialVariableName = variableName;
    }

    @Override
    public void create() {
        super.create();
        getShell().setText("Add variable mapping");
        getShell().setMinimumSize(400, 200);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite dialogComposite = (Composite) super.createDialogArea(parent);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(10, 10).applyTo(dialogComposite);

        final Label infoLabel = new Label(dialogComposite, SWT.WRAP);
        infoLabel.setText("Specify name and value of variable which will be used in parameterized imports.");
        GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).span(2, 1).applyTo(infoLabel);

        final Label nameLabel = new Label(dialogComposite, SWT.NONE);
        nameLabel.setText("Name");

        nameText = new StyledText(dialogComposite, SWT.SINGLE | SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).hint(300, SWT.DEFAULT).applyTo(nameText);
        nameText.setText(initialVariableName);
        nameText.addModifyListener(e -> validate());

        final Label valueLabel = new Label(dialogComposite, SWT.NONE);
        valueLabel.setText("Value");

        valueText = new StyledText(dialogComposite, SWT.SINGLE | SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).hint(300, SWT.DEFAULT).applyTo(valueText);
        valueText.setText("value");
        valueText.addModifyListener(e -> validate());

        exceptionLabel = new Label(dialogComposite, SWT.WRAP);
        exceptionLabel.setText("");
        GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(exceptionLabel);

        final StyledText controlToFocus = isVariablePredefined ? valueText : nameText;
        controlToFocus.setFocus();
        controlToFocus.setSelection(0, controlToFocus.getText().length());

        return dialogComposite;
    }

    private void validate() {
        final List<String> errorMsgs = new ArrayList<>();

        if (nameText.getText().isEmpty()) {
            errorMsgs.add("Name cannot be empty");
            nameText.setBackground(nameText.getDisplay().getSystemColor(SWT.COLOR_RED));
        } else {
            nameText.setBackground(nameText.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        }

        if (valueText.getText().isEmpty()) {
            errorMsgs.add("Value cannot be empty");
            valueText.setBackground(valueText.getDisplay().getSystemColor(SWT.COLOR_RED));
        } else {
            valueText.setBackground(valueText.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        }

        exceptionLabel.setText(String.join("\n", errorMsgs));
        getButton(IDialogConstants.OK_ID).setEnabled(errorMsgs.isEmpty());
    }

    @Override
    protected void okPressed() {
        mapping = VariableMapping.create(nameText.getText(), valueText.getText());

        super.okPressed();
    }

    @VisibleForTesting
    Button getOkButton() {
        return getButton(IDialogConstants.OK_ID);
    }

    @VisibleForTesting
    StyledText getNameText() {
        return nameText;
    }

    @VisibleForTesting
    StyledText getValueText() {
        return valueText;
    }

    public VariableMapping getMapping() {
        return mapping;
    }
}
