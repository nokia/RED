/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl.old;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Container implements IContainerElement {

    private final Container parent;

    private final List<IContainerElement> elements = new ArrayList<>();

    private boolean isOpenForModification = true;

    Container(final Container parent) {
        this.parent = parent;
    }

    Container getParent() {
        return parent;
    }

    void addElement(final IContainerElement element) {
        if (isOpenForModification()) {
            elements.add(element);
        } else {
            throw new UnsupportedOperationException("Container is closed for modification!");
        }
    }

    void closeForModification() {
        this.isOpenForModification = false;
    }

    boolean isOpenForModification() {
        return isOpenForModification;
    }

    List<IContainerElement> getElements() {
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
        return String.format("Container [hasParent=%s, elements=%s, isOpenForModification=%s]", (parent != null),
                elements, isOpenForModification);
    }

    @Override
    public String prettyPrint(final int deepLevel) {
        final StringBuilder text = new StringBuilder();
        text.append(formatWithSpaces(deepLevel, String.format("Container [hasParent=%s, isOpenForModification=%s",
                (parent != null), isOpenForModification)));
        final int childDeepLevel = deepLevel + 1;
        for (final IContainerElement elem : elements) {
            text.append("\n");
            text.append(elem.prettyPrint(childDeepLevel));
            text.append(",");
        }
        text.append("\n");
        text.append(formatWithSpaces(deepLevel, "]"));
        return text.toString();
    }

    private String formatWithSpaces(final int deepLevel, final String text) {
        String result;
        if (deepLevel > 0) {
            result = String.format("%" + deepLevel + "s%s", " ", text);
        } else {
            result = String.format("%s", text);
        }

        return result;
    }

    ContainerType getContainerType() {
        ContainerType type = ContainerType.MIX;
        if (!getElements().isEmpty()) {
            final IContainerElement element = getElements().get(0);
            if (element.getType() == ContainerElementType.CURLY_BRACKET_OPEN) {
                type = ContainerType.VARIABLE;
            } else if (element.getType() == ContainerElementType.SQUARE_BRACKET_OPEN) {
                type = ContainerType.INDEX;
            }
        }

        return type;
    }

    enum ContainerType {
        MIX(null),
        VARIABLE(ContainerElementType.CURLY_BRACKET_OPEN),
        INDEX(ContainerElementType.SQUARE_BRACKET_OPEN);

        private ContainerElementType openType;

        private ContainerType(final ContainerElementType openType) {
            this.openType = openType;
        }

        ContainerElementType getOpenType() {
            return openType;
        }
    }

    @Override
    public TextPosition getPosition() {
        return elements.get(0).getPosition();
    }
}
