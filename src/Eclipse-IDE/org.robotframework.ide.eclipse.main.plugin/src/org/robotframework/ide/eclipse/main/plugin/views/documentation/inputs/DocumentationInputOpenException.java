/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import org.eclipse.ui.PartInitException;

public class DocumentationInputOpenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DocumentationInputOpenException(final String message) {
        super(message);
    }

    public DocumentationInputOpenException(final String message, final PartInitException cause) {
        super(message, cause);
    }
}
