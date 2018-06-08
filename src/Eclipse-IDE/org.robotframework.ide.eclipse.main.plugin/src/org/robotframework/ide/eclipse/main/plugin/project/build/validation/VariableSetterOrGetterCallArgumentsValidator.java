/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;

import com.google.common.collect.Range;

class VariableSetterOrGetterCallArgumentsValidator extends KeywordCallArgumentsValidator {

    VariableSetterOrGetterCallArgumentsValidator(final IFile file, final RobotToken definingToken,
            final ValidationReportingStrategy reporter, final ArgumentsDescriptor descriptor,
            final List<RobotToken> arguments) {
        super(file, definingToken, reporter, descriptor, arguments);
    }

    @Override
    public void validate(final IProgressMonitor monitor) {
        final boolean shallContinue = validateNumberOfArguments();
        if (!shallContinue) {
            return;
        }
        validateArguments();
    }

    private boolean validateNumberOfArguments() {
        final Range<Integer> expectedArgsNumber = descriptor.getPossibleNumberOfArguments();
        final int actual = arguments.size();
        if (!expectedArgsNumber.contains(actual)) {
            if (!listIsPassed() && !dictIsPassed()) {
                final String additional = String
                        .format("Keyword '%s' expects " + getRangesInfo(expectedArgsNumber, "argument") + ", but %d "
                                + toBeInProperForm(actual) + " provided", definingToken.getText(), actual);

                final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS)
                        .formatMessageWith(additional);
                reporter.handleProblem(problem, file, definingToken);
                return false;
            }
        }
        return true;
    }

    private boolean dictIsPassed() {
        return hasTokenOfType(RobotTokenType.VARIABLES_DICTIONARY_DECLARATION);
    }

    private boolean listIsPassed() {
        return hasTokenOfType(RobotTokenType.VARIABLES_LIST_DECLARATION);
    }

    private boolean hasTokenOfType(final RobotTokenType type) {
        return arguments.stream().anyMatch(token -> token.getTypes().contains(type));
    }

    private void validateArguments() {
        final RobotToken firstArg = arguments.get(0);
        if (!firstArg.getTypes().contains(RobotTokenType.VARIABLES_SCALAR_DECLARATION)
                && !firstArg.getTypes().contains(RobotTokenType.VARIABLES_LIST_DECLARATION)
                && !firstArg.getTypes().contains(RobotTokenType.VARIABLES_DICTIONARY_DECLARATION)) {
            final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.INVALID_VARIABLE_SYNTAX)
                    .formatMessageWith(firstArg.getText());
            reporter.handleProblem(problem, file, firstArg);
        }
    }
}
