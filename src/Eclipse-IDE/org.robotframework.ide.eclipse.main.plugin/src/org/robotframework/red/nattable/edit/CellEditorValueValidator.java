/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

/**
 * @author Michal Anglart
 */
public interface CellEditorValueValidator<V> {

    void validate(V value);

    public static class CellEditorValueValidationException extends RuntimeException {

        private static final long serialVersionUID = 3031857236018948462L;

        public CellEditorValueValidationException(final String message) {
            super(message);
        }
    }
}
