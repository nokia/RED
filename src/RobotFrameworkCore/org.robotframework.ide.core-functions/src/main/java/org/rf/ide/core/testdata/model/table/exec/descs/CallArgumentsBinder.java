/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs;

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

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.libraries.ArgumentsDescriptor.Argument;
import org.rf.ide.core.libraries.ArgumentsDescriptor.InvalidArgumentsDescriptorException;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.base.Splitter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Streams;

/**
 * @author Michal Anglart
 */
public class CallArgumentsBinder<T> {

    public static boolean canBind(final RobotVersion version, final ArgumentsDescriptor descriptor) {
        try {
            descriptor.validate(version);
            return true;
        } catch (final InvalidArgumentsDescriptorException e) {
            return false;
        }
    }

    private final CallSiteArgumentExtractor<T> extractor;

    private final ArgumentsDescriptor descriptor;

    private TaggedCallSiteArguments<T> taggedArguments;

    private BindedCallSiteArguments<T> bindings;


    public CallArgumentsBinder(final CallSiteArgumentExtractor<T> extractor, final ArgumentsDescriptor descriptor) {
        this.extractor = extractor;
        this.descriptor = descriptor;
    }

    public boolean hasBindings() {
        return bindings != null;
    }

    public void bind(final List<T> arguments) {
        try {
            this.taggedArguments = tagArguments(arguments, groupDescriptorArgumentsByNames());
            validatePositionalAndNamedOrder(arguments, taggedArguments);

            this.bindings = BindedCallSiteArguments.bindArguments(extractor, descriptor, arguments,
                    groupDescriptorArgumentsByNames(), taggedArguments);

            validatePositionalDuplicatedByNamedArgument(taggedArguments, bindings);
            validateNumberOfArgs(arguments, taggedArguments);

        } catch (final ArgumentsProblemFoundException e) {
            this.bindings = null;
        }
    }

    public Optional<T> getLastBindedTo(final Argument arg) {
        if (bindings == null) {
            return Optional.empty();
        }
        return bindings.callSiteArgumentsOf(arg).stream().reduce((a, b) -> b);
    }

    public Optional<String> getLastValueBindedTo(final Argument arg) {
        return getLastBindedTo(arg).map(extractor::getText).map(a -> CallArgumentsBinder.getValue(arg, a));
    }

    public List<String> getValuesBindedTo(final Argument arg) {
        if (bindings == null) {
            return new ArrayList<>();
        }
        final List<T> bindedArgs = bindings.callSiteArgumentsOf(arg);
        return bindedArgs.stream()
                .filter(a -> !taggedArguments.isNamedDuplicateArgument(a))
                .map(a -> taggedArguments.isNamedArgument(a) ? getValue(arg, extractor.getText(a))
                        : extractor.getText(a))
                .collect(toList());
    }

    private Map<String, Argument> groupDescriptorArgumentsByNames() {
        return Streams.stream(descriptor)
                .filter(arg -> arg.isRequired() || arg.isDefault() || arg.isKeywordOnly())
                .collect(toMap(Argument::getName, arg -> arg));
    }

    private TaggedCallSiteArguments<T> tagArguments(final List<T> arguments, final Map<String, Argument> argsByNames) {
        return TaggedCallSiteArguments.tagArguments(extractor, arguments, argsByNames,
                descriptor.supportsKwargs() || descriptor.supportsKeywordOnlyArguments());
    }

    private void validatePositionalAndNamedOrder(final List<T> arguments,
            final TaggedCallSiteArguments<T> taggedArguments) {
        boolean foundNamed = false;
        for (final T arg : arguments) {
            if (taggedArguments.isNamedArgument(arg) || taggedArguments.isKeywordArgument(arg)) {
                foundNamed = true;

            } else if (foundNamed && !taggedArguments.isKeywordArgument(arg)) {
                throw new ArgumentsProblemFoundException();
            }
        }
    }

    private void validatePositionalDuplicatedByNamedArgument(final TaggedCallSiteArguments<T> taggedArguments,
            final BindedCallSiteArguments<T> bindedArguments) {

        for (final Argument arg : descriptor) {
            final List<T> callSiteArgs = bindedArguments.callSiteArgumentsOf(arg);
            if ((arg.isRequired() || arg.isDefault()) && callSiteArgs.size() > 1
                    && taggedArguments.containsArgumentPassedPositionallyAndByName(callSiteArgs)) {
                throw new ArgumentsProblemFoundException();
            }
        }
    }

    private void validateNumberOfArgs(final List<T> arguments, final TaggedCallSiteArguments<T> taggedArguments) {
        final boolean supportsKeyword = descriptor.supportsKwargs() || descriptor.supportsKeywordOnlyArguments();
        final int actual = supportsKeyword ? taggedArguments.getNumberOfNonKeywordArguments(arguments)
                : taggedArguments.getNumberOfArguments(arguments);
        final Range<Integer> possibleRange = supportsKeyword ? descriptor.getPossibleNumberOfNonKwargsArguments()
                : descriptor.getPossibleNumberOfArguments();

        if (!possibleRange.contains(actual) && !taggedArguments.containsCollectionArgument()) {
            throw new ArgumentsProblemFoundException();

        } else if (possibleRange.hasUpperBound() && taggedArguments.containsCollectionArgument()
                && possibleRange.upperEndpoint() < taggedArguments.getNumberOfNonCollectionArguments(arguments)) {
            throw new ArgumentsProblemFoundException();
        }
    }

