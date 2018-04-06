/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationViewInput;

class DocumentationsBrowsingHistory {

    private final DocumentationsLinksSupport linksSupport;

    private final List<URI> urisHistory = new ArrayList<>();
    private int current = -1;

    public DocumentationsBrowsingHistory(final DocumentationsLinksSupport linksSupport) {
        this.linksSupport = linksSupport;
    }

    void newInput(final DocumentationViewInput input) {
        try {
            final URI uri = input.getInputUri();
            if (urisHistory.isEmpty()) {
                urisHistory.add(uri);
                current = 0;
            } else {
                if (uri.equals(urisHistory.get(current))) {
                    return;
                }

                for (int i = urisHistory.size() - 1; i >= 0 && i > current; i--) {
                    urisHistory.remove(i);
                }
                urisHistory.add(uri);
                current++;
            }
        } catch (final URISyntaxException e) {
            // no uri registered in this history
        }
    }

    boolean isBackEnabled() {
        return 0 < current && current <= urisHistory.size() - 1;
    }

    void back() {
        current--;
        open(urisHistory.get(current));
    }
    boolean isForwardEnabled() {
        return 0 <= current && current < urisHistory.size() - 1;
    }

    void forward() {
        current++;
        open(urisHistory.get(current));
    }

    private void open(final URI toOpen) {
        linksSupport.changeLocationTo(toOpen);
    }
}
