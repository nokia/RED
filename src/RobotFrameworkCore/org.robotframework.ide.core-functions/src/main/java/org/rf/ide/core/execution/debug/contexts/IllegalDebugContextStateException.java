/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug.contexts;


public class IllegalDebugContextStateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    IllegalDebugContextStateException(final String message) {
        super(message);
    }

}
