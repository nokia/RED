/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

public class RobotCollectionElement {

    private int index;

    private String key;

    private String value;

    public RobotCollectionElement(final int index, final String key, final String value) {
        this.index = index;
        this.key = key;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public void incrementIndex() {
        this.index++;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }
}
