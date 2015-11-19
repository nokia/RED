/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.importer;

import java.util.ArrayList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableScope;
import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.core.testData.model.table.variables.IVariableHolder;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public abstract class AVariableImported<T> implements IVariableHolder {

    private final String name;
    private final VariableType type;
    private final List<RobotToken> comment = new ArrayList<>();
    private final VariableScope scope = VariableScope.TEST_SUITE;
    private final String robotRepresentation;
    private T value;


    public AVariableImported(final String name, final VariableType type) {
        this.name = name;
        this.type = type;
        if (shouldWrapName(name, type)) {
            this.robotRepresentation = type.getIdentificator() + '{' + name
                    + '}';
        } else {
            this.robotRepresentation = name;
        }
    }


    private boolean shouldWrapName(final String name, final VariableType type) {
        boolean result = true;

        if (name != null && !name.trim().isEmpty()) {
            result = !name.startsWith(type.getIdentificator());
            if (!result) {
                VariableType varType = VariableType.getTypeByChar(name.trim()
                        .charAt(0));
                result = (varType != null && varType != VariableType.INVALID);
            }
        }

        return result;
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
    public void addCommentPart(RobotToken rt) {
        // nothing to do
    }


    @Override
    public RobotToken getDeclaration() {
        return null;
    }
}
