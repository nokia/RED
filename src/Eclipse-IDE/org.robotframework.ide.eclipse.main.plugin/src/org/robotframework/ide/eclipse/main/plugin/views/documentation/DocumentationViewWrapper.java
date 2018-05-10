/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import org.eclipse.e4.tools.compat.parts.DIViewPart;

/**
 * @author mmarzec
 */
@SuppressWarnings("restriction")
public class DocumentationViewWrapper extends DIViewPart<DocumentationView> {
    public DocumentationViewWrapper() {
        super(DocumentationView.class);
    }

    @Override
    public DocumentationView getComponent() {
        // overriding so that there will be no access-restricion warnings on call sites
        return super.getComponent();
    }
}
