/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors;

public class TextPosition {

    private final String text;
    private final int startPosition;
    private final int endPosition;


    public TextPosition(final String text, final int startPosition,
            final int endPosition) {
        this.text = text;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }


    public String getText() {
        return text;
    }


    public int getStart() {
        return startPosition;
    }


    public int getEnd() {
        return endPosition;
    }


    @Override
    public String toString() {
        return String.format("TextPosition [text=%s, start=%s, end=%s]", text,
                startPosition, endPosition);
    }
}
