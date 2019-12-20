/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AExecutableElementSettingsRecognizer extends ATokenRecognizer {

    private static final Pattern BRACKET_EXTRACTION = Pattern.compile("(?!\\[)([^\\]])+(?!\\\\])");

    protected AExecutableElementSettingsRecognizer(final RobotTokenType type) {
        super(build(buildVariants(type)), type);
    }

    private static List<String> buildVariants(final RobotTokenType type) {
        final List<String> variants = new ArrayList<>();

        final List<String> representations = type.getRepresentation();
        for (final String r : representations) {
            final Matcher matcher = BRACKET_EXTRACTION.matcher(r);
            String toAdd = r;
            if (matcher.find()) {
                final int start = matcher.start();
                final int end = matcher.end();

                if (start == 1 && end == r.length() - 1) {
                    toAdd = r.substring(start, end);
                }
            }
            variants.add(toAdd);
        }
        return variants;
    }

    private static Pattern build(final List<String> settingNameVariants) {
        final StringBuilder patternText = new StringBuilder();
        if (!settingNameVariants.isEmpty()) {
            patternText.append("[ ]?(");
            patternText.append(settingNameVariants.stream()
                    .map(variant -> "(\\[\\s*" + createUpperLowerCaseWord(variant) + "\\s*\\])")
                    .collect(joining("|")));
            patternText.append(')');
        }
        return Pattern.compile(patternText.toString());
    }
}
