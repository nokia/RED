/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.recognizer;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;


public abstract class AExecutableElementSettingsRecognizer extends
        ATokenRecognizer {

    private final static Pattern BRACKET_EXTRACTION = Pattern
            .compile("(?!\\[)([^\\]])+(?!\\\\])");


    protected AExecutableElementSettingsRecognizer(RobotTokenType type) {
        super(build(buildVariants(type)), type);
    }


    @VisibleForTesting
    protected static List<String> buildVariants(final RobotTokenType type) {
        List<String> variants = new LinkedList<>();

        List<String> representations = type.getRepresentation();
        for (String r : representations) {
            Matcher matcher = BRACKET_EXTRACTION.matcher(r);
            String toAdd = r;
            if (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();

                if (start == 1 && end == r.length() - 1) {
                    toAdd = r.substring(start, end);
                }
            }

            variants.add(toAdd);
        }

        return variants;
    }


    @VisibleForTesting
    protected static Pattern build(final List<String> settingNameVariants) {
        StringBuilder patternText = new StringBuilder();
        int numOfVariants = settingNameVariants.size();
        if (numOfVariants > 0) {
            patternText.append("[ ]?(");
            for (int i = 0; i < numOfVariants; i++) {
                patternText.append("(\\[\\s*");
                patternText.append(createUpperLowerCaseWord(settingNameVariants
                        .get(i)));
                patternText.append("\\s*\\])");
                if (i < numOfVariants - 1) {
                    patternText.append('|');
                }
            }

            patternText.append(')');
        }

        return Pattern.compile(patternText.toString());
    }
}
