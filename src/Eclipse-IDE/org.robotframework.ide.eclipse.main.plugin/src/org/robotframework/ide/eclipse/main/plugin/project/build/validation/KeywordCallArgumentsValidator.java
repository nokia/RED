/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.libraries.ArgumentsDescriptor.Argument;
import org.rf.ide.core.libraries.ArgumentsDescriptor.InvalidArgumentsDescriptorException;
import org.rf.ide.core.testdata.model.table.exec.descs.CallArgumentsBinder.ArgumentsProblemFoundException;
import org.rf.ide.core.testdata.model.table.exec.descs.CallArgumentsBinder.BindedCallSiteArguments;
import org.rf.ide.core.testdata.model.table.exec.descs.CallArgumentsBinder.RobotTokenAsArgExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.CallArgumentsBinder.TaggedCallSiteArguments;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;

import com.google.common.base.Splitter;
import com.google.common.collect.Range;
import com.google.common.collect.Streams;

/**
 * @author Michal Anglart
 */
class KeywordCallArgumentsValidator implements ModelUnitValidator {

    private final FileValidationContext validationContext;

    private final RobotToken definingToken;

    private final ValidationReportingStrategy reporter;

    private final ArgumentsDescriptor descriptor;

    private final List<RobotToken> arguments;

    KeywordCallArgumentsValidator(final FileValidationContext validationContext, final RobotToken definingToken,
            final ValidationReportingStrategy reporter, final ArgumentsDescriptor descriptor,
            final List<RobotToken> arguments) {
        this.validationContext = validationContext;
        this.definingToken = definingToken;
        this.reporter = reporter;
        this.descriptor = descriptor;
        this.arguments = arguments;
    }

    @Override
    public void validate(final IProgressMonitor monitor) {
        try {
            final RobotTokenAsArgExtractor extractor = new RobotTokenAsArgExtractor();

            validateDescriptor();

            final Map<String, Argument> argsByNames = groupDescriptorArgumentsByNames();
            final TaggedCallSiteArguments<RobotToken> taggedArguments = tagArguments(extractor, argsByNames);

            validatePositionalAndNamedOrder(taggedArguments);

            final BindedCallSiteArguments<RobotToken> bindedArguments = BindedCallSiteArguments
                    .bindArguments(extractor, descriptor, arguments, argsByNames, taggedArguments);

            validatePositionalDuplicatedByNamedArgument(taggedArguments, bindedArguments);
            validateNumberOfArgs(taggedArguments);
            validateNamedDuplicatedByNamed(taggedArguments);

            if (taggedArguments.containsCollectionArgument()) {
                validateCollectionArguments(taggedArguments, bindedArguments);
            } else {
                validateAllRequiredAreProvided(bindedArguments);
                validateNoKeywordUnexpectedIsProvided(bindedArguments);
            }

        } catch (final ArgumentsProblemFoundException e) {
            // nothing to do just breaks one of validation step
        }
    }

    protected String getKeywordName() {
        return definingToken.getText();
    }

    protected ProblemPosition getKeywordProblemPosition() {
        return new ProblemPosition(definingToken.getLineNumber(), Range.closed(definingToken.getStartOffset(),
                definingToken.getStartOffset() + definingToken.getText().length()));
    }

