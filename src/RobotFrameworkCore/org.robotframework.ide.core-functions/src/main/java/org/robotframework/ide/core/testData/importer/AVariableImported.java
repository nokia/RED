/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.importer;

import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableType;


public abstract class AVariableImported<T> {

    private final String name;
    private final VariableType type;
    private T value;


    public AVariableImported(final String name, final VariableType type) {
        this.name = name;
        this.type = type;
    }


    public String getName() {
        return name;
    }


    public VariableType getType() {
        return type;
    }


    public T getValue() {
        return value;
    }


    public void setValue(T value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return String.format(this.getClass() + " [name=%s, type=%s, value=%s]",
                name, type, value);
    }
}
