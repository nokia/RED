/*
 * Copyright 2015 Nokia Solutions and Networks
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
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;

import com.google.common.collect.Range;

/**
 * @author Michal Anglart
 */
abstract class KeywordCallArgumentsValidator implements ModelUnitValidator {

    protected final IFile file;

    protected final RobotToken definingToken;

    protected final ProblemsReportingStrategy reporter;

    protected final ArgumentsDescriptor descriptor;

    protected final List<RobotToken> arguments;

    KeywordCallArgumentsValidator(final IFile file, final RobotToken definingToken,
            final ProblemsReportingStrategy reporter, final ArgumentsDescriptor descriptor,
            final List<RobotToken> arguments) {
        this.file = file;
        this.definingToken = definingToken;
        this.reporter = reporter;
        this.descriptor = descriptor;
        this.arguments = arguments;
    }

    @Override
    public void validate(final IProgressMonitor monitor) {
        final boolean shallContinue = validateNumberOfArguments();
        if (!shallContinue) {
            return;
        }

        validateArguments(monitor);
    }

    abstract void validateArguments(final IProgressMonitor monitor);

    private boolean validateNumberOfArguments() {
        final Range<Integer> expectedArgsNumber = descriptor.getPossibleNumberOfArguments();
        final int actual = arguments.size();
        if (!expectedArgsNumber.contains(actual)) {
            if (!listIsPassed() && !dictIsPassed()) {
                final String additional = String.format("Keyword '%s' expects " + getRangesInfo(expectedArgsNumber)
                        + ", but %d " + toBeInProperForm(actual) + " provided", definingToken.getText(), actual);

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

    private static String getRangesInfo(final Range<Integer> range) {
        final int minArgs = range.lowerEndpoint();
        if (!range.hasUpperBound()) {
            return "at least " + minArgs + " " + toPluralIfNeeded("argument", minArgs);
        } else if (range.lowerEndpoint().equals(range.upperEndpoint())) {
            return minArgs + " " + toPluralIfNeeded("argument", minArgs);
        } else {
            final int maxArgs = range.upperEndpoint();
            return "from " + minArgs + " to " + maxArgs + " arguments";
        }
    }

    private static String toBeInProperForm(final int amount) {
        return amount == 1 ? "is" : "are";
    }

    protected static String toPluralIfNeeded(final String noun, final int amount) {
        return amount == 1 ? noun : noun + "s";
    }

}
