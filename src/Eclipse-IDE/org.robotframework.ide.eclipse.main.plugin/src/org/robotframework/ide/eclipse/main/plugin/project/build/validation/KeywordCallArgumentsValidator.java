/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor.Argument;

import com.google.common.base.Splitter;
import com.google.common.collect.Range;

/**
 * @author Michal Anglart
 *
 */
class KeywordCallArgumentsValidator implements ModelUnitValidator {

    private final IFile file;

    private final RobotToken definingToken;

    private final ProblemsReportingStrategy reporter;

    private final ArgumentsDescriptor descriptor;

    private final List<RobotToken> arguments;

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

        final Range<Integer> expectedArgsNumber = descriptor.getPossibleNumberOfArguments();
        final int actual = arguments.size();
        if (!expectedArgsNumber.contains(actual)) {
            final String additional = String.format("Keyword '%s' expects " + getRangesInfo(expectedArgsNumber)
                    + ", but %d " + toBeInProperForm(actual) + " provided", definingToken.getText(), actual);

            final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS)
                    .formatMessageWith(additional);
            reporter.handleProblem(problem, file, definingToken);
            return;
        }

        final Map<String, Argument> argumentsWithNames = new HashMap<>();
        for (final Argument argument : descriptor) {
            argumentsWithNames.put(argument.getName(), argument);
        }


        boolean thereWasNamedArgumentAlready = false;
        for (final RobotToken arg : arguments) {
            if (isNamed(arg, argumentsWithNames.keySet())) {
                thereWasNamedArgumentAlready = true;
            } else if (thereWasNamedArgumentAlready) { // positional argument after named ones
                final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.POSITIONAL_ARGUMENT_AFTER_NAMED);
                reporter.handleProblem(problem, file, arg);
            } else {

            }
        }
    }

    private boolean isNamed(final RobotToken arg, final Collection<String> argumentNames) {
        final String argument = arg.getText();
        if (argument.contains("=")) {
            final String name = Splitter.on('=').limit(2).splitToList(argument).get(0);
            return argumentNames.contains(name);
        }
        return false;
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

    private static String toPluralIfNeeded(final String noun, final int amount) {
        return amount == 1 ? noun : noun + "s";
    }
}
