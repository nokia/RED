/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;


/**
 * @author Michal Anglart
 *
 */
public class DefaultRedCellEditorValueValidator implements CellEditorValueValidator<String> {

    @Override
    public void validate(final String value) {
        if (value.contains("  ") || value.contains("\t") || value.contains(" | ")) {
            throw new CellEditorValueValidationException("Single entry cannot contain cells separator");
        }
    }
}
