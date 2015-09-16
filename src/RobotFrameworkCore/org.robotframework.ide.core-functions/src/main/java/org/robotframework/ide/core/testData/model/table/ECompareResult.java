/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table;

public enum ECompareResult {
    LESS(-1), EQUAL(0), GREATER(1);

    private int value;


    private ECompareResult(final int value) {
        this.value = value;
    }


    public int getValue() {
        return value;
    }
}