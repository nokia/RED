/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.libraries;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.rf.ide.core.libraries.ArgumentsDescriptor.Argument;
import org.rf.ide.core.testdata.model.RobotVersion;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.google.common.collect.Streams;

/**
 * @author Michal Anglart
 */
public class ArgumentsDescriptor implements Iterable<Argument> {

    private final List<Argument> arguments;

    private final int lastPositional;

    @VisibleForTesting
    ArgumentsDescriptor(final List<Argument> args, final int lastPositional) {
        this.arguments = args;
        this.lastPositional = lastPositional;
    }

    public static ArgumentsDescriptor createDescriptor(final String... args) {
        return createDescriptor(newArrayList(args));
    }

    public static ArgumentsDescriptor createDescriptor(final List<String> args) {
        if (args == null) {
            return new ArgumentsDescriptor(newArrayList(), -1);
        }

        int lastPositional = -1;
        boolean foundEndOfPositional = false;
        final List<Argument> arguments = newArrayList();
        for (final String arg : args) {
            final ArgumentType type;
            final String possiblyAnnotatedName;
            String value = null;
            if (arg.contains("=")) {
                type = foundEndOfPositional ? ArgumentType.KEYWORD_ONLY : ArgumentType.DEFAULT;
                final List<String> splitted = Splitter.on('=').splitToList(arg);
                possiblyAnnotatedName = splitted.get(0);
                value = String.join("=", splitted.subList(1, splitted.size()));

            } else if (arg.startsWith("**")) {
                type = ArgumentType.KWARG;
                possiblyAnnotatedName = arg.substring(2);

            } else if (arg.startsWith("*")) {
                type = ArgumentType.VARARG;
                possiblyAnnotatedName = arg.substring(1);
                foundEndOfPositional = true;

                if (possiblyAnnotatedName.isEmpty()) {
                    // this is omitted because it's a artificial * argument which only marks end of positional
                    lastPositional = arguments.size();
                    continue;
                }

            } else {
                type = foundEndOfPositional ? ArgumentType.KEYWORD_ONLY : ArgumentType.REQUIRED;
                possiblyAnnotatedName = arg;
            }

            String name = possiblyAnnotatedName;
            String annotation = null;

            if (possiblyAnnotatedName.contains(":")) {
                final List<String> splitted = Splitter.on(':').splitToList(possiblyAnnotatedName);
                name = splitted.get(0);
                annotation = String.join(":", splitted.subList(1, splitted.size()));
            }
            arguments.add(new Argument(type, name, annotation, value));
        }
        return new ArgumentsDescriptor(arguments, lastPositional);
    }

    @Override
    public Iterator<Argument> iterator() {
        return arguments.iterator();
    }

    public Stream<Argument> stream() {
        return arguments.stream();
    }

    public int size() {
        return arguments.size();
    }

    public Argument get(final int index) {
        return arguments.get(index);
    }

    public void validate(final RobotVersion robotVersion) {
        if (!Ordering.natural().isOrdered(arguments.stream().map(arg -> arg.type.order).collect(toList()))) {
            throw new InvalidArgumentsDescriptorException("Order of arguments is wrong");
        }
        if (arguments.stream().filter(Argument::isVarArg).count() > 1) {
            throw new InvalidArgumentsDescriptorException("There should be only one vararg");
        }
        if (arguments.stream().filter(Argument::isKwArg).count() > 1) {
            throw new InvalidArgumentsDescriptorException("There should be only one kwarg");
        }
        if (arguments.stream().map(Argument::getName).distinct().count() != arguments.size()) {
            throw new InvalidArgumentsDescriptorException("Argument names can't be duplicated");
        }
        if (robotVersion.isOlderThan(new RobotVersion(3, 1))
                && (arguments.stream().filter(Argument::isKeywordOnly).count() > 0 || lastPositional > -1)) {
            throw new InvalidArgumentsDescriptorException(
                    "Keyword-only arguments are only supported with Robot Framework 3.1 or newer");
        }
    }

    public Range<Integer> getPossibleNumberOfArgumentsPassedByPosition() {
        final int min = getRequiredArguments().size();
        return supportsVarargs() ? Range.atLeast(min) : Range.closed(min, min + getDefaultArguments().size());
    }

    public Range<Integer> getPossibleNumberOfArgumentsPassedByName() {
        final int min = getKeywordOnlyRequiredArguments().size();
        return supportsKwargs() ? Range.atLeast(min) : Range.closed(min, min + getKeywordOnlyDefaultArguments().size());
    }

    public Range<Integer> getPossibleNumberOfNonKwargsArguments() {
        final int min = getRequiredArguments().size();
        return supportsVarargs() ? Range.atLeast(min) : Range.closed(min, min + getDefaultArguments().size());
    }

    public Range<Integer> getPossibleNumberOfArguments() {
        final int min = getRequiredArguments().size();
        return supportsVarargs() || supportsKwargs() ? Range.atLeast(min) : Range.closed(min, arguments.size());
    }

