/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl;

import org.rf.ide.core.testdata.model.table.variables.descs.VariableUse.VariableUseSyntaxException;
import org.rf.ide.core.testdata.model.table.variables.descs.impl.ExpressionAstNode.VarSyntaxIssue;

class VariableSyntaxValidator {

    void validate(final VarAstNodeAdapter varNode) {
        if (varNode.getErrorType() == VarSyntaxIssue.MISSING_PAREN) {
            throw new VariableUseSyntaxException(
                    "Variable '" + varNode.asToken().getText() + "' was not closed properly",
                    varNode.asToken().getText() + "}");

        } else if (varNode.getErrorType() == VarSyntaxIssue.MISSING_BRACKET) {
            throw new VariableUseSyntaxException(
                    "Variable item '" + varNode.asToken().getText() + "' was not closed properly",
                    varNode.asToken().getText() + "]");
        }
    }
}
