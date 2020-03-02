/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl;

import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.descs.PythonExpression;


public class PythonExprAdapter implements PythonExpression {

    private final ExpressionAstNode node;

    public PythonExprAdapter(final ExpressionAstNode node) {
        this.node = node;
    }

    @Override
    public String getExpression() {
        final String text = node.getText();
        return text.substring(3, text.length() - 2);
    }

    @Override
    public FileRegion getRegion() {
        return node.getRegion();
    }

    @Override
    public VariableType getType() {
        return VariableType.getTypeByChar(node.getText().charAt(0));
    }
}
