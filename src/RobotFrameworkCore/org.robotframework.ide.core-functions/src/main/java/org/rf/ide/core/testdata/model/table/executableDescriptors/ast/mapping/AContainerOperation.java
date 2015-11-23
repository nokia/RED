/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.executableDescriptors.ast.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.table.executableDescriptors.ast.ContainerElementType;


public abstract class AContainerOperation implements IElementDeclaration {

    protected final List<IElementDeclaration> elementsDeclaredInside = new ArrayList<>();


    @Override
    public void addElementDeclarationInside(final IElementDeclaration elementToAdd) {
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
            final IElementDeclaration d = elementsDeclaredInside.get(i);
            if (d == elementToRemove) {
                elementsDeclaredInside.remove(i);
                i--;
            }
        }
    }


    @Override
    public List<ContainerElementType> getTypes() {
        final List<ContainerElementType> types = new ArrayList<>();
        for (final IElementDeclaration dec : elementsDeclaredInside) {
            types.addAll(dec.getTypes());
        }
        return types;
    }


    @Override
    public List<IElementDeclaration> getElementsDeclarationInside() {
        return Collections.unmodifiableList(elementsDeclaredInside);
    }
}
