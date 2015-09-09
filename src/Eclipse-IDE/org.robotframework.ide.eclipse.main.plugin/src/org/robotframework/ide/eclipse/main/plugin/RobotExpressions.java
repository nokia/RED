/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

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

    private static Character lookahead(final String expression, final int index) {
        return index < expression.length() ? expression.charAt(index) : null;
    }
}
