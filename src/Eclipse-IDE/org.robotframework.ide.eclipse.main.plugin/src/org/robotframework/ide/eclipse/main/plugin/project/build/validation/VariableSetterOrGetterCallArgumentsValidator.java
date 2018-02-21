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
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;

class VariableSetterOrGetterCallArgumentsValidator extends KeywordCallArgumentsValidator {

    VariableSetterOrGetterCallArgumentsValidator(final IFile file, final RobotToken definingToken,
            final ProblemsReportingStrategy reporter, final ArgumentsDescriptor descriptor,
            final List<RobotToken> arguments) {
        super(file, definingToken, reporter, descriptor, arguments);
    }

    @Override
    void validateArguments(final IProgressMonitor monitor) {
        validateVariableSetterOrGetterFirstArgument();
    }

    private void validateVariableSetterOrGetterFirstArgument() {
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
