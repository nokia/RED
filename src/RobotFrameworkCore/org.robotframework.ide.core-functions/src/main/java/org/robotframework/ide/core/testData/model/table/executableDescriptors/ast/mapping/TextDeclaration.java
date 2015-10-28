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


public class TextDeclaration implements IElementDeclaration {

    private IElementDeclaration levelUpElement;
    private final List<IElementDeclaration> elementsDeclaredInside = new LinkedList<>();
    private TextPosition text;


    public TextDeclaration(final TextPosition text) {
        this.text = text;
    }


    public void setLevelUpElement(final IElementDeclaration levelUpElement) {
        this.levelUpElement = levelUpElement;
    }


    @Override
    public TextPosition getStart() {
        return text;
    }


    @Override
    public TextPosition getEnd() {
        return text;
    }


    @Override
    public List<IElementDeclaration> getElementsDeclarationInside() {
        return Collections.unmodifiableList(elementsDeclaredInside);
    }


    @Override
    public IElementDeclaration getLevelUpElement() {
        return levelUpElement;
    }


    @Override
    public void addElementsDeclarationInside(IElementDeclaration elementToAdd) {
        throw new UnsupportedOperationException(
                "Adding elements to TEXT declaration is not allowed please use container class for it.");
    }
}
