/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.libraries.ArgumentsDescriptor.Argument;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ArgumentProblem;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Streams;

/**
 * @author Michal Anglart
 */
class KeywordCallArgumentsValidator implements ModelUnitValidator {

    private final IFile file;

    private final RobotToken definingToken;

    private final ValidationReportingStrategy reporter;

    protected final ArgumentsDescriptor descriptor;

    protected final List<RobotToken> arguments;

    KeywordCallArgumentsValidator(final IFile file, final RobotToken definingToken,
            final ValidationReportingStrategy reporter, final ArgumentsDescriptor descriptor,
            final List<RobotToken> arguments) {
        this.file = file;
        this.definingToken = definingToken;
        this.reporter = reporter;
        this.descriptor = descriptor;
        this.arguments = arguments;
    }

    @Override
    public void validate(final IProgressMonitor monitor) {
        try {
            validateDescriptor();

            final Map<String, Argument> argsByNames = Streams.stream(descriptor)
                    .filter(arg -> arg.isRequired() || arg.isDefault())
                    .collect(toMap(Argument::getName, arg -> arg));
            final TaggedCallSiteArguments taggedArguments = TaggedCallSiteArguments.tagArguments(arguments, argsByNames,
                    descriptor.supportsKwargs());

            validatePositionalAndNamedOrder(taggedArguments);

            final BindedCallSiteArguments bindedArguments = BindedCallSiteArguments.bindArguments(descriptor, arguments,
                    argsByNames, taggedArguments);

            validatePositionalDuplicatedByNamedArgument(taggedArguments, bindedArguments);
            validateNumberOfArgs(taggedArguments);
            validateNamedDuplicatedByNames(taggedArguments);

            if (taggedArguments.containsCollectionArgument()) {
                validateCollectionArguments(taggedArguments, bindedArguments);
            } else {
                validateAllRequiredAreProvided(bindedArguments);
            }

        } catch (final ArgumentsProblemFoundException e) {
            // nothing to do just breaks one of validation step
        }
    }

