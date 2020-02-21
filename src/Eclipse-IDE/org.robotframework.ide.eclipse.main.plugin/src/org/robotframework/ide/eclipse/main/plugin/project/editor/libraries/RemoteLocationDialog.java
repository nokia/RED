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
        return String.format("http://127.0.0.1:%d/", RemoteLocation.DEFAULT_PORT);
    }

    @Override
    protected void validate(final String text,
            final AddNewElementDialog<RemoteLocation>.LocalValidationCallback callback) {
        try {
            final URI uri = new URI(text);
            getButton(IDialogConstants.OK_ID).setEnabled(true);

            if (Strings.isNullOrEmpty(uri.getPath()) && uri.getPort() == -1) {
                callback.warning(String.format("URI have an empty path and port. Path '%s' and port %d will be used",
                        RemoteLocation.DEFAULT_PATH, RemoteLocation.DEFAULT_PORT));

            } else if (Strings.isNullOrEmpty(uri.getPath())) {
                callback.warning(
                        String.format("URI have an empty path. Path '%s' will be used", RemoteLocation.DEFAULT_PATH));

            } else if (uri.getPort() == -1) {
                callback.warning(
                        String.format("URI have no port specified. Port %d will be used", RemoteLocation.DEFAULT_PORT));

            } else {
                callback.passed();
            }
        } catch (final URISyntaxException e) {
            callback.error("URI problem " + e.getMessage().toLowerCase());
        }
    }

    @Override
    protected RemoteLocation createElement(final String text) {
        return RemoteLocation.create(RemoteLocation.addDefaults(URI.create(text)));
    }
}
