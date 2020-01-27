/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl.old;

import java.util.List;
import java.util.stream.Collectors;

import org.rf.ide.core.testdata.model.FilePosition;

interface IElementDeclaration {

    TextPosition getStart();

    FilePosition getStartFromFile();

    TextPosition getEnd();

    FilePosition getEndFromFile();

    void setRobotTokenPosition(FilePosition robotTokenPosition);

    FilePosition findRobotTokenPosition();

    void addElementDeclarationInside(IElementDeclaration elementToAdd);

    void removeElementDeclarationInside(int indexOfElementToRemove);

    void removeExactlyTheSameInstance(IElementDeclaration elementToRemove);

    List<ContainerElementType> getTypes();

    List<IElementDeclaration> getElementsDeclarationInside();

    void setLevelUpElement(IElementDeclaration levelUpElement);

    IElementDeclaration getLevelUpElement();

    boolean isComplex();

    default String getText() {
        return getElementsDeclarationInside().stream()
                .map(IElementDeclaration::getStart)
                .map(TextPosition::getText)
                .collect(Collectors.joining());
    }
}
