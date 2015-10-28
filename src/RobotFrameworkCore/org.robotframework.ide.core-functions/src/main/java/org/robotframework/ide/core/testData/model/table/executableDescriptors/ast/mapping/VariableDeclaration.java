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


public class VariableDeclaration implements IElementDeclaration {

    private IElementDeclaration levelUpElement;
    private final List<IElementDeclaration> elementsDeclaredInside = new LinkedList<>();
    private boolean isEscaped = false;
    private final TextPosition variableStart;
    private final TextPosition variableName;
    private final TextPosition variableEnd;


    public VariableDeclaration(final TextPosition variableStart,
            final TextPosition variableName, final TextPosition variableEnd) {
        this.variableStart = variableStart;
        this.variableName = variableName;
        this.variableEnd = variableEnd;
    }


    public boolean isEscaped() {
        return isEscaped;
    }


    public void escaped() {
        this.isEscaped = true;
    }


    @Override
    public TextPosition getStart() {
        return variableStart;
    }


    public TextPosition getVariableName() {
        return variableName;
    }


    @Override
    public TextPosition getEnd() {
        return variableEnd;
    }


    @Override
    public List<IElementDeclaration> getElementsDeclarationInside() {
        return Collections.unmodifiableList(elementsDeclaredInside);
    }


    public void setLevelUpElement(final IElementDeclaration levelUpElement) {
        this.levelUpElement = levelUpElement;
    }


    @Override
    public IElementDeclaration getLevelUpElement() {
        return levelUpElement;
    }


    @Override
    public void addElementDeclarationInside(IElementDeclaration elementToAdd) {
        elementsDeclaredInside.add(elementToAdd);
    }


    @Override
    public List<ContainerElementType> getTypes() {
        List<ContainerElementType> types = new LinkedList<>();
        for (IElementDeclaration dec : elementsDeclaredInside) {
            types.addAll(dec.getTypes());
        }
        return types;
    }
}
