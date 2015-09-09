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
    private final String robotRepresentation;
    private T value;


    public AVariableImported(final String name, final VariableType type) {
        this.name = name;
        this.type = type;
        this.robotRepresentation = type.getIdentificator() + '{' + name + '}';
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


    public String getRobotRepresentation() {
        return robotRepresentation;
    }


    @Override
    public String toString() {
        return String.format(this.getClass()
                + " [name=%s, type=%s, value=%s, robotName=%]", name, type,
                value, robotRepresentation);
    }
}
