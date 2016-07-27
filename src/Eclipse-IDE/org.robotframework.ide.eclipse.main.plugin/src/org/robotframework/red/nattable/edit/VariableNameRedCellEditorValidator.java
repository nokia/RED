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

    private final VariableType validType;

    private final CommonVariableHelper commonVarHelper;

    public VariableNameRedCellEditorValidator(final VariableType validType) {
        super();
        this.validType = validType;
        this.commonVarHelper = new CommonVariableHelper();
    }

    @Override
    public void validate(final String value) {
        super.validate(value);

        Optional<String> error = getProblemsWithVariableName(value);

        if (error.isPresent()) {
            throw new CellEditorValueValidationException(error.get());
        }
    }

    @VisibleForTesting
    protected Optional<String> getProblemsWithVariableName(final String value) {
        Optional<String> error = Optional.absent();
        char[] chars = value.toCharArray();
        final String identificator = validType.getIdentificator();
        if (chars.length > 3) {
            if (value.startsWith(identificator + '{')) {
                if (value.endsWith("}")) {
                    if (!commonVarHelper.isCorrectVariable(value)) {
                        error = Optional
                                .of("Incorrect variable name it should be in syntax " + identificator + "{name} .");
                    }
                } else {
                    error = Optional.of("Expected to ends variable with } .");
                }
            } else {
                error = Optional.of("Expected to start variable with " + identificator + "{ .");
            }
        } else {
            error = Optional.of("Is not variable syntax " + identificator + "{[name]}.");
        }
        return error;
    }

    public VariableType getValidType() {
        return validType;
    }
}
