/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.table.exec.descs.TextPosition;

public class IndexDeclaration extends AContainerOperation {

    private IElementDeclaration levelUpElement;

    private FilePosition robotTokenPosition;

    private final TextPosition indexStart;

    private final TextPosition indexEnd;

    public IndexDeclaration(final TextPosition indexStart, final TextPosition indexEnd) {
        this.indexStart = indexStart;
        this.indexEnd = indexEnd;
    }

    @Override
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
        return indexStart;
    }

    @Override
    public FilePosition getStartFromFile() {
        final FilePosition position = findRobotTokenPosition();
        return new FilePosition(position.getLine(), position.getColumn() + indexStart.getStart(),
                position.getOffset() + indexStart.getStart());
    }

    @Override
    public TextPosition getEnd() {
        return indexEnd;
    }

    @Override
    public FilePosition getEndFromFile() {
        final FilePosition position = findRobotTokenPosition();
        return new FilePosition(position.getLine(), position.getColumn() + indexEnd.getEnd(),
                position.getOffset() + indexEnd.getEnd());
    }

    @Override
    public FilePosition findRobotTokenPosition() {
        final FilePosition position = getRobotTokenPosition();
        return position != null ? position : levelUpElement.findRobotTokenPosition();
    }

    @Override
    public IElementDeclaration getLevelUpElement() {
        return levelUpElement;
    }

    @Override
    public boolean isComplex() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("IndexDeclaration [start=%s, end=%s]", getStart(), getEnd());
    }
}
