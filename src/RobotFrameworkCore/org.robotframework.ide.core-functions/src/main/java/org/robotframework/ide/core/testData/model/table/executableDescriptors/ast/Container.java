/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.ast;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class Container implements IContainerElement {

    private Container parent;
    private List<IContainerElement> elements = new LinkedList<>();
    private boolean isOpenForModification = true;


    public Container(final Container parent) {
        this.parent = parent;
    }


    public Container getParent() {
        return parent;
    }


    public void addElement(final IContainerElement element) {
        if (isOpenForModification()) {
            elements.add(element);
        } else {
            throw new UnsupportedOperationException(
                    "Container is closed for modification!");
        }
    }


    public void closeForModification() {
        this.isOpenForModification = false;
    }


    public boolean isOpenForModification() {
        return isOpenForModification;
    }


    public List<IContainerElement> getElements() {
        return Collections.unmodifiableList(elements);
    }


    @Override
    public boolean isComplex() {
        return true;
    }


    @Override
    public ContainerElementType getType() {
        return null;
    }


    @Override
    public String toString() {
        return String
                .format("Container [hasParent=%s, elements=%s, isOpenForModification=%s]",
                        (parent != null), elements, isOpenForModification);
    }


    @Override
    public String prettyPrint(int deepLevel) {
        StringBuilder text = new StringBuilder();
        text.append(formatWithSpaces(deepLevel, String.format(
                "Container [hasParent=%s, isOpenForModification=%s",
                (parent != null), isOpenForModification)));
        int childDeepLevel = deepLevel + 1;
        for (IContainerElement elem : elements) {
            text.append("\n");
            text.append(elem.prettyPrint(childDeepLevel));
            text.append(",");
        }
        text.append("\n");
        text.append(formatWithSpaces(deepLevel, "]"));
        return text.toString();
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


    public ContainerType getContainerType() {
        ContainerType type = ContainerType.MIX;
        if (!getElements().isEmpty()) {
            IContainerElement element = getElements().get(0);
            if (element.getType() == ContainerElementType.CURRLY_BRACKET_OPEN) {
                type = ContainerType.VARIABLE;
            } else if (element.getType() == ContainerElementType.SQUARE_BRACKET_OPEN) {
                type = ContainerType.INDEX;
            }
        }

        return type;
    }

    public enum ContainerType {
        MIX(null), VARIABLE(ContainerElementType.CURRLY_BRACKET_OPEN), INDEX(
                ContainerElementType.SQUARE_BRACKET_OPEN);

        private ContainerElementType openType;


        private ContainerType(final ContainerElementType openType) {
            this.openType = openType;
        }


        public ContainerElementType getOpenType() {
            return openType;
        }
    }
}
