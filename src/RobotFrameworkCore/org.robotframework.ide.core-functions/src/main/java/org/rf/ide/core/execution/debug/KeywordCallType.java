/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Set;

public enum KeywordCallType {

    SETUP,
    TEARDOWN,
    NORMAL_CALL,
    FOR,
    FOR_ITERATION;

    private static final Set<String> SETUPS = newHashSet("suite setup", "test setup", "setup");

    private static final Set<String> TEARDOWNS = newHashSet("suite teardown", "test teardown", "teardown");

    private static final Set<String> FORS = newHashSet("suite for", "test for", "for");

    private static final Set<String> FOR_ITEMS = newHashSet("suite foritem", "test foritem", "for item");

    public static KeywordCallType from(final String keywordType) {
        if (keywordType.equalsIgnoreCase("keyword")) {
            return NORMAL_CALL;

        } else if (SETUPS.contains(keywordType.toLowerCase())) {
            return SETUP;

        } else if (TEARDOWNS.contains(keywordType.toLowerCase())) {
            return TEARDOWN;

        } else if (FORS.contains(keywordType.toLowerCase())) {
            return FOR;

        } else if (FOR_ITEMS.contains(keywordType.toLowerCase())) {
            return FOR_ITERATION;

        } else {
            throw new IllegalStateException("Unrecognized keyword type: " + keywordType);
        }
    }
}
