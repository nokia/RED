/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.test.helpers;

import java.util.ArrayList;
import java.util.List;


/**
 * It is generator of possible combinations. Currently supports only text.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class CombinationGenerator {

    public List<String> combinations(final String text) {
        final List<String> combinations = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return combinations;
        }

        final String originalLowerCase = text.toLowerCase();

        final long numberOfLowerCaseLetters = originalLowerCase.chars().filter(Character::isLowerCase).count();
        if (numberOfLowerCaseLetters == 0) {
            combinations.add(originalLowerCase);
        } else {
            for (int i = 0; i < Math.pow(2, numberOfLowerCaseLetters); i++) {
                combinations.add(applyAsBitMask(originalLowerCase, i));
            }
        }
        return combinations;
    }

    private String applyAsBitMask(final String originalLowerCase, final int mask) {
        final StringBuilder result = new StringBuilder();

        int currentMask = mask;
        for (int i = 0; i < originalLowerCase.length(); i++) {
            final char ch = originalLowerCase.charAt(i);
            if (Character.isLowerCase(ch)) {
                if (currentMask % 2 == 1) {
                    result.append(Character.toUpperCase(ch));
                } else {
                    result.append(ch);
                }
                currentMask >>= 1;
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }
}
