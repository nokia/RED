/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping;

import java.util.List;

import org.robotframework.ide.core.testData.model.table.executableDescriptors.TextPosition;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.ContainerElementType;


public interface IElementDeclaration {

    TextPosition getStart();


    TextPosition getEnd();


    void addElementDeclarationInside(final IElementDeclaration elementToAdd);


    List<ContainerElementType> getTypes();


    List<IElementDeclaration> getElementsDeclarationInside();


    void setLevelUpElement(final IElementDeclaration levelUpElement);


    IElementDeclaration getLevelUpElement();
}
