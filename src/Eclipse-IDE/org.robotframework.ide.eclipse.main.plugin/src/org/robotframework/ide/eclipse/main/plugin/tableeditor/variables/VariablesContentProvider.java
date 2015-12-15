/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import java.util.Arrays;

import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.StructuredContentProvider;

public class VariablesContentProvider extends StructuredContentProvider {

    private final boolean editable;

    public VariablesContentProvider(final boolean editable) {
        this.editable = editable;
    }

    @Override
    public Object[] getElements(final Object inputElement) {
        if (inputElement instanceof RobotSuiteFileSection) {
            final RobotSuiteFileSection section = (RobotSuiteFileSection) inputElement;
            final Object[] elements = section.getChildren().toArray();
            final Object[] newElements = Arrays.copyOf(elements, elements.length + 1, Object[].class);
            newElements[elements.length] = new ElementAddingToken("scalar", editable);
            return newElements;
        }
        return new Object[0];
    }

}
