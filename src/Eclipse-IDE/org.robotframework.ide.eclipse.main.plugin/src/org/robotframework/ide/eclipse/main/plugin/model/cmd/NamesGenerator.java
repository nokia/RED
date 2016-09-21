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

    public static String generateUniqueName(final RobotElement parent, final String prefix) {
        return generateUniqueName(parent, prefix, true);
    }

    public static String generateUniqueName(final RobotElement parent, final String prefix,
            final boolean includeSpace) {
        final String prefixWithoutNumber = removeNumberSuffix(prefix);
        final int maxNumber = getCurrentMaxNumber(parent, prefixWithoutNumber);
        if (maxNumber < 0) {
            return prefix;
        } else if (includeSpace) {
            return prefixWithoutNumber + " " + (maxNumber + 1);
        } else {
            return prefixWithoutNumber + (maxNumber + 1);
        }
    }

    private static String removeNumberSuffix(final String name) {
        final StringBuilder number = new StringBuilder();
        for (int i = name.length() - 1; i >= 0; i--) {
            if (Character.isDigit(name.charAt(i))) {
                number.append(name.charAt(i));
            } else {
                break;
            }
        }
        return name.substring(0, name.length() - number.length()).trim();
    }

    private static int getCurrentMaxNumber(final RobotElement parent, final String prefix) {
        final Collection<String> currentNames = Collections2.transform(
                newArrayList(Iterables.filter(parent.getChildren(), RobotElement.class)),
                new Function<RobotElement, String>() {
                    @Override
                    public String apply(final RobotElement element) {
                        return element.getName().toLowerCase();
                    }
                });
        final Collection<Integer> numbers = Collections2.transform(currentNames, new Function<String, Integer>() {
            @Override
            public Integer apply(final String name) {
                if (name.startsWith(prefix.toLowerCase())) {
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
