/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.descs.VariableUse.VariableUseSyntaxException;

class VariableSyntaxValidatorPreRf32 extends VariableSyntaxValidator {

    @Override
    void validate(final VarAstNodeAdapter varNode) {
        // the parser for vars <3.2 ensures that variable node has one of type identifiers followed
        // { and ended with }, so we can remove them and look into the name body

        // this is implementation of robot.variables.isvar.is_var(string, identifiers) function

        if (varNode.isDynamic()) {
            return;
        }

        final String name = varNode.getContentWithoutBraces();

        if (name.isEmpty() || name.contains("{") || name.contains("}")) {
            throw new VariableUseSyntaxException("The name '" + varNode.asToken().getText() + "' is invalid",
                    proposal(varNode.getType(), name));
        }
    }

    private String proposal(final VariableType type, final String name) {
        final String nameFixed = name.replaceAll("\\{", "").replaceAll("\\}", "");

        return type.getIdentificator() + "{" + nameFixed + "}";
    }
}
