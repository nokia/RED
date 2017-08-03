/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords.names;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Range;

public class CamelCaseKeywordNamesSupport {

    private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("[A-Z][a-z]*");

    /**
     * @return List of camel case parts from input, or empty if input is not in camel case format or
     *         contains characters other than letters
     */
    static List<String> toCamelCaseParts(final String input) {
        final List<String> results = new ArrayList<>();
        final Matcher matcher = CAMEL_CASE_PATTERN.matcher(input);
        while (matcher.find()) {
            results.add(matcher.group());
        }
        if (String.join("", results).equals(input)) {
            return results;
        }
        return new ArrayList<>();
    }

    /**
     * @return List of ranges in definition which match with given occurrence, or empty if no match
     *         or occurrenceName is not in camel case format
     */
    public static List<Range<Integer>> matches(final String definitionName, final String occurrenceName) {
        final List<Range<Integer>> ranges = new ArrayList<>();
        final List<String> parts = toCamelCaseParts(occurrenceName);
        if (!parts.isEmpty()) {
            final String regex = "^(" + String.join(")\\w*\\W+(", parts) + ").*$";
            final Pattern pattern = Pattern.compile(regex);
            final Matcher matcher = pattern.matcher(definitionName);
            if (matcher.matches()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    ranges.add(Range.closedOpen(matcher.start(i), matcher.end(i)));
                }
            }
        }
        return ranges;
    }

}