    private void validateDescriptor() {
        if (!descriptor.isValid()) {
            final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.INVALID_ARGUMENTS_DESCRIPTOR)
                    .formatMessageWith(definingToken.getText());
            reporter.handleProblem(problem, file, definingToken);

            throw new ArgumentsProblemFoundException();
        }
    }

    private void validatePositionalAndNamedOrder(final TaggedCallSiteArguments taggedArguments) {
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
                reporter.handleProblem(problem, file, arg);
                orderIsWrong = true;
            }
        }
        if (orderIsWrong) {
            throw new ArgumentsProblemFoundException();
        }
    }

    private void validatePositionalDuplicatedByNamedArgument(final TaggedCallSiteArguments taggedArguments,
            final BindedCallSiteArguments bindedArguments) {

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
                    reporter.handleProblem(problem, file, argToken);
                    isPositionalDuplicatedByNamed = true;
                }
            }
        }

        if (isPositionalDuplicatedByNamed) {
            throw new ArgumentsProblemFoundException();
        }
    }

    private void validateNamedDuplicatedByNames(final TaggedCallSiteArguments taggedArguments) {
        for (final RobotToken callSiteArg : arguments) {
            if (taggedArguments.isNamedDuplicateArgument(callSiteArg)) {
                reporter.handleProblem(RobotProblem.causedBy(ArgumentProblem.OVERRIDDEN_NAMED_ARGUMENT)
                        .formatMessageWith(getName(callSiteArg.getText())), file, callSiteArg);
            }
        }
    }

    protected final void validateNumberOfArgs(final TaggedCallSiteArguments taggedArguments) {
        final int actual = descriptor.supportsKwargs() ? taggedArguments.getNumberOfNonKeywordArguments(arguments)
                : taggedArguments.getNumberOfArguments(arguments);
        final Range<Integer> possibleRange = descriptor.supportsKwargs()
                ? descriptor.getPossibleNumberOfNonKwargsArguments()
                : descriptor.getPossibleNumberOfArguments();

        if (!possibleRange.contains(actual) && !taggedArguments.containsCollectionArgument()) {
            // there can be any number of arguments when collection is passed, so we
            // cannot say if that's ok or not
            final String element = descriptor.supportsKwargs() ? "non-keyword argument" : "argument";
            reporter.handleProblem(RobotProblem.causedBy(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS)
                    .formatMessageWith(definingToken.getText(), getRangesInfo(possibleRange, element), actual,
                            toBeInProperForm(actual)),
                    file, definingToken);

            throw new ArgumentsProblemFoundException();

        } else if (possibleRange.hasUpperBound() && taggedArguments.containsCollectionArgument()) {
            final int noOfNonCollectionArgs = taggedArguments.getNumberOfNonCollectionArguments(arguments);
            if (possibleRange.upperEndpoint() < noOfNonCollectionArgs) {
                // even if there is a collection it cannot have negative number of elements, so it
                // may be also a problem when there is upper bound on number of arguments
                final Range<Integer> actualRange = taggedArguments.containsListArgument()
                        ? Range.atLeast(noOfNonCollectionArgs)
                        : Range.closed(noOfNonCollectionArgs, noOfNonCollectionArgs);

                final String element = descriptor.supportsKwargs() ? "non-keyword argument" : "argument";
                reporter.handleProblem(RobotProblem.causedBy(ArgumentProblem.INVALID_NUMBER_OF_PARAMETERS)
                        .formatMessageWith(definingToken.getText(), getRangesInfo(possibleRange, element),
                                getRangesInfo(actualRange, ""), toBeInProperForm(actual)),
                        file, definingToken);

                throw new ArgumentsProblemFoundException();
            }
        }
    }

    private void validateAllRequiredAreProvided(final BindedCallSiteArguments bindedArguments) {
        final List<String> missingArguments = descriptor.stream()
                .filter(Argument::isRequired)
                .filter(arg -> bindedArguments.callSiteArgumentsOf(arg).isEmpty())
                .map(Argument::getName)
                .collect(toList());
        if (!missingArguments.isEmpty()) {
            final RobotProblem problem = RobotProblem.causedBy(ArgumentProblem.NO_VALUE_PROVIDED_FOR_REQUIRED_ARG)
                    .formatMessageWith(definingToken.getText(), "(" + String.join(", ", missingArguments) + ") "
                            + toPluralIfNeeded("argument", missingArguments.size()));
            reporter.handleProblem(problem, file, definingToken);
        }
    }

    private void validateCollectionArguments(final TaggedCallSiteArguments taggedArguments,
            final BindedCallSiteArguments bindedArguments) {

        boolean listValidated = false;
        boolean listProblemFound = false;
        boolean dictValidated = false;
        boolean dictProblemFound = false;
        for (int i = arguments.size() - 1; i >= 0; i--) {
            final RobotToken callSiteArg = arguments.get(i);

            // all lists/dictionaries will be reported to be empty except the last ones
            if (taggedArguments.isListArgument(callSiteArg) && listValidated && listProblemFound
                    || taggedArguments.isDictionaryArgument(callSiteArg) && dictValidated && dictProblemFound) {
                final String type = taggedArguments.isListArgument(callSiteArg) ? "List" : "Dictionary";
                final RobotProblem problem = RobotProblem
                        .causedBy(ArgumentProblem.COLLECTION_ARGUMENT_SHOULD_PROVIDE_ARGS)
                        .formatMessageWith(type, callSiteArg.getText(), "has to be empty");
                reporter.handleProblem(problem, file, callSiteArg);

            } else {
                final List<String> nonBindedRequired = new ArrayList<>();
                final List<String> nonBindedDefaults = new ArrayList<>();

                for (final Argument arg : descriptor) {
                    if (bindedArguments.callSiteArgumentsOf(arg).isEmpty()) {
                        if (arg.isRequired()) {
                            nonBindedRequired.add(arg.getName());
                        } else if (arg.isDefault()) {
                            nonBindedDefaults.add(arg.getName());
                        }
                    }
                }

                if (taggedArguments.isListArgument(callSiteArg)) {
                    final Optional<RobotProblem> problem = findListArgumentProblem(callSiteArg,
                            nonBindedRequired.size(), nonBindedDefaults.size());
                    problem.ifPresent(p -> reporter.handleProblem(p, file, callSiteArg));
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
                            nonBindedDefaults, forbiddenArgs);
                    problem.ifPresent(p -> reporter.handleProblem(p, file, callSiteArg));
                    dictProblemFound = problem.isPresent();
                    dictValidated = true;
                }
            }
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
            final List<String> forbiddenArgs) {

        final int noOfNonBindedRequired = nonBindedRequired.size();
        final int noOfNonBindedDefaults = nonBindedDefaults.size();
        final int noOfForbidden = forbiddenArgs.size();

        final Range<Integer> range = descriptor.supportsKwargs() ? Range.atLeast(noOfNonBindedRequired)
                : Range.closed(noOfNonBindedRequired, noOfNonBindedRequired + noOfNonBindedDefaults);

        String description;
        if (!range.hasUpperBound() && noOfNonBindedRequired == 0 && noOfForbidden == 0) {
            description = null;
        } else if (!range.hasUpperBound() && noOfNonBindedRequired == 0) {
            description = "cannot have " + toPluralIfNeeded("key", noOfForbidden) + ": ("
                    + String.join(", ", forbiddenArgs) + ")";
        } else if (range.hasUpperBound() && noOfNonBindedRequired == 0 && noOfNonBindedDefaults == 0) {
            description = "has to be empty";
        } else {
            final List<String> details = new ArrayList<>();
            if (noOfNonBindedRequired > 0) {
                details.add("required " + toPluralIfNeeded("key", noOfNonBindedRequired) + ": ("
                        + String.join(", ", nonBindedRequired) + ")");
            }
            if (range.hasUpperBound() && noOfNonBindedDefaults > 0) {
                details.add("possible " + toPluralIfNeeded("key", noOfNonBindedDefaults) + ": ("
                        + String.join(", ", nonBindedDefaults) + ")");
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

    static class TaggedCallSiteArguments {

        private static final Pattern LIST_PATTERN = Pattern.compile("^@\\{[\\w ]+\\}$");

        private static final Pattern DICT_PATTERN = Pattern.compile("^&\\{[\\w ]+\\}$");

        private final SymmetricRelation<RobotToken, ArgumentTag> bindings;

        public TaggedCallSiteArguments(final SymmetricRelation<RobotToken, ArgumentTag> bindings) {
            this.bindings = bindings;
        }

        public boolean isPositionalLookingLikeNamed(final RobotToken argument) {
            return bindings.getLeftRelated(argument).contains(ArgumentTag.CONTAINS_EQUALS);
        }

        public boolean isNamedArgument(final RobotToken argument) {
            return bindings.getLeftRelated(argument).contains(ArgumentTag.NAMED);
        }

        public boolean isNamedDuplicateArgument(final RobotToken argument) {
            return bindings.getLeftRelated(argument).contains(ArgumentTag.NAMED_DUPLICATE);
        }

        public boolean isListArgument(final RobotToken argument) {
            return bindings.getLeftRelated(argument).contains(ArgumentTag.LIST);
        }

        public boolean isDictionaryArgument(final RobotToken argument) {
            return bindings.getLeftRelated(argument).contains(ArgumentTag.DICTIONARY);
        }

        public boolean isKeywordArgument(final RobotToken argument) {
            return bindings.getLeftRelated(argument).contains(ArgumentTag.KEYWORD);
        }

        public boolean isKeywordNonDictionaryArgument(final RobotToken argument) {
            final List<ArgumentTag> tags = bindings.getLeftRelated(argument);
            return tags.contains(ArgumentTag.KEYWORD) && !tags.contains(ArgumentTag.DICTIONARY);
        }

        public boolean containsArgumentPassedPositionallyAndByName(final List<RobotToken> arguments) {
            final Set<ArgumentTag> tags = arguments.stream()
                    .map(bindings::getLeftRelated)
                    .flatMap(List::stream)
                    .collect(toSet());
            return tags.contains(ArgumentTag.POSITIONAL) && tags.contains(ArgumentTag.NAMED);
        }

        public boolean containsCollectionArgument() {
            return !bindings.getRightRelated(ArgumentTag.COLLECTION).isEmpty();
        }

        public boolean containsListArgument() {
            return !bindings.getRightRelated(ArgumentTag.LIST).isEmpty();
        }

        public int getNumberOfNonCollectionArguments(final List<RobotToken> arguments) {
            return (int) arguments.stream()
                    .filter(arg -> !bindings.getLeftRelated(arg).contains(ArgumentTag.COLLECTION))
                    .filter(arg -> !bindings.getLeftRelated(arg).contains(ArgumentTag.KEYWORD))
                    .count();
        }

        public int getNumberOfArguments(final List<RobotToken> arguments) {
            return (int) arguments.stream()
                    .map(bindings::getLeftRelated)
                    .filter(tags -> !tags.contains(ArgumentTag.LIST))
                    .filter(tags -> !tags.contains(ArgumentTag.NAMED_DUPLICATE))
                    .count();
        }

        public int getNumberOfNonKeywordArguments(final List<RobotToken> arguments) {
            return (int) arguments.stream()
                    .map(bindings::getLeftRelated)
                    .filter(tags -> !tags.contains(ArgumentTag.KEYWORD))
                    .filter(tags -> !tags.contains(ArgumentTag.LIST))
                    .filter(tags -> !tags.contains(ArgumentTag.NAMED_DUPLICATE))
                    .count();
        }

        public List<RobotToken> getPositionalArguments() {
            return bindings.getRightRelated(ArgumentTag.POSITIONAL);
        }

        public List<RobotToken> getNamedArguments() {
            return bindings.getRightRelated(ArgumentTag.NAMED);
        }

        static TaggedCallSiteArguments tagArguments(final List<RobotToken> callSiteArguments,
                final Map<String, Argument> argsByNames, final boolean kwargsAreSupported) {

            final SymmetricRelation<RobotToken, ArgumentTag> bindings = new SymmetricRelation<>();
            final Map<String, RobotToken> previousWithSameName = new HashMap<>();
            for (final RobotToken arg : callSiteArguments) {

                if (isCleanList(arg)) {
                    bindings.bind(arg, ArgumentTag.LIST);
                    bindings.bind(arg, ArgumentTag.COLLECTION);
                    bindings.bind(arg, ArgumentTag.POSITIONAL);

                } else if (isCleanDictionary(arg)) {
                    bindings.bind(arg, ArgumentTag.DICTIONARY);
                    bindings.bind(arg, ArgumentTag.COLLECTION);
                    bindings.bind(arg, ArgumentTag.NAMED);
                    if (kwargsAreSupported) {
                        bindings.bind(arg, ArgumentTag.KEYWORD);
                    }

                } else {
                    final String argument = arg.getText();
                    if (argument.contains("=")) {
                        final String name = getName(argument);
                        if (name.endsWith("\\")) {
                            // '=' character is escaped
                            bindings.bind(arg, ArgumentTag.POSITIONAL);

                        } else if (argsByNames.keySet().contains(name)) {
                            bindings.bind(arg, ArgumentTag.NAMED);

                            if (previousWithSameName.get(name) != null) {
                                bindings.bind(previousWithSameName.get(name), ArgumentTag.NAMED_DUPLICATE);
                            }
                            previousWithSameName.put(name, arg);

                        } else if (kwargsAreSupported) {
                            bindings.bind(arg, ArgumentTag.KEYWORD);

                        } else {
                            bindings.bind(arg, ArgumentTag.POSITIONAL);
                            bindings.bind(arg, ArgumentTag.CONTAINS_EQUALS);
                        }
                    } else {
                        bindings.bind(arg, ArgumentTag.POSITIONAL);
                    }
                }
            }
            return new TaggedCallSiteArguments(bindings);
        }

        private static boolean isCleanList(final RobotToken token) {
            return token != null && token.getTypes().contains(RobotTokenType.VARIABLES_LIST_DECLARATION)
                    && LIST_PATTERN.matcher(token.getText()).matches();
        }

        private static boolean isCleanDictionary(final RobotToken token) {
            return token != null && token.getTypes().contains(RobotTokenType.VARIABLES_DICTIONARY_DECLARATION)
                    && DICT_PATTERN.matcher(token.getText()).matches();
        }
    }

    private static class BindedCallSiteArguments {

        private final SymmetricRelation<Argument, RobotToken> bindings;

        public BindedCallSiteArguments(final SymmetricRelation<Argument, RobotToken> bindings) {
            this.bindings = bindings;
        }

        public List<RobotToken> callSiteArgumentsOf(final Argument definitionArgument) {
            return bindings.getLeftRelated(definitionArgument);
        }

        public List<Argument> definitionArgumentsOf(final RobotToken token) {
            return bindings.getRightRelated(token);
        }

        private static BindedCallSiteArguments bindArguments(final ArgumentsDescriptor descriptor,
                final List<RobotToken> arguments, final Map<String, Argument> argsByNames,
                final TaggedCallSiteArguments taggedArguments) {

            final List<RobotToken> positional = taggedArguments.getPositionalArguments();
            final List<RobotToken> named = taggedArguments.getNamedArguments();

            final SymmetricRelation<Argument, RobotToken> mapping = new SymmetricRelation<>();
            bindPositionalArguments(descriptor, mapping, positional, taggedArguments);
            bindNamedArguments(mapping, named, argsByNames, taggedArguments);
            bindKeywordArguments(descriptor, arguments, taggedArguments, mapping);
            return new BindedCallSiteArguments(mapping);
        }

        private static void bindPositionalArguments(final ArgumentsDescriptor descriptor,
                final SymmetricRelation<Argument, RobotToken> mapping, final List<RobotToken> positional,
                final TaggedCallSiteArguments taggedArguments) {

            final int sizeWithoutVarArgsAndKwargs = descriptor.size() - (descriptor.supportsVarargs() ? 1 : 0)
                    - (descriptor.supportsKwargs() ? 1 : 0);

            final int lastListIndex = indexOfLastList(positional, taggedArguments);

            int i = 0;
            int j = 0;
            while (i < sizeWithoutVarArgsAndKwargs && j < lastListIndex) {
                if (!taggedArguments.isListArgument(positional.get(j))) {
                    mapping.bind(descriptor.get(i), positional.get(j));
                    i++;
                }
                j++;
            }
            if (lastListIndex >= 0) {
                j++;
            }
            for (; i < sizeWithoutVarArgsAndKwargs && j < positional.size(); i++, j++) {
                mapping.bind(descriptor.get(i), positional.get(j));
            }
            if (descriptor.supportsVarargs()) {
                for (; j < positional.size(); j++) {
                    mapping.bind(descriptor.getVarargArgument().get(), positional.get(j));
                }
            }
        }

        private static int indexOfLastList(final List<RobotToken> positional,
                final TaggedCallSiteArguments taggedArguments) {
            final int lastListIndex = Iterables.indexOf(Lists.reverse(positional), taggedArguments::isListArgument);
            if (lastListIndex == -1) {
                return -1;
            }
            return positional.size() - 1 - lastListIndex;
        }

        private static void bindNamedArguments(final SymmetricRelation<Argument, RobotToken> mapping,
                final List<RobotToken> named, final Map<String, Argument> argsByNames,
                final TaggedCallSiteArguments taggedArguments) {

            for (final RobotToken argToken : named) {
                if (!taggedArguments.isDictionaryArgument(argToken) && taggedArguments.isNamedArgument(argToken)) {
                    final Argument arg = argsByNames.get(getName(argToken.getText()));
                    mapping.bind(arg, argToken);
                }
            }
        }

        private static void bindKeywordArguments(final ArgumentsDescriptor descriptor, final List<RobotToken> arguments,
                final TaggedCallSiteArguments taggedArguments, final SymmetricRelation<Argument, RobotToken> mapping) {
            for (final RobotToken kwArg : arguments) {
                if (taggedArguments.isKeywordNonDictionaryArgument(kwArg)) {
                    mapping.bind(descriptor.getKwargArgument().get(), kwArg);
                }
            }
        }
    }

    private static class SymmetricRelation<L, R> {

        private final ArrayListMultimap<L, R> leftToRightMapping = ArrayListMultimap.create();

        private final ArrayListMultimap<R, L> rightToLeftMapping = ArrayListMultimap.create();

        public void bind(final L left, final R right) {
            leftToRightMapping.put(left, right);
            rightToLeftMapping.put(right, left);
        }

        public List<R> getLeftRelated(final L left) {
            return leftToRightMapping.get(left);
        }

        public List<L> getRightRelated(final R right) {
            return rightToLeftMapping.get(right);
        }
    }

    private static enum ArgumentTag {
        POSITIONAL,
        NAMED,
        NAMED_DUPLICATE,
        KEYWORD,
        CONTAINS_EQUALS,
        LIST,
        DICTIONARY,
        COLLECTION
    }

    static class ArgumentsProblemFoundException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ArgumentsProblemFoundException() {
            super();
        }
    }
}
