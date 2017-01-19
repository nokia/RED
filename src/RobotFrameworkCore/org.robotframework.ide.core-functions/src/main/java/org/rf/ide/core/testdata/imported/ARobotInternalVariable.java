/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.imported;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableScope;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.IVariableHolder;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public abstract class ARobotInternalVariable<T> implements IVariableHolder {

    private final String name;

    private final VariableType type;

    private final List<RobotToken> comment = new ArrayList<>();

    private final VariableScope scope = VariableScope.GLOBAL;

    private final String robotRepresentation;

    private final T value;

    public ARobotInternalVariable(final String name, final T value, final VariableType type) {
        this.type = type;
        this.value = value;
        if (!name.startsWith(type.getIdentificator())) {
            final String corrected = correctNameForRobot28(name);
            this.name = corrected;
            this.robotRepresentation = type.getIdentificator() + '{' + corrected + '}';
        } else {
            this.name = name;
            this.robotRepresentation = name;
        }
    }

    private String correctNameForRobot28(final String oldName) {
        String newName = oldName;
        if (oldName != null && oldName.length() > 3) {
            final int startIndex = oldName.indexOf('{');
            final int endIndex = oldName.lastIndexOf('}');
            if (startIndex > -1 && endIndex > -1) {
                newName = new String(oldName.substring(startIndex + 1, endIndex));
            }
        }

        return newName;
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
        return String.format(this.getClass() + " [name=%s, type=%s, value=%s, robotName=%s]", name, type, value,
                robotRepresentation);
    }

    @Override
    public RobotToken getDeclaration() {
        return null;
    }

    @Override
    public List<RobotToken> getValueTokens() {
        return new ArrayList<>();
    }

    @Override
    public List<RobotToken> getComment() {
        return comment;
    }

    @Override
    public void addCommentPart(final RobotToken rt) {
        // nothing to do
    }
}
