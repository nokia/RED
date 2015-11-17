/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.TextPosition;


public class IndexDeclaration extends AContainerOperation {

    private IElementDeclaration levelUpElement;
    private FilePosition robotTokenPosition;
    private final TextPosition indexBegin;
    private final TextPosition indexEnd;


    public IndexDeclaration(final TextPosition indexBegin,
            final TextPosition indexEnd) {
        this.indexBegin = indexBegin;
        this.indexEnd = indexEnd;
    }


    public void setLevelUpElement(final IElementDeclaration levelUpElement) {
        this.levelUpElement = levelUpElement;
    }


    @Override
    public void setRobotTokenPosition(final FilePosition robotTokenPosition) {
        this.robotTokenPosition = robotTokenPosition;
    }


    private FilePosition getRobotTokenPosition() {
        return robotTokenPosition;
    }


    @Override
    public TextPosition getStart() {
        return indexBegin;
    }


    @Override
    public FilePosition getStartFromFile() {
        FilePosition position = findRobotTokenPosition();
        position = new FilePosition(position.getLine(), position.getColumn()
                + indexBegin.getStart(), position.getOffset()
                + indexBegin.getStart());
        return position;
    }


    @Override
    public TextPosition getEnd() {
        return indexEnd;
    }


    @Override
    public FilePosition getEndFromFile() {
        FilePosition position = findRobotTokenPosition();
        position = new FilePosition(position.getLine(), position.getColumn()
                + indexEnd.getEnd(), position.getOffset() + indexEnd.getEnd());
        return position;
    }


    @Override
    public FilePosition findRobotTokenPosition() {
        FilePosition position = getRobotTokenPosition();
        if (position == null) {
            position = this.levelUpElement.findRobotTokenPosition();
        }

        return position;
    }


    @Override
    public IElementDeclaration getLevelUpElement() {
        return levelUpElement;
    }


    @Override
    public boolean isComplex() {
        return true;
    }
}
