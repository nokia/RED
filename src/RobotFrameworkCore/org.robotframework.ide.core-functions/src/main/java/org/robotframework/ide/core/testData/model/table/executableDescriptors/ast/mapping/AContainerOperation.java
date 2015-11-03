/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.ContainerElementType;


public abstract class AContainerOperation implements IElementDeclaration {

    protected final List<IElementDeclaration> elementsDeclaredInside = new LinkedList<>();


    @Override
    public void addElementDeclarationInside(IElementDeclaration elementToAdd) {
        elementsDeclaredInside.add(elementToAdd);
    }


    @Override
    public void removeElementDeclarationInside(final int indexOfElementToRemove) {
        elementsDeclaredInside.remove(indexOfElementToRemove);
    }


    @Override
    public void removeExactlyTheSameInstance(
            final IElementDeclaration elementToRemove) {
        for (int i = 0; i < elementsDeclaredInside.size(); i++) {
            IElementDeclaration d = elementsDeclaredInside.get(i);
            if (d == elementToRemove) {
                elementsDeclaredInside.remove(i);
                i--;
            }
        }
    }


    @Override
    public List<ContainerElementType> getTypes() {
        List<ContainerElementType> types = new LinkedList<>();
        for (IElementDeclaration dec : elementsDeclaredInside) {
            types.addAll(dec.getTypes());
        }
        return types;
    }


    @Override
    public List<IElementDeclaration> getElementsDeclarationInside() {
        return Collections.unmodifiableList(elementsDeclaredInside);
    }
}