    private void validateDescriptor() {
        try {
            descriptor.validate(validationContext.getVersion());
        } catch (final InvalidArgumentsDescriptorException e) {
            final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.INVALID_ARGUMENTS_DESCRIPTOR)
                    .formatMessageWith(getKeywordName(), e.getMessage());
            reporter.handleProblem(problem, validationContext.getFile(), getKeywordProblemPosition());

            throw new ArgumentsProblemFoundException();
        }
    }

    protected final Map<String, Argument> groupDescriptorArgumentsByNames() {
        return Streams.stream(descriptor)
                .filter(arg -> arg.isRequired() || arg.isDefault() || arg.isKeywordOnly())
                .collect(toMap(Argument::getName, arg -> arg));
    }

    protected final TaggedCallSiteArguments<RobotToken> tagArguments(final RobotTokenAsArgExtractor extractor,
            final Map<String, Argument> argsByNames) {
        return TaggedCallSiteArguments.tagArguments(extractor, arguments, argsByNames,
                descriptor.supportsKwargs() || descriptor.supportsKeywordOnlyArguments());
    }

    private void validatePositionalAndNamedOrder(final TaggedCallSiteArguments<RobotToken> taggedArguments) {
        boolean orderIsWrong = false;
        boolean foundNamed = false;
        for (final RobotToken arg : arguments) {
            if (taggedArguments.isNamedArgument(arg) || taggedArguments.isKeywordArgument(arg)) {
                foundNamed = true;

            } else if (foundNamed && !taggedArguments.isKeywordArgument(arg)) {
                final String additionalMsg = taggedArguments.isPositionalLookingLikeNamed(arg)
                        ? ". Although this argument looks like named one, it isn't because there is no '"
                                + getName(arg.getText()) + "' argument in the keyword definition"
                        : "";
                final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.POSITIONAL_ARGUMENT_AFTER_NAMED)
                        .formatMessageWith(additionalMsg);
                reporter.handleProblem(problem, validationContext.getFile(), arg);
                orderIsWrong = true;
            }
        }
        if (orderIsWrong) {
            throw new ArgumentsProblemFoundException();
        }
    }

    private void validatePositionalDuplicatedByNamedArgument(final TaggedCallSiteArguments<RobotToken> taggedArguments,
            final BindedCallSiteArguments<RobotToken> bindedArguments) {

        boolean isPositionalDuplicatedByNamed = false;

        for (final Argument arg : descriptor) {
            final List<RobotToken> values = bindedArguments.callSiteArgumentsOf(arg);

            if ((arg.isRequired() || arg.isDefault()) && values.size() > 1
                    && taggedArguments.containsArgumentPassedPositionallyAndByName(values)) {
                // it is ok to pass same arguments multiple times by name e.g. call arg=1 arg=2
                final String firstValue = values.get(0).getText();
                for (int i = 1; i < values.size(); i++) {
                    final RobotToken argToken = values.get(i);
                    final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.MULTIPLE_MATCH_TO_SINGLE_ARG)
                            .formatMessageWith(arg.getName(), firstValue);
                    reporter.handleProblem(problem, validationContext.getFile(), argToken);
                    isPositionalDuplicatedByNamed = true;
                }
            }
        }

        if (isPositionalDuplicatedByNamed) {
            throw new ArgumentsProblemFoundException();
        }
    }

    private void validateNamedDuplicatedByNamed(final TaggedCallSiteArguments<RobotToken> taggedArguments) {
        for (final RobotToken callSiteArg : arguments) {
            if (taggedArguments.isNamedDuplicateArgument(callSiteArg)) {
                reporter.handleProblem(RobotProblem.causedBy(ArgumentProblem.OVERRIDDEN_NAMED_ARGUMENT)
                        .formatMessageWith(getName(callSiteArg.getText())), validationContext.getFile(), callSiteArg);
            }
        }
    }

    protected final void validateNumberOfArgs(final TaggedCallSiteArguments<RobotToken> taggedArguments) {
        final boolean supportsKeyword = descriptor.supportsKwargs() || descriptor.supportsKeywordOnlyArguments();
        final int actual = supportsKeyword
                ? taggedArguments.getNumberOfNonKeywordArguments(arguments)
                : taggedArguments.getNumberOfArguments(arguments);
        final Range<Integer> possibleRange = supportsKeyword
                ? descriptor.getPossibleNumberOfNonKwargsArguments()
                : descriptor.getPossibleNumberOfArguments();

        if (!possibleRange.contains(actual) && !taggedArguments.containsCollectionArgument()) {
            // there can be any number of arguments when collection is passed, so we
            // cannot say if that's ok or not
            final String element = supportsKeyword ? "non-named argument" : "argument";
            reporter.handleProblem(RobotProblem.causedBy(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS)
                    .formatMessageWith(getKeywordName(), getRangesInfo(possibleRange, element), actual,
                            toBeInProperForm(actual)),
                    validationContext.getFile(), getKeywordProblemPosition());

            throw new ArgumentsProblemFoundException();

        } else if (possibleRange.hasUpperBound() && taggedArguments.containsCollectionArgument()) {
            final int noOfNonCollectionArgs = taggedArguments.getNumberOfNonCollectionArguments(arguments);
            if (possibleRange.upperEndpoint() < noOfNonCollectionArgs) {
                // even if there is a collection it cannot have negative number of elements, so it
                // may be also a problem when there is upper bound on number of arguments
                final Range<Integer> actualRange = taggedArguments.containsListArgument()
                        ? Range.atLeast(noOfNonCollectionArgs)
                        : Range.closed(noOfNonCollectionArgs, noOfNonCollectionArgs);

                final String element = supportsKeyword ? "non-named argument" : "argument";
                reporter.handleProblem(RobotProblem.causedBy(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS)
                        .formatMessageWith(getKeywordName(), getRangesInfo(possibleRange, element),
                                getRangesInfo(actualRange, ""), toBeInProperForm(actual)),
                        validationContext.getFile(), getKeywordProblemPosition());

                throw new ArgumentsProblemFoundException();
            }
        }
    }

    private void validateAllRequiredAreProvided(final BindedCallSiteArguments<RobotToken> bindedArguments) {
        final List<Argument> missingArguments = descriptor.stream()
                .filter(arg -> arg.isRequired() || arg.isKeywordOnlyRequired())
                .filter(arg -> bindedArguments.callSiteArgumentsOf(arg).isEmpty())
                .collect(toList());
        if (!missingArguments.isEmpty()) {
            final boolean hasKeywordOnly = missingArguments.stream().anyMatch(Argument::isKeywordOnlyRequired);
            final String argType = hasKeywordOnly ? " keyword-only" : "";
            final String args = missingArguments.stream().map(Argument::getName).collect(joining(", ", "(", ")"));

            final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.NO_VALUE_PROVIDED_FOR_REQUIRED_ARG)
                    .formatMessageWith(getKeywordName(),
                            args + argType + toPluralIfNeeded(" argument", missingArguments.size()));
            reporter.handleProblem(problem, validationContext.getFile(), getKeywordProblemPosition());
        }
    }

    private void validateNoKeywordUnexpectedIsProvided(final BindedCallSiteArguments<RobotToken> bindedArguments) {
        if (!descriptor.supportsKwargs() && descriptor.supportsKeywordOnlyArguments()) {
            final List<RobotToken> tokensWithMissingBinding = arguments.stream()
                    .filter(token -> bindedArguments.definitionArgumentsOf(token).isEmpty())
                    .collect(toList());

            for (final RobotToken argToken : tokensWithMissingBinding) {
                final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.UNEXPECTED_NAMED_ARGUMENT)
                        .formatMessageWith(getName(argToken.getText()));
                reporter.handleProblem(problem, validationContext.getFile(), argToken);
            }
        }
    }

    private void validateCollectionArguments(final TaggedCallSiteArguments<RobotToken> taggedArguments,
            final BindedCallSiteArguments<RobotToken> bindedArguments) {

        boolean listValidated = false;
        boolean listProblemFound = false;
        boolean dictValidated = false;
        boolean dictProblemFound = false;
        for (int i = arguments.size() - 1; i >= 0; i--) {
            final RobotToken callSiteArg = arguments.get(i);

            // all lists/dictionaries will be reported to be empty except the last ones
            if (taggedArguments.isListArgument(callSiteArg)
                    && (listValidated && listProblemFound || dictValidated && dictProblemFound)
                    || taggedArguments.isDictionaryArgument(callSiteArg) && dictValidated && dictProblemFound) {
                final String type = taggedArguments.isListArgument(callSiteArg) ? "List" : "Dictionary";
                final RobotProblem problem = RobotProblem
                        .causedBy(ArgumentProblem.COLLECTION_ARGUMENT_SHOULD_PROVIDE_ARGS)
                        .formatMessageWith(type, callSiteArg.getText(), "has to be empty");
                reporter.handleProblem(problem, validationContext.getFile(), callSiteArg);

            } else {
                final List<String> nonBindedRequired = new ArrayList<>();
                final List<String> nonBindedDefaults = new ArrayList<>();
                final List<String> nonBindedKeywordRequired = new ArrayList<>();
                final List<String> nonBindedKeywordDefaults = new ArrayList<>();

                for (final Argument arg : descriptor) {
                    if (bindedArguments.callSiteArgumentsOf(arg).isEmpty()) {
                        if (arg.isRequired()) {
                            nonBindedRequired.add(arg.getName());
                        } else if (arg.isDefault()) {
                            nonBindedDefaults.add(arg.getName());
                        } else if (arg.isKeywordOnlyRequired()) {
                            nonBindedKeywordRequired.add(arg.getName());
                        } else if (arg.isKeywordOnlyDefault()) {
                            nonBindedKeywordDefaults.add(arg.getName());
                        }
                    }
                }

                if (taggedArguments.isListArgument(callSiteArg)) {
                    final Optional<RobotProblem> problem = findListArgumentProblem(callSiteArg,
                            nonBindedRequired.size(), nonBindedDefaults.size());
                    problem.ifPresent(p -> reporter.handleProblem(p, validationContext.getFile(), callSiteArg));
                    listProblemFound = problem.isPresent();
                    listValidated = true;

                } else if (taggedArguments.isDictionaryArgument(callSiteArg)) {
                    final List<String> forbiddenArgs = taggedArguments.getPositionalArguments()
                            .stream()
                            .flatMap(token -> bindedArguments.definitionArgumentsOf(token).stream())
                            .filter(arg -> arg.isRequired() || arg.isDefault())
                            .map(Argument::getName)
                            .distinct()
                            .collect(toList());

                    final Optional<RobotProblem> problem = validateDictionaryArgument(callSiteArg, nonBindedRequired,
                            nonBindedDefaults, nonBindedKeywordRequired, nonBindedKeywordDefaults, forbiddenArgs);
                    problem.ifPresent(p -> reporter.handleProblem(p, validationContext.getFile(), callSiteArg));
                    dictProblemFound = problem.isPresent();
                    dictValidated = true;
                }
            }
        }

        if (listValidated && !dictValidated) {
            // if there is no dictionary then lists cannot provide keyword-only arguments so we need
            // to raise
            // an error if there are missing keyword only arguments
            validateAllKeywordOnlyRequiredAreProvided(bindedArguments);
        }
    }

    private void validateAllKeywordOnlyRequiredAreProvided(final BindedCallSiteArguments<RobotToken> bindedArguments) {
        final List<Argument> missingArguments = descriptor.stream()
                .filter(Argument::isKeywordOnlyRequired)
                .filter(arg -> bindedArguments.callSiteArgumentsOf(arg).isEmpty())
                .collect(toList());
        if (!missingArguments.isEmpty()) {
            final String args = missingArguments.stream().map(Argument::getName).collect(joining(", ", "(", ")"));

            final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.NO_VALUE_PROVIDED_FOR_REQUIRED_ARG)
                    .formatMessageWith(getKeywordName(),
                            args + " keyword-only" + toPluralIfNeeded(" argument", missingArguments.size()));
            reporter.handleProblem(problem, validationContext.getFile(), getKeywordProblemPosition());
        }
    }

    private Optional<RobotProblem> findListArgumentProblem(final RobotToken collectionArgument,
            final int noOfNonBindedRequired, final int noOfNonBindedDefaults) {

        final Range<Integer> range = descriptor.supportsVarargs() ? Range.atLeast(noOfNonBindedRequired)
                : Range.closed(noOfNonBindedRequired, noOfNonBindedRequired + noOfNonBindedDefaults);

        final String description;
        if (!range.hasUpperBound() && noOfNonBindedRequired == 0) {
            description = null;
        } else if (range.hasUpperBound() && noOfNonBindedRequired == 0 && noOfNonBindedDefaults == 0) {
            description = "has to be empty";
        } else {
            description = "has to contain " + getRangesInfo(range, "item");
        }
        return Optional.ofNullable(description)
                .map(desc -> RobotProblem.causedBy(ArgumentProblem.COLLECTION_ARGUMENT_SHOULD_PROVIDE_ARGS)
                        .formatMessageWith("List", collectionArgument.getText(), desc));
    }

    private Optional<RobotProblem> validateDictionaryArgument(final RobotToken collectionArgument,
            final List<String> nonBindedRequired, final List<String> nonBindedDefaults,
            final List<String> nonBindedKeywordRequired, final List<String> nonBindedKeywordDefaults,
            final List<String> forbiddenArgs) {

        final List<String> allRequired = new ArrayList<>();
        allRequired.addAll(nonBindedRequired);
        allRequired.addAll(nonBindedKeywordRequired);
        final List<String> allDefaults = new ArrayList<>();
        allDefaults.addAll(nonBindedDefaults);
        allDefaults.addAll(nonBindedKeywordDefaults);

        final int required = allRequired.size();
        final int defaults = allDefaults.size();
        final int noOfForbidden = forbiddenArgs.size();

        final Range<Integer> range = descriptor.supportsKwargs() ? Range.atLeast(required)
                : Range.closed(required, required + defaults);

        String description;
        if (!range.hasUpperBound() && required == 0 && noOfForbidden == 0) {
            description = null;
        } else if (!range.hasUpperBound() && required == 0) {
            description = "cannot have " + toPluralIfNeeded("key", noOfForbidden) + ": ("
                    + String.join(", ", forbiddenArgs) + ")";
        } else if (range.hasUpperBound() && required == 0 && defaults == 0) {
            description = "has to be empty";
        } else {
            final List<String> details = new ArrayList<>();
            if (required > 0) {
                details.add("required " + toPluralIfNeeded("key", required) + ": ("
                        + String.join(", ", allRequired) + ")");
            }
            if (range.hasUpperBound() && defaults > 0) {
                details.add("possible " + toPluralIfNeeded("key", defaults) + ": ("
                        + String.join(", ", allDefaults) + ")");
            }
            if (noOfForbidden > 0) {
                details.add("forbidden " + toPluralIfNeeded("key", noOfForbidden) + ": ("
                        + String.join(", ", forbiddenArgs) + ")");
            }

            description = "has to contain " + getRangesInfo(range, "mapping");
            if (!details.isEmpty()) {
                details.set(0, firstToUpper(details.get(0)));
                description += ". " + String.join(", ", details);
            }
        }
        return Optional.ofNullable(description)
                .map(desc -> RobotProblem.causedBy(ArgumentProblem.COLLECTION_ARGUMENT_SHOULD_PROVIDE_ARGS)
                        .formatMessageWith("Dictionary", collectionArgument.getText(), desc));
    }

    private static String getName(final String arg) {
        return Splitter.on('=').limit(2).splitToList(arg).get(0);
    }

    private static String firstToUpper(final String msg) {
        return msg.substring(0, 1).toUpperCase() + msg.substring(1);
    }

    private static String getRangesInfo(final Range<Integer> range, final String item) {
        final int minArgs = range.lowerEndpoint();
        if (!range.hasUpperBound()) {
            return "at least " + minArgs + toPluralIfNeeded(" " + item, minArgs);
        } else if (range.lowerEndpoint().equals(range.upperEndpoint())) {
            return minArgs + toPluralIfNeeded(" " + item, minArgs);
        } else {
            final int maxArgs = range.upperEndpoint();
            return "from " + minArgs + " to " + maxArgs + toPluralIfNeeded(" " + item, maxArgs);
        }
    }

    private static String toBeInProperForm(final int amount) {
        return amount == 1 ? "is" : "are";
    }

    private static String toPluralIfNeeded(final String noun, final int amount) {
        if (noun.isEmpty() || noun.equals(" ")) {
            return "";
        }
        return amount == 1 ? noun : noun + "s";
    }
}