    public boolean hasFixedNumberOfArguments() {
        // has fixed number when there are only required arguments (no defaults, no vararg, no kwarg)
        return arguments.stream().allMatch(arg -> arg.isRequired() || arg.isKeywordOnlyRequired());
    }

    public boolean supportsVarargs() {
        return getVarargArgument().isPresent();
    }

    public boolean supportsKeywordOnlyArguments() {
        return arguments.stream().filter(Argument::isKeywordOnly).findFirst().isPresent();
    }

    public boolean supportsKwargs() {
        return getKwargArgument().isPresent();
    }

    public List<Argument> getRequiredArguments() {
        return arguments.stream().filter(Argument::isRequired).collect(toList());
    }

    public List<Argument> getDefaultArguments() {
        return arguments.stream().filter(Argument::isDefault).collect(toList());
    }

    public Optional<Argument> getVarargArgument() {
        return arguments.stream().filter(Argument::isVarArg).findFirst();
    }

    public List<Argument> getKeywordOnlyRequiredArguments() {
        return arguments.stream().filter(Argument::isKeywordOnlyRequired).collect(toList());
    }

    public List<Argument> getKeywordOnlyDefaultArguments() {
        return arguments.stream().filter(Argument::isKeywordOnlyDefault).collect(toList());
    }

    public Optional<Argument> getKwargArgument() {
        return arguments.stream().filter(Argument::isKwArg).findFirst();
    }

    public String getDescription() {
        final Stream<String> descriptionsStream;
        if (lastPositional == -1) {
            descriptionsStream = arguments.stream().map(Argument::getDescription);
        } else {
            descriptionsStream = Streams.concat(arguments.stream().limit(lastPositional).map(Argument::getDescription),
                    Stream.of("*"), arguments.stream().skip(lastPositional).map(Argument::getDescription));
        }
        return descriptionsStream.collect(joining(", ", "[", "]"));
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ArgumentsDescriptor) {
            final ArgumentsDescriptor that = (ArgumentsDescriptor) obj;
            return this.arguments.equals(that.arguments) && this.lastPositional == that.lastPositional;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(arguments, lastPositional);
    }

    @Override
    public String toString() {
        return "[" + Joiner.on(", ").join(arguments) + "]";
    }

    public static class Argument {

        private final ArgumentType type;

        private final String argumentName;

        private final Optional<String> annotation;

        private final Optional<String> defaultValue;

        @VisibleForTesting
        Argument(final ArgumentType type, final String name, final String annotation, final String defaultValue) {
            this.type = type;
            this.argumentName = name;
            this.annotation = Optional.ofNullable(annotation).map(String::trim);
            this.defaultValue = Optional.ofNullable(defaultValue).map(String::trim);
        }

        public String getName() {
            return argumentName;
        }

        public boolean isRequired() {
            return type == ArgumentType.REQUIRED;
        }

        public boolean isDefault() {
            return type == ArgumentType.DEFAULT;
        }

        public boolean isVarArg() {
            return type == ArgumentType.VARARG;
        }

        public boolean isKeywordOnly() {
            return type == ArgumentType.KEYWORD_ONLY;
        }

        public boolean isKeywordOnlyRequired() {
            return isKeywordOnly() && !defaultValue.isPresent();
        }

        public boolean isKeywordOnlyDefault() {
            return isKeywordOnly() && defaultValue.isPresent();
        }

        public boolean isKwArg() {
            return type == ArgumentType.KWARG;
        }

        public Optional<String> getAnnotation() {
            return annotation;
        }

        public Optional<String> getDefaultValue() {
            return defaultValue;
        }

        public String getDescription() {
            final StringBuilder desc = new StringBuilder();
            desc.append(getPrefix());
            desc.append(argumentName);
            annotation.ifPresent(a -> desc.append(": ").append(a));
            defaultValue.ifPresent(v -> desc.append("=").append(v));

            return desc.toString();
        }

        private String getPrefix() {
            if (type == ArgumentType.VARARG) {
                return "*";
            } else if (type == ArgumentType.KWARG) {
                return "**";
            }
            return "";
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Argument) {
                final Argument that = (Argument) obj;
                return this.type == that.type && this.argumentName.equals(that.argumentName)
                        && this.annotation.equals(that.annotation)
                        && this.defaultValue.equals(that.defaultValue);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, argumentName, annotation, defaultValue);
        }

        @Override
        public String toString() {
            return getDescription();
        }
    }

    public enum ArgumentType {
        REQUIRED(1),
        DEFAULT(2),
        VARARG(3),
        KEYWORD_ONLY(4),
        KWARG(5);

        private int order;

        private ArgumentType(final int order) {
            this.order = order;
        }
    }

    public static class InvalidArgumentsDescriptorException extends RuntimeException {

        private static final long serialVersionUID = -5633858508868505276L;

        public InvalidArgumentsDescriptorException(final String message) {
            super(message);
        }
    }
}
