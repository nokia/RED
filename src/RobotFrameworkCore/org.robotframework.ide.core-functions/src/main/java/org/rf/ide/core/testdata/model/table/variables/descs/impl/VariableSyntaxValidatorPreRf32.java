/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl;

import org.rf.ide.core.testdata.model.table.variables.descs.VariableUse.VariableUseSyntaxException;

class VariableSyntaxValidatorPreRf32 extends VariableSyntaxValidator {

    @Override
    void validate(final VarAstNodeAdapter varNode) {
        // the parser for vars <3.2 ensures that variable node has one of type identifiers followed
        // { and ended with }, so we can remove them and look into the name body

        // this is implementation of robot.variables.isvar.is_var(string, identifiers) function

        final String name = varNode.asToken().getText();

        final String nameBody = name.substring(2, name.length() - 1);
        if (nameBody.isEmpty() || nameBody.contains("{") || nameBody.contains("}")) {
            throw new VariableUseSyntaxException("The name '" + name + "' is invalid", proposal(name));
        }
    }

    private String proposal(final String name) {
        final String nameBody = name.substring(2, name.length() - 1);
        final String nameBodyFixed = nameBody.replaceAll("\\{", "").replaceAll("\\}", "");

        return name.substring(0, 2) + nameBodyFixed + name.substring(name.length() - 1);
    }
}
