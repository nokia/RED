/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.Collections;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;

public class NamesGenerator {

    public static String generateUniqueName(final RobotElement parent, final String prefix,
            final boolean includeSpace) {
        final int maxNumber = getCurrentMaxNumber(parent, prefix);
        if (maxNumber < 0) {
            return prefix;
        } else if (includeSpace) {
            return prefix + " " + (maxNumber + 1);
        } else {
            return prefix + (maxNumber + 1);
        }
    }

    public static String generateUniqueName(final RobotElement parent, final String prefix) {
        return generateUniqueName(parent, prefix, true);
    }

    private static int getCurrentMaxNumber(final RobotElement parent, final String prefix) {
        final Collection<String> currentNames = Collections2.transform(
                newArrayList(Iterables.filter(parent.getChildren(), RobotElement.class)),
                new Function<RobotElement, String>() {
                    @Override
                    public String apply(final RobotElement element) {
                        return element.getName();
                    }
                });
        final Collection<Integer> numbers = Collections2.transform(currentNames, new Function<String, Integer>() {
            @Override
            public Integer apply(final String name) {
                if (name.startsWith(prefix)) {
                    final String number = name.substring(prefix.length()).trim();
                    final Integer num = Ints.tryParse(number);
                    return num == null ? 0 : num;
                }
                return -1;
            }
        });
        return numbers.isEmpty() ? -1 : Collections.max(numbers);
    }
}
