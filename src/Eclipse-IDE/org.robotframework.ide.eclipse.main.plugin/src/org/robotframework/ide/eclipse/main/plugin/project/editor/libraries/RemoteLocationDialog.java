/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.net.URI;
import java.net.URISyntaxException;

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
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

class RemoteLocationDialog extends Dialog {

    private RemoteLocation location;

    private Label exceptionLabel;

    private StyledText uriText;

    RemoteLocationDialog(final Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        getShell().setText("Add Remote location");
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
        infoLabel.setText("Specify URI of XML-RPC server location. This server will be used"
                + " for running keywords using Remote library.");
        GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).span(2, 1).applyTo(infoLabel);

        final Label uriLabel = new Label(dialogComposite, SWT.NONE);
        uriLabel.setText("URI");

        uriText = new StyledText(dialogComposite, SWT.SINGLE | SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).hint(300, SWT.DEFAULT).applyTo(uriText);
        uriText.setText("http://127.0.0.1:8270/");
        uriText.addModifyListener(e -> validate());

        exceptionLabel = new Label(dialogComposite, SWT.WRAP);
        exceptionLabel.setText("");
        GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(exceptionLabel);

        uriText.setFocus();

        return dialogComposite;
    }

    private void validate() {
        try {
            final URI uri = new URI(uriText.getText());
            getButton(IDialogConstants.OK_ID).setEnabled(true);

            if (Strings.isNullOrEmpty(uri.getPath()) && uri.getPort() == -1) {
                uriText.setBackground(uriText.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
                exceptionLabel.setText("URI have an empty path and port. Path '/RPC2' and port 8270 will be used");
            } else if (Strings.isNullOrEmpty(uri.getPath())) {
                uriText.setBackground(uriText.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
                exceptionLabel.setText("URI have an empty path. Path '/RPC2' will be used");
            } else if (uri.getPort() == -1) {
                uriText.setBackground(uriText.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
                exceptionLabel.setText("URI have no port specified. Port 8270 will be used");
            } else {
                uriText.setBackground(uriText.getDisplay().getSystemColor(SWT.COLOR_WHITE));
                exceptionLabel.setText("");
            }
        } catch (final URISyntaxException e) {
            uriText.setBackground(uriText.getDisplay().getSystemColor(SWT.COLOR_RED));
            getButton(IDialogConstants.OK_ID).setEnabled(false);
            exceptionLabel.setText("URI problem " + e.getMessage().toLowerCase());
        }
    }

    @Override
    protected void okPressed() {
        try {
            location = RemoteLocation.create(createUriWithDefaultsIfMissing(new URI(uriText.getText()), 8270, "/RPC2"));
        } catch (final URISyntaxException e) {
            throw new IllegalStateException("Can't happen. It is not possible to click ok with invalid URI", e);
        }

        super.okPressed();
    }

    @VisibleForTesting
    static URI createUriWithDefaultsIfMissing(final URI uri, final int defaultPort, final String defaultPath) {
        try {
            final int port = uri.getPort() != -1 ? uri.getPort() : defaultPort;
            final String uriPath = uri.getPath();
            final String path = !Strings.isNullOrEmpty(uriPath) ? uriPath : defaultPath;
            final URI newUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), port, path, uri.getQuery(),
                    uri.getFragment());
            return newUri;
        } catch (final URISyntaxException e) {
            return uri;
        }
    }

    @VisibleForTesting
    Button getOkButton() {
        return getButton(IDialogConstants.OK_ID);
    }

    @VisibleForTesting
    StyledText getUriText() {
        return uriText;
    }

    RemoteLocation getRemoteLocation() {
        return location;
    }
}
