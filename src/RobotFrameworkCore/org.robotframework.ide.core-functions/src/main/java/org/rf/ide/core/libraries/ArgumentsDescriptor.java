/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.libraries;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.rf.ide.core.libraries.ArgumentsDescriptor.Argument;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

/**
 * @author Michal Anglart
 *
 */
public class ArgumentsDescriptor implements Iterable<Argument> {

    private final List<Argument> arguments;

    private ArgumentsDescriptor(final List<Argument> args) {
        this.arguments = args;
    }

    public static ArgumentsDescriptor createDescriptor(final String... args) {
        return createDescriptor(newArrayList(args));
    }

    public static ArgumentsDescriptor createDescriptor(final List<String> args) {
        if (args == null) {
            return new ArgumentsDescriptor(Lists.<Argument> newArrayList());
        }

        final List<Argument> arguments = newArrayList();
        for (final String arg : args) {
            if (arg.contains("=")) {
                final List<String> splitted = Splitter.on("=").splitToList(arg);
                arguments.add(new Argument(ArgumentType.DEFAULT, splitted.get(0), splitted.get(1)));
            } else if (arg.startsWith("**")) {
                arguments.add(new Argument(ArgumentType.KWARG, arg.substring(2)));
            } else if (arg.startsWith("*")) {
                arguments.add(new Argument(ArgumentType.VARARG, arg.substring(1)));
            } else {
                arguments.add(new Argument(ArgumentType.REQUIRED, arg));
            }
        }
        return new ArgumentsDescriptor(arguments);
    }

    @Override
    public Iterator<Argument> iterator() {
        return arguments.iterator();
    }

    public int size() {
        return arguments.size();
    }

    public Argument get(final int index) {
        return arguments.get(index);
    }

    public Optional<Argument> getKwargArgument() {
        for (int i = arguments.size() - 1; i >= 0; i--) {
            if (arguments.get(i).isKwArg()) {
                return Optional.of(arguments.get(i));
            }
        }
        return Optional.empty();
    }

    public boolean supportsKwargs() {
        return getKwargArgument().isPresent();
    }

    public List<Argument> getRequiredArguments() {
        final List<Argument> required = new ArrayList<>();
        for (final Argument argument : arguments) {
            if (argument.isRequired()) {
                required.add(argument);
            }
        }
        return required;
    }


    public Range<Integer> getPossibleNumberOfArguments() {
        int min = 0;
        boolean isUnbounded = false;
        for (final Argument argument : arguments) {
            if (argument.isRequired()) {
                min++;
            }
            isUnbounded |= argument.isVarArg() || argument.isKwArg();
        }
        return isUnbounded ? Range.atLeast(min) : Range.closed(min, arguments.size());
    }

    public String getDescription() {
        final Iterable<String> args = Iterables.transform(arguments, Argument::getDescription);
        return "[" + Joiner.on(", ").join(args) + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ArgumentsDescriptor) {
            final ArgumentsDescriptor that = (ArgumentsDescriptor) obj;
            return this.arguments.equals(that.arguments);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return arguments.hashCode();
    }

    @Override
    public String toString() {
        return "[" + Joiner.on(", ").join(arguments) + "]";
    }

    public static class Argument {

        private final ArgumentType type;
        
        private final String argumentName;

        private final Optional<String> defaultValue;

        public Argument(final ArgumentType type, final String arg) {
            this(type, arg, null);
        }

        public Argument(final ArgumentType type, final String arg, final String defaultValue) {
            this.type = type;
            this.argumentName = arg;
            this.defaultValue = Optional.ofNullable(defaultValue);
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

        public boolean isKwArg() {
            return type == ArgumentType.KWARG;
        }

        public String getDescription() {
            return getPrefix() + (defaultValue.isPresent() ? argumentName + "=" + defaultValue.get() : argumentName);
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
                        && this.defaultValue.equals(that.defaultValue);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, argumentName, defaultValue);
        }

        @Override
        public String toString() {
            return getDescription();
        }
    }

    public enum ArgumentType {
        REQUIRED,
        DEFAULT,
        VARARG,
        KWARG
    }
}