    private static String getName(final String arg) {
        return Splitter.on('=').limit(2).splitToList(arg).get(0);
    }

    private static String getValue(final Argument arg, final String callSiteArg) {
        return callSiteArg.startsWith(arg.getName() + "=")
                ? Splitter.on('=').limit(2).splitToList(callSiteArg).stream().reduce((a, b) -> b).get()
                : callSiteArg;
    }

    public static class TaggedCallSiteArguments<T> {

        private final SymmetricRelation<T, ArgumentTag> bindings;

        private TaggedCallSiteArguments(final SymmetricRelation<T, ArgumentTag> bindings) {
            this.bindings = bindings;
        }

        public boolean isPositionalLookingLikeNamed(final T argument) {
            return bindings.getLeftRelated(argument).contains(ArgumentTag.CONTAINS_EQUALS);
        }

        public boolean isNamedArgument(final T argument) {
            return bindings.getLeftRelated(argument).contains(ArgumentTag.NAMED);
        }

        public boolean isNamedDuplicateArgument(final T argument) {
            return bindings.getLeftRelated(argument).contains(ArgumentTag.NAMED_DUPLICATE);
        }

        public boolean isListArgument(final T argument) {
            return bindings.getLeftRelated(argument).contains(ArgumentTag.LIST);
        }

        public boolean isDictionaryArgument(final T argument) {
            return bindings.getLeftRelated(argument).contains(ArgumentTag.DICTIONARY);
        }

        public boolean isKeywordArgument(final T argument) {
            return bindings.getLeftRelated(argument).contains(ArgumentTag.KEYWORD);
        }

        public boolean isKeywordNonDictionaryArgument(final T argument) {
            final List<ArgumentTag> tags = bindings.getLeftRelated(argument);
            return tags.contains(ArgumentTag.KEYWORD) && !tags.contains(ArgumentTag.DICTIONARY);
        }

