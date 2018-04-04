/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import java.net.MalformedURLException;
import java.net.URI;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationViewLinksSupport.OpenableUri;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationViewLinksSupport.UnableToOpenUriException;
import org.robotframework.red.swt.SwtThread;

class ExternalBrowserUri implements OpenableUri {

    private final URI uri;

    private final IWorkbenchBrowserSupport browserSupport;

    ExternalBrowserUri(final URI uri, final IWorkbenchBrowserSupport browserSupport) {
        this.uri = uri;
        this.browserSupport = browserSupport;
    }

    @Override
    public void open() {
        SwtThread.asyncExec(() -> {
            try {
                final IWebBrowser wbBrowser = browserSupport.createBrowser(null);
                wbBrowser.openURL(uri.toURL());
            } catch (PartInitException | MalformedURLException e) {
                throw new UnableToOpenUriException("Unable to open external browser", e);
            }
        });
    }
}
