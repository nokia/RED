/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;

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
        final List<Integer> numbers = parent.getChildren()
                .stream()
                .map(toLowerCaseNames())
                .map(byExtractingNumbersFromSuffixes(prefix))
                .collect(Collectors.toList());
        return numbers.isEmpty() ? -1 : Collections.max(numbers);
    }

    private static Function<String, Integer> byExtractingNumbersFromSuffixes(final String prefix) {
        return name -> {
            if (name.startsWith(prefix.toLowerCase())) {
                final String numberInSuffix = name.substring(prefix.length()).trim();
                final Integer num = Ints.tryParse(numberInSuffix);
                return num == null ? 0 : num;
            }
            return -1;
        };
    }

    private static Function<RobotElement, String> toLowerCaseNames() {
        return element -> element.getName().toLowerCase();
    }
}