        public boolean containsArgumentPassedPositionallyAndByName(final List<T> arguments) {
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

        public int getNumberOfNonCollectionArguments(final List<T> arguments) {
            return (int) arguments.stream()
                    .filter(arg -> !bindings.getLeftRelated(arg).contains(ArgumentTag.COLLECTION))
                    .filter(arg -> !bindings.getLeftRelated(arg).contains(ArgumentTag.KEYWORD))
                    .count();
        }

        public int getNumberOfArguments(final List<T> arguments) {
            return (int) arguments.stream()
                    .map(bindings::getLeftRelated)
                    .filter(tags -> !tags.contains(ArgumentTag.LIST))
                    .filter(tags -> !tags.contains(ArgumentTag.NAMED_DUPLICATE))
                    .count();
        }

        public int getNumberOfNonKeywordArguments(final List<T> arguments) {
            return (int) arguments.stream()
                    .map(bindings::getLeftRelated)
                    .filter(tags -> !tags.contains(ArgumentTag.KEYWORD))
                    .filter(tags -> !tags.contains(ArgumentTag.LIST))
                    .filter(tags -> !tags.contains(ArgumentTag.NAMED_DUPLICATE))
                    .count();
        }

        public List<T> getPositionalArguments() {
            return bindings.getRightRelated(ArgumentTag.POSITIONAL);
        }

        public List<T> getNamedArguments() {
            return bindings.getRightRelated(ArgumentTag.NAMED);
        }

        public static <T> TaggedCallSiteArguments<T> tagArguments(final CallSiteArgumentExtractor<T> extractor,
                final List<T> callSiteArguments, final Map<String, Argument> argsByNames,
                final boolean kwargsAreSupported) {

            final SymmetricRelation<T, ArgumentTag> bindings = new SymmetricRelation<>();
            final Map<String, T> previousWithSameName = new HashMap<>();
            for (final T arg : callSiteArguments) {

                if (extractor.isCleanList(arg)) {
                    bindings.bind(arg, ArgumentTag.LIST);
                    bindings.bind(arg, ArgumentTag.COLLECTION);
                    bindings.bind(arg, ArgumentTag.POSITIONAL);

                } else if (extractor.isCleanDictionary(arg)) {
                    bindings.bind(arg, ArgumentTag.DICTIONARY);
                    bindings.bind(arg, ArgumentTag.COLLECTION);
                    bindings.bind(arg, ArgumentTag.NAMED);
                    if (kwargsAreSupported) {
                        bindings.bind(arg, ArgumentTag.KEYWORD);
                    }

                } else {
                    final String argument = extractor.getText(arg);
                    if (argument.contains("=")) {
                        final String name = getName(argument);
                        if (name.endsWith("\\")) {
                            // '=' character is escaped
                            bindings.bind(arg, ArgumentTag.POSITIONAL);

                        } else if (argsByNames.keySet().contains(name)) {
                            bindings.bind(arg, argsByNames.get(name).isKeywordOnly() ? ArgumentTag.KEYWORD : ArgumentTag.NAMED);

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
            return new TaggedCallSiteArguments<>(bindings);
        }
    }

    public static class BindedCallSiteArguments<T> {

        private final SymmetricRelation<Argument, T> bindings;

        private BindedCallSiteArguments(final SymmetricRelation<Argument, T> bindings) {
            this.bindings = bindings;
        }

        public List<T> callSiteArgumentsOf(final Argument definitionArgument) {
            return bindings.getLeftRelated(definitionArgument);
        }

        public List<Argument> definitionArgumentsOf(final T token) {
            return bindings.getRightRelated(token);
        }

        public static <T> BindedCallSiteArguments<T> bindArguments(final CallSiteArgumentExtractor<T> extractor,
                final ArgumentsDescriptor descriptor, final List<T> arguments, final Map<String, Argument> argsByNames,
                final TaggedCallSiteArguments<T> taggedArguments) {

            final List<T> positional = taggedArguments.getPositionalArguments();
            final List<T> named = taggedArguments.getNamedArguments();

            final SymmetricRelation<Argument, T> mapping = new SymmetricRelation<>();
            bindPositionalArguments(descriptor, mapping, positional, taggedArguments);
            bindNamedArguments(extractor, mapping, named, argsByNames, taggedArguments);
            bindKeywordArguments(extractor, descriptor, mapping, arguments, argsByNames, taggedArguments);
            return new BindedCallSiteArguments<>(mapping);
        }

        private static <T> void bindPositionalArguments(final ArgumentsDescriptor descriptor,
                final SymmetricRelation<Argument, T> mapping, final List<T> positional,
                final TaggedCallSiteArguments<T> taggedArguments) {

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

        private static <T> int indexOfLastList(final List<T> positional,
                final TaggedCallSiteArguments<T> taggedArguments) {
            final int lastListIndex = Iterables.indexOf(Lists.reverse(positional), taggedArguments::isListArgument);
            if (lastListIndex == -1) {
                return -1;
            }
            return positional.size() - 1 - lastListIndex;
        }

        private static <T> void bindNamedArguments(final CallSiteArgumentExtractor<T> extractor,
                final SymmetricRelation<Argument, T> mapping, final List<T> named,
                final Map<String, Argument> argsByNames, final TaggedCallSiteArguments<T> taggedArguments) {

            for (final T argToken : named) {
                if (!taggedArguments.isDictionaryArgument(argToken) && taggedArguments.isNamedArgument(argToken)) {
                    final Argument arg = argsByNames.get(getName(extractor.getText(argToken)));
                    mapping.bind(arg, argToken);
                }
            }
        }

        private static <T> void bindKeywordArguments(final CallSiteArgumentExtractor<T> extractor,
                final ArgumentsDescriptor descriptor, final SymmetricRelation<Argument, T> mapping,
                final List<T> arguments,
                final Map<String, Argument> argsByNames, final TaggedCallSiteArguments<T> taggedArguments) {
            for (final T argToken : arguments) {
                if (taggedArguments.isKeywordNonDictionaryArgument(argToken)) {
                    final Argument arg = argsByNames.get(getName(extractor.getText(argToken)));
                    if (arg != null && arg.isKeywordOnly()) {
                        mapping.bind(arg, argToken);
                    } else if (descriptor.supportsKwargs()) {
                        mapping.bind(descriptor.getKwargArgument().get(), argToken);
                    }
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

    public static interface CallSiteArgumentExtractor<T> {

        public static final Pattern LIST_PATTERN = Pattern.compile("^@\\{[\\w ]+\\}$");

        public static final Pattern DICT_PATTERN = Pattern.compile("^&\\{[\\w ]+\\}$");

        String getText(T arg);

        boolean isCleanList(T arg);

        boolean isCleanDictionary(T arg);
    }

    public static class StringAsArgExtractor implements CallSiteArgumentExtractor<String> {

        @Override
        public String getText(final String arg) {
            return arg;
        }

        @Override
        public boolean isCleanList(final String arg) {
            return arg != null && LIST_PATTERN.matcher(arg).matches();
        }

        @Override
        public boolean isCleanDictionary(final String arg) {
            return arg != null && DICT_PATTERN.matcher(arg).matches();
        }
    }

    public static class RobotTokenAsArgExtractor implements CallSiteArgumentExtractor<RobotToken> {

        @Override
        public String getText(final RobotToken arg) {
            return arg.getText();
        }

        @Override
        public boolean isCleanList(final RobotToken arg) {
            return arg != null && arg.getTypes().contains(RobotTokenType.VARIABLES_LIST_DECLARATION)
                    && LIST_PATTERN.matcher(arg.getText()).matches();
        }

        @Override
        public boolean isCleanDictionary(final RobotToken arg) {
            return arg != null && arg.getTypes().contains(RobotTokenType.VARIABLES_DICTIONARY_DECLARATION)
                    && DICT_PATTERN.matcher(arg.getText()).matches();
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

    public static class ArgumentsProblemFoundException extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }
}
