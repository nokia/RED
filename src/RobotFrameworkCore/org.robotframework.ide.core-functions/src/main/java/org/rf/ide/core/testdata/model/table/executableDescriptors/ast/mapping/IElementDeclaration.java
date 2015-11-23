/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.executableDescriptors.ast.mapping;

import java.util.List;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.table.executableDescriptors.TextPosition;
import org.rf.ide.core.testdata.model.table.executableDescriptors.ast.ContainerElementType;


public interface IElementDeclaration {

    TextPosition getStart();


    FilePosition getStartFromFile();


    TextPosition getEnd();


    FilePosition getEndFromFile();


    void setRobotTokenPosition(final FilePosition robotTokenPosition);


    FilePosition findRobotTokenPosition();


    void addElementDeclarationInside(final IElementDeclaration elementToAdd);


    void removeElementDeclarationInside(final int indexOfElementToRemove);


    void removeExactlyTheSameInstance(final IElementDeclaration elementToRemove);


    List<ContainerElementType> getTypes();


    List<IElementDeclaration> getElementsDeclarationInside();


    void setLevelUpElement(final IElementDeclaration levelUpElement);


    IElementDeclaration getLevelUpElement();


    boolean isComplex();
}
