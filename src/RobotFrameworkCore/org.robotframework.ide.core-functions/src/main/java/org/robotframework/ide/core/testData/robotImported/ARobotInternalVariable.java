/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.robotImported;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableScope;
import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.core.testData.model.table.variables.IVariableHolder;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public abstract class ARobotInternalVariable<T> implements IVariableHolder {

    private final String name;
    private final VariableType type;
    private final List<RobotToken> comment = new LinkedList<>();
    private final VariableScope scope = VariableScope.GLOBAL;
    private final String robotRepresentation;
    private final T value;


    public ARobotInternalVariable(final String name, final T value, final VariableType type) {
        this.name = name;
        this.type = type;
        this.value = value;
        if (!this.name.startsWith(type.getIdentificator())) {
            this.robotRepresentation = type.getIdentificator() + '{' + name
                    + '}';
        } else {
            this.robotRepresentation = name;
        }
    }


    @Override
    public String getName() {
        return name;
    }


    @Override
    public VariableType getType() {
        return type;
    }


    public T getValue() {
        return value;
    }


    public String getRobotRepresentation() {
        return robotRepresentation;
    }


    @Override
    public VariableScope getScope() {
        return scope;
    }


    @Override
    public String toString() {
        return String.format(this.getClass()
                + " [name=%s, type=%s, value=%s, robotName=%s]", name, type,
                value, robotRepresentation);
    }


    @Override
    public List<RobotToken> getComment() {
        return comment;
    }


    @Override
    public void addCommentPart(final RobotToken rt) {
        // nothing to do
    }


    @Override
    public RobotToken getDeclaration() {
        return null;
    }
}