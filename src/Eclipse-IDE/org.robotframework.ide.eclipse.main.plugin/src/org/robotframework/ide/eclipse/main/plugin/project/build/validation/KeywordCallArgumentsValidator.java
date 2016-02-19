/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor.Argument;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
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
        boolean shallContinue = validateNumberOfArguments();
        if (!shallContinue) {
            return;
        }
        final Map<String, Argument> namesToArgs = namesToArgsMapping();

        shallContinue = validatePositionalAndNamedArgsOrder(namesToArgs.keySet());
        if (!shallContinue) {
            return;
        }

        final ArrayListMultimap<Argument, RobotToken> argsMapping = mapDescriptorArgumentsToTokens(namesToArgs);

        validateArgumentsMapping(namesToArgs, argsMapping);
    }

    private boolean validateNumberOfArguments() {
        final Range<Integer> expectedArgsNumber = descriptor.getPossibleNumberOfArguments();
        final int actual = arguments.size();
        if (!expectedArgsNumber.contains(actual)) {
            final String additional = String.format("Keyword '%s' expects " + getRangesInfo(expectedArgsNumber)
                    + ", but %d " + toBeInProperForm(actual) + " provided", definingToken.getText(), actual);

            final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS)
                    .formatMessageWith(additional);
            reporter.handleProblem(problem, file, definingToken);
            return false;
        }
        return true;
    }

    private Map<String, Argument> namesToArgsMapping() {
        final Map<String, Argument> argumentsWithNames = new HashMap<>();
        for (final Argument argument : descriptor) {
            argumentsWithNames.put(argument.getName(), argument);
        }
        return argumentsWithNames;
    }

    private boolean validatePositionalAndNamedArgsOrder(final Collection<String> argumentNames) {
        boolean thereWasNamedArgumentAlready = false;
        boolean thereIsAMessInOrder = false;
        for (final RobotToken arg : arguments) {
            if (!isPositional(arg, argumentNames)) {
                thereWasNamedArgumentAlready = true;
            } else if (thereWasNamedArgumentAlready) {
                final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.POSITIONAL_ARGUMENT_AFTER_NAMED);
                reporter.handleProblem(problem, file, arg);
                thereIsAMessInOrder = true;
            }
        }
        return !thereIsAMessInOrder;
    }

    private ArrayListMultimap<Argument, RobotToken> mapDescriptorArgumentsToTokens(
            final Map<String, Argument> namesToArgs) {
        final List<RobotToken> positional = new ArrayList<>();
        final List<RobotToken> named = new ArrayList<>();
        for (final RobotToken arg : arguments) {
            if (isPositional(arg, namesToArgs.keySet())) {
                positional.add(arg);
            } else {
                named.add(arg);
            }
        }

        final ArrayListMultimap<Argument, RobotToken> argsMapping = ArrayListMultimap.create();
        // map positional arguments
        for (int i = 0, j = 0; i < positional.size(); i++) {
            final Argument definingArg = descriptor.get(j);
            argsMapping.put(definingArg, positional.get(i));
            if (definingArg.isRequired() || definingArg.isDefault()) {
                j++;
            }
        }
        // map named arguments
        for (final RobotToken argToken : named) {
            final String name = getName(argToken);
            final Argument potentialArgument = namesToArgs.get(name);
            if (potentialArgument != null) {
                argsMapping.put(potentialArgument, argToken);
            } else {
                argsMapping.put(descriptor.getKwargArgument().get(), argToken);
            }
        }
        return argsMapping;
    }

    private void validateArgumentsMapping(final Map<String, Argument> namesToArgs,
            final ArrayListMultimap<Argument, RobotToken> argsMapping) {
        for (final Argument arg : descriptor) {
            final List<RobotToken> values = argsMapping.get(arg);
            if (arg.isRequired() && values.isEmpty()) {
                final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.NO_VALUE_PROVIDED_FOR_REQUIRED_ARG)
                        .formatMessageWith(definingToken.getText(), arg.getName());
                reporter.handleProblem(problem, file, definingToken);
            } else if ((arg.isRequired() || arg.isDefault()) && values.size() > 1) {
                final String firstValue = values.get(0).getText();
                for (int i = 1; i < values.size(); i++) {
                    final RobotToken argToken = values.get(i);
                    final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.MULTIPLE_MATCH_TO_SINGLE_ARG)
                            .formatMessageWith(arg.getName(), firstValue);
                    reporter.handleProblem(problem, file, argToken);
                }
            } else if (arg.isKwArg()) {
                for (final RobotToken argToken : values) {
                    if (isPositional(argToken, namesToArgs.keySet())) {
                        final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.MISMATCHING_ARGUMENT)
                                .formatMessageWith(argToken.getText(), definingToken.getText(), arg.getName());
                        reporter.handleProblem(problem, file, argToken);
                    }
                }
            }
        }
    }

    private boolean isPositional(final RobotToken arg, final Collection<String> argumentNames) {
        final String argument = arg.getText();
        if (argument.contains("=")) {
            final String name = Splitter.on('=').limit(2).splitToList(argument).get(0);
            return !descriptor.supportsKwargs() && !argumentNames.contains(name);
        }
        return !arg.getTypes().contains(RobotTokenType.VARIABLES_DICTIONARY_DECLARATION);
    }

    private String getName(final RobotToken robotToken) {
        return Splitter.on('=').limit(2).splitToList(robotToken.getText()).get(0);
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
