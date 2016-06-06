/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.presenter.MoveElementHelper;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class ScalarVariable extends AVariable {

    private final List<RobotToken> values = new ArrayList<>();

    public ScalarVariable(final String name, final RobotToken declaration, final VariableScope scope) {
        super(VariableType.SCALAR, name, declaration, scope);
    }

    public void addValue(final RobotToken value) {
        values.add(value);
    }

    public void addValue(final RobotToken value, final int position) {
        values.set(position, value);
    }

    public void removeValue(final RobotToken value) {
        values.remove(value);
    }

    public boolean moveLeftValue(final RobotToken value) {
        return MoveElementHelper.moveLeft(values, value);
    }

    public boolean moveRightValue(final RobotToken value) {
        return MoveElementHelper.moveRight(values, value);
    }

    public List<RobotToken> getValues() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public boolean isPresent() {
        return (getDeclaration() != null);
    }

    @Override
    public VariableType getType() {
        if (values.size() >= 2) {
            this.type = VariableType.SCALAR_AS_LIST;
        } else {
            this.type = VariableType.SCALAR;
        }

        return type;
    }

    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> tokens = new ArrayList<>();
        if (isPresent()) {
            tokens.add(getDeclaration());
            tokens.addAll(getValues());
            tokens.addAll(getComment());
        }

        return tokens;
    }

    @Override
    public ScalarVariable copy() {
        final RobotToken dec = RobotToken.create(
                VariableType.SCALAR.getIdentificator() + "{" + getName() + "}", getDeclaration().getTypes());

        final ScalarVariable scalar = new ScalarVariable(getName(), dec, getScope());
        for (final RobotToken valueToken : getValues()) {
            final RobotToken token = RobotToken.create(valueToken.getText(), valueToken.getTypes());
            scalar.addValue(token);
        }
        for (final RobotToken commentToken : getComment()) {
            final RobotToken token = RobotToken.create(commentToken.getText(), commentToken.getTypes());
            scalar.addCommentPart(token);
        }
        return scalar;
    }
}
