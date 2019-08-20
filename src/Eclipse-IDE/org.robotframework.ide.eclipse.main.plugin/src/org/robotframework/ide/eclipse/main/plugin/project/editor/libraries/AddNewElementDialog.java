/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

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

import com.google.common.annotations.VisibleForTesting;


public abstract class AddNewElementDialog<T> extends Dialog {

    private StyledText text;

    private Label exceptionLabel;

    private T createdElement;


    public AddNewElementDialog(final Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        getShell().setText(getDialogTitle());
        getShell().setMinimumSize(400, 200);
    }

    protected abstract String getDialogTitle();

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite dialogComposite = (Composite) super.createDialogArea(parent);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(10, 10).applyTo(dialogComposite);

        final Label infoLabel = new Label(dialogComposite, SWT.WRAP);
        infoLabel.setText(getInfoText());
        GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).span(2, 1).applyTo(infoLabel);

        final Label uriLabel = new Label(dialogComposite, SWT.NONE);
        uriLabel.setText(getTextLabel());

        text = new StyledText(dialogComposite, SWT.SINGLE | SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).hint(300, SWT.DEFAULT).applyTo(text);
        text.setText(getDefaultText());
        text.addModifyListener(e -> {
            getButton(IDialogConstants.OK_ID).setEnabled(false);
            validate(text.getText(), new LocalValidationCallback());
        });

        exceptionLabel = new Label(dialogComposite, SWT.WRAP);
        exceptionLabel.setText("");
        GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(exceptionLabel);

        text.setFocus();

        return dialogComposite;
    }

    protected abstract String getInfoText();

    protected abstract String getTextLabel();

    protected abstract String getDefaultText();

    protected abstract void validate(String text, LocalValidationCallback callback);

    @Override
    protected void okPressed() {
        createdElement = createElement(text.getText());

        super.okPressed();
    }

    protected abstract T createElement(String text);

    @VisibleForTesting
    Button getOkButton() {
        return getButton(IDialogConstants.OK_ID);
    }

    @VisibleForTesting
    StyledText getText() {
        return text;
    }

    public T getCreatedElement() {
        return createdElement;
    }

    protected class LocalValidationCallback {

        void passed() {
            setResult("", SWT.COLOR_WHITE, true);
        }

        void warning(final String msg) {
            setResult(msg, SWT.COLOR_YELLOW, true);
        }

        void error(final String msg) {
            setResult(msg, SWT.COLOR_RED, false);
        }

        private void setResult(final String msg, final int color, final boolean shouldEnableOk) {
            text.setBackground(text.getDisplay().getSystemColor(color));
            exceptionLabel.setText(msg);

            getButton(IDialogConstants.OK_ID).setEnabled(shouldEnableOk);
        }
    }
}
