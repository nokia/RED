/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.library;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.table.keywords.KeywordArguments;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

/**
 * @author Michal Anglart
 *
 */
public class ArgumentsDescriptor {

    private final List<Argument> arguments;

    private ArgumentsDescriptor(final List<Argument> args) {
        this.arguments = args;
    }

    public static ArgumentsDescriptor createDescriptor(final UserKeyword userKeyword) {
        final List<KeywordArguments> arguments = userKeyword.getArguments();
        if (arguments == null) {
            return null;
        }
        final List<Argument> args = new ArrayList<>(2);
        for (final RobotToken argumentToken : arguments.get(arguments.size() - 1).getArguments()) {
            
        }
        return new ArgumentsDescriptor(args);
    }

    static ArgumentsDescriptor createDescriptor(final List<String> args) {
        if (args == null) {
            return new ArgumentsDescriptor(Lists.<Argument> newArrayList());
        }

        final List<Argument> arguments = newArrayList();
        for (final String arg : args) {
            if (arg.contains("=")) {
                final List<String> splitted = Splitter.on("=").splitToList(arg);
                arguments.add(new Argument(splitted.get(0), splitted.get(1)));
            } else {
                arguments.add(new Argument(arg));
            }
        }
        return new ArgumentsDescriptor(arguments);
    }

    public Range<Integer> getPossibleNumberOfArguments() {
        int min = 0;
        boolean isUnbounded = false;
        for (final Argument argument : arguments) {
            if (!argument.isOptional() && !argument.isVarArg() && !argument.isKwArg()) {
                min++;
            }
            isUnbounded |= argument.isVarArg() || argument.isKwArg();
        }
        if (isUnbounded) {
            return Range.atLeast(min);
        } else {
            return Range.closed(min, arguments.size());
        }
    }

    public String getDescription() {
        final Iterable<String> args = Iterables.transform(arguments, new Function<Argument, String>() {

            @Override
            public String apply(final Argument arg) {
                return arg.getDescription();
            }
        });
        return "[" + Joiner.on(", ").join(args) + "]";
    }

    public static class Argument {

        private final String argumentName;

        private final Optional<String> defaultValue;

        public Argument(final String arg) {
            this.argumentName = arg;
            this.defaultValue = Optional.absent();
        }

        public Argument(final String arg, final String defaultValue) {
            this.argumentName = arg;
            this.defaultValue = Optional.of(defaultValue);
        }

        public String getName() {
            return argumentName;
        }

        public boolean isOptional() {
            return defaultValue.isPresent();
        }

        public boolean isVarArg() {
            return argumentName.startsWith("*") && !argumentName.startsWith("**");
        }

        public boolean isKwArg() {
            return argumentName.startsWith("**");
        }

        public String getDescription() {
            return defaultValue.isPresent() ? argumentName + "=" + defaultValue.get() : argumentName;
        }
    }
}
