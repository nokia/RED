/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping;

import java.util.List;

import org.robotframework.ide.core.testData.model.table.executableDescriptors.TextPosition;


public interface IElementDeclaration {

    TextPosition getStart();


    TextPosition getEnd();


    void addElementsDeclarationInside(final IElementDeclaration elementToAdd);


    List<IElementDeclaration> getElementsDeclarationInside();


    void setLevelUpElement(final IElementDeclaration levelUpElement);


    IElementDeclaration getLevelUpElement();
}
