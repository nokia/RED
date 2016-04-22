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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.RemoteLocation;

import com.google.common.base.Strings;

class RemoteLocationDialog extends Dialog {

    private RemoteLocation location;
    private Label exceptionLabel;
    private Text uriText;

    RemoteLocationDialog(final Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        getShell().setText("Add Remote location");
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
        
        uriText = new Text(dialogComposite, SWT.SINGLE | SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).hint(300, SWT.DEFAULT).applyTo(uriText);
        uriText.setText("http://127.0.0.1:8270/");
        uriText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent event) {
                try {
                    final URI uri = new URI(uriText.getText());
                    getButton(IDialogConstants.OK_ID).setEnabled(true);

                    if (Strings.isNullOrEmpty(uri.getPath()) && uri.getPort() == -1) {
                        uriText.setBackground(uriText.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
                        exceptionLabel
                                .setText("URI have an empty path and port. Path '/RPC2' and port 8270 will be used");
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
        });

        exceptionLabel = new Label(dialogComposite, SWT.NONE);
        exceptionLabel.setText("");
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(exceptionLabel);

        uriText.setFocus();

        return dialogComposite;
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

    RemoteLocation getRemoteLocation() {
        return location;
    }
}