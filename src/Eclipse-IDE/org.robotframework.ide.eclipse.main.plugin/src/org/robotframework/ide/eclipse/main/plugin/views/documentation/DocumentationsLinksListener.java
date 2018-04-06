/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.ui.statushandlers.StatusManager;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationsLinksSupport.UnableToOpenUriException;

public class DocumentationsLinksListener implements LocationListener {

    private final DocumentationsLinksSupport linksSupport;

    public DocumentationsLinksListener(final DocumentationsLinksSupport linksSupport) {
        this.linksSupport = linksSupport;
    }

    @Override
    public void changing(final LocationEvent event) {
        try {
            event.doit = !linksSupport.changeLocationTo(toUri(event.location));

        } catch (final UnableToOpenUriException e) {
            StatusManager.getManager().handle(
                    new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, "Cannot open '" + event.location + "'", e),
                    StatusManager.BLOCK);
        }
    }

    private URI toUri(final String location) {
        try {
            return new URI(location);
        } catch (final URISyntaxException e) {
            throw new UnableToOpenUriException("Syntax error in uri '" + location + "'", e);
        }
    }

    @Override
    public void changed(final LocationEvent event) {
        // nothing to do
    }
}