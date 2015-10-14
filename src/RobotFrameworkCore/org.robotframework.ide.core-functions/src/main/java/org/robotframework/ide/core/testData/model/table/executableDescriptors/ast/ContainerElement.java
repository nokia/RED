/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.ast;

import org.robotframework.ide.core.testData.model.table.executableDescriptors.TextPosition;


public class ContainerElement implements IContainerElement {

    private TextPosition position;
    private ContainerElementType type;


    public ContainerElement(final TextPosition position,
            final ContainerElementType type) {
        this.position = position;
        this.type = type;
    }


    public TextPosition getPosition() {
        return position;
    }


    public void increaseEndPosition() {
        position = new TextPosition(position.getText(), position.getStart(),
                position.getEnd() + 1);
    }


    @Override
    public boolean isComplex() {
        return false;
    }


    @Override
    public ContainerElementType getType() {
        return type;
    }


    @Override
    public String toString() {
        return String.format("ContainerElement [type=%s, position=%s]", type,
                position);
    }


    @Override
    public String prettyPrint(int deepLevel) {
        return formatWithSpaces(deepLevel, this.toString());
    }


    private String formatWithSpaces(int deepLevel, String text) {
        String result;
        if (deepLevel > 0) {
            result = String.format("%" + deepLevel + "s%s", " ", text);
        } else {
            result = String.format("%s", text);
        }

        return result;
    }
}
