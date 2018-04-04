/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import java.net.URI;
import java.util.function.Consumer;

import org.robotframework.ide.eclipse.main.plugin.views.documentation.DocumentationViewLinksSupport.OpenableUri;
import org.robotframework.red.swt.SwtThread;

class FragmentUri implements OpenableUri {

    static boolean isAboutBlankFragmentUri(final URI uri) {
        return uri.getScheme().equals("about") && uri.getSchemeSpecificPart().equals("blank")
                && uri.getFragment() != null;
    }

    private final URI uri;

    private final Consumer<String> javascriptConsumer;

    FragmentUri(final URI uri, final Consumer<String> javascriptConsumer) {
        this.uri = uri;
        this.javascriptConsumer = javascriptConsumer;
    }

    @Override
    public void open() {
        SwtThread.asyncExec(() -> {
            javascriptConsumer.accept("document.getElementById('" + uri.getFragment() + "').scrollIntoView();");
        });
    }
}
