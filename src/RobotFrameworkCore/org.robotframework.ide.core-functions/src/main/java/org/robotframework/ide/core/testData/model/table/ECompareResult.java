/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table;

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
        ECompareResult mapped = ECompareResult.COMPARE_NOT_SET;
        for (ECompareResult ecr : values()) {
            if (ecr.getValue() == numberResult) {
                mapped = ecr;
                break;
            }
        }

        return mapped;
    }
}