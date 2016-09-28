/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import org.rf.ide.core.testdata.mapping.variables.CommonVariableHelper;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

/**
 * @author wypych
 */
public class VariableNameRedCellEditorValidator extends DefaultRedCellEditorValueValidator {

    private final CommonVariableHelper commonVarHelper = new CommonVariableHelper();

    @Override
    public void validate(final String value) {
        super.validate(value);

        final Optional<String> error = getProblemsWithVariableName(value);

        if (error.isPresent()) {
            throw new CellEditorValueValidationException(error.get());
        }
    }

    @VisibleForTesting
    Optional<String> getProblemsWithVariableName(final String value) {
        Optional<String> error = Optional.absent();
        final char[] chars = value.toCharArray();
        if (chars.length > 3) {
            if (value.startsWith(VariableType.SCALAR.getIdentificator() + "{")
                    || value.startsWith(VariableType.LIST.getIdentificator() + "{")
                    || value.startsWith(VariableType.DICTIONARY.getIdentificator() + "{")) {
                if (value.endsWith("}")) {
                    if (!commonVarHelper.matchesBracketsConditionsForCorrectVariable(value)) {
                        error = Optional.of("Name should match with [$@&]{name}");
                    }
                } else {
                    error = Optional.of("Name should end with }");
                }
            } else {
                error = Optional.of("Name should start with one of [$@&] followed by {");
            }
        } else {
            error = Optional.of("Name should match with [$@&]{name}");
        }
        return error;
    }
}
