/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

public class RemoteLocationDialog extends AddNewElementDialog<RemoteLocation> {

    public RemoteLocationDialog(final Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected String getDialogTitle() {
        return "Add Remote location";
    }

    @Override
    protected String getInfoText() {
        return "Specify URI of XML-RPC server location. This server will be used for running keywords using Remote library.";
    }

    @Override
    protected String getTextLabel() {
        return "URI";
    }

    @Override
    protected String getDefaultText() {
        return "http://127.0.0.1:8270/";
    }

    @Override
    protected void validate(final String text,
            final AddNewElementDialog<RemoteLocation>.LocalValidationCallback callback) {
        try {
            final URI uri = new URI(text);
            getButton(IDialogConstants.OK_ID).setEnabled(true);

            if (Strings.isNullOrEmpty(uri.getPath()) && uri.getPort() == -1) {
                callback.warning("URI have an empty path and port. Path '/RPC2' and port 8270 will be used");

            } else if (Strings.isNullOrEmpty(uri.getPath())) {
                callback.warning("URI have an empty path. Path '/RPC2' will be used");

            } else if (uri.getPort() == -1) {
                callback.warning("URI have no port specified. Port 8270 will be used");

            } else {
                callback.passed();
            }
        } catch (final URISyntaxException e) {
            callback.error("URI problem " + e.getMessage().toLowerCase());
        }
    }

    @Override
    protected RemoteLocation createElement(final String text) {
        try {
            return RemoteLocation.create(createUriWithDefaultsIfMissing(new URI(text), 8270, "/RPC2"));
        } catch (final URISyntaxException e) {
            throw new IllegalStateException("Can't happen. It is not possible to click ok with invalid URI", e);
        }
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
}
