/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.stream.Stream;

public enum ECompareResult {
    LESS_THAN(-1), EQUAL_TO(0), GREATER_THAN(1), COMPARE_NOT_SET(-2);

    private int value;


    private ECompareResult(final int value) {
        this.value = value;
    }


    public int getValue() {
        return value;
    }

    public static ECompareResult map(final int numberResult) {
        return Stream.of(values())
                .filter(ecr -> ecr.getValue() == numberResult)
                .findFirst()
                .orElse(ECompareResult.COMPARE_NOT_SET);
    }
}