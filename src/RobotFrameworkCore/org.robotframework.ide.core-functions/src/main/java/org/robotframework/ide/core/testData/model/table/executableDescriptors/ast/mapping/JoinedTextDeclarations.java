/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.executableDescriptors.TextPosition;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.ContainerElementType;


public class JoinedTextDeclarations implements IElementDeclaration {

    private IElementDeclaration parent;
    private final List<IElementDeclaration> textInside = new LinkedList<>();


    @Override
    public TextPosition getStart() {
        TextPosition pos = null;
        if (!textInside.isEmpty()) {
            pos = textInside.get(0).getStart();
        }

        return pos;
    }


    @Override
    public TextPosition getEnd() {
        TextPosition pos = null;
        if (!textInside.isEmpty()) {
            pos = textInside.get(textInside.size() - 1).getEnd();
        }

        return pos;
    }


    @Override
    public void addElementDeclarationInside(IElementDeclaration elementToAdd) {
        textInside.add(elementToAdd);
    }


    @Override
    public List<IElementDeclaration> getElementsDeclarationInside() {
        return Collections.unmodifiableList(textInside);
    }


    @Override
    public void setLevelUpElement(IElementDeclaration levelUpElement) {
        this.parent = levelUpElement;
    }


    @Override
    public IElementDeclaration getLevelUpElement() {
        return parent;
    }


    @Override
    public List<ContainerElementType> getTypes() {
        List<ContainerElementType> types = new LinkedList<>();
        for (IElementDeclaration dec : textInside) {
            types.addAll(dec.getTypes());
        }
        return types;
    }


    @Override
    public String toString() {
        return String.format("Joined [start=%s, end=%s]", getStart(), getEnd());
    }
}
