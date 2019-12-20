/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

public class GlobalVariable<T> {

    private final String name;

    private final T value;

    public GlobalVariable(final String name, final T value) {
        this.value = value;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format(this.getClass() + " [name=%s, value=%s]", name, value);
    }
}
