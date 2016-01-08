/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;

import com.google.common.base.Function;
import com.google.common.collect.Range;

public class RobotExpressions {

    public static List<Range<Integer>> getVariablesPositions(final String expression) {
        final List<Range<Integer>> ranges = newArrayList();

        int rangeStart = -1;
        boolean inVariable = false;
        for (int i = 0; i < expression.length(); i++) {
            if (!inVariable && "%$@&".contains(Character.toString(expression.charAt(i)))
                    && Character.valueOf('{').equals(lookahead(expression, i + 1))) {
                inVariable = true;
                rangeStart = i;
            } else if (inVariable && expression.charAt(i) == '}') {
                inVariable = false;
                ranges.add(Range.closed(rangeStart, i));
            }
        }
        return ranges;
    }

    public static List<String> getVariables(final String expression) {
        final List<Range<Integer>> positions = getVariablesPositions(expression);
        return newArrayList(transform(positions, new Function<Range<Integer>, String>() {

            @Override
            public String apply(final Range<Integer> range) {
                return expression.substring(range.lowerEndpoint(), range.upperEndpoint() + 1);
            }
        }));
    }

    public static String resolve(final Map<String, String> knownVariables, final String expression) {
        final List<Range<Integer>> positions = getVariablesPositions(expression);
        Collections.sort(positions, new Comparator<Range<Integer>>() {
            @Override
            public int compare(final Range<Integer> o1, final Range<Integer> o2) {
                return o2.lowerEndpoint().compareTo(o1.lowerEndpoint());
            }
        });
        
        final StringBuilder resolved = new StringBuilder(expression);
        for (final Range<Integer> position : positions) {
            final String variable = VariableNamesSupport.extractUnifiedVariableName(expression.substring(
                    position.lowerEndpoint(), position.upperEndpoint() + 1));
            if (knownVariables.containsKey(variable)) {
                resolved.replace(position.lowerEndpoint(), position.upperEndpoint() + 1, knownVariables.get(variable));
            }
        }
        return resolved.toString();
    }
    
    public static boolean isParameterized(final String pathOrName) {
        return Pattern.compile("[@$&%]\\{[^\\}]+\\}").matcher(pathOrName).find();
    }

    private static Character lookahead(final String expression, final int index) {
        return index < expression.length() ? expression.charAt(index) : null;
    }
}
