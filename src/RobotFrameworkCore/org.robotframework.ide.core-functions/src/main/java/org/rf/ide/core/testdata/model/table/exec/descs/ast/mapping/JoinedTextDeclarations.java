/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.table.exec.descs.TextPosition;

public class JoinedTextDeclarations extends AContainerOperation {

    private IElementDeclaration parent;

    private FilePosition robotTokenPosition;

    @Override
    public void setRobotTokenPosition(final FilePosition robotTokenPosition) {
        this.robotTokenPosition = robotTokenPosition;
    }

    private FilePosition getRobotTokenPosition() {
        return robotTokenPosition;
    }

    @Override
    public TextPosition getStart() {
        return !super.elementsDeclaredInside.isEmpty() ? super.elementsDeclaredInside.get(0).getStart() : null;
    }

    @Override
    public FilePosition getStartFromFile() {
        final FilePosition position = findRobotTokenPosition();
        return new FilePosition(position.getLine(), position.getColumn() + getStart().getStart(),
                position.getOffset() + getStart().getStart());
    }

    @Override
    public TextPosition getEnd() {
        return !super.elementsDeclaredInside.isEmpty()
                ? super.elementsDeclaredInside.get(super.elementsDeclaredInside.size() - 1).getEnd()
                : null;
    }

    @Override
    public FilePosition getEndFromFile() {
        final FilePosition position = findRobotTokenPosition();
        return new FilePosition(position.getLine(), position.getColumn() + getEnd().getEnd(),
                position.getOffset() + getEnd().getEnd());
    }

    @Override
    public FilePosition findRobotTokenPosition() {
        FilePosition position = getRobotTokenPosition();
        if (position == null) {
            for (final IElementDeclaration dec : elementsDeclaredInside) {
                position = dec.findRobotTokenPosition();
                if (position != null) {
                    break;
                }
            }
        }

        fixJoin();
        return position;
    }

    private void fixJoin() {
        int end = 0;
        final int inside = super.elementsDeclaredInside.size();
        for (int index = 0; index < inside; index++) {
            final IElementDeclaration t = super.elementsDeclaredInside.get(index);
            if (index > 0) {
                if (end != t.getStart().getStart()) {
                    super.elementsDeclaredInside.clear();
                    super.elementsDeclaredInside.add(t);
                }
            }
            end = t.getEnd().getEnd() + 1;
        }
    }

    @Override
    public void setLevelUpElement(final IElementDeclaration levelUpElement) {
        this.parent = levelUpElement;
    }

    @Override
    public IElementDeclaration getLevelUpElement() {
        return parent;
    }

    @Override
    public String getText() {
        final StringBuilder text = new StringBuilder();
        int end = 0;
        final int inside = super.elementsDeclaredInside.size();
        for (int index = 0; index < inside; index++) {
            final IElementDeclaration t = super.elementsDeclaredInside.get(index);
            if (index > 0) {
                if (end != t.getStart().getStart()) {
                    super.elementsDeclaredInside.clear();
                    super.elementsDeclaredInside.add(t);
                    return t.getStart().getFullText().substring(t.getStart().getStart(), t.getEnd().getEnd() + 1);
                }
            }
            end = t.getEnd().getEnd() + 1;
            text.append(t.getStart().getFullText().substring(t.getStart().getStart(), end));
        }

        return text.toString();
    }

    public TextPosition join() {
        final int inside = super.elementsDeclaredInside.size();
        // get text added for check if text are coherent
        final String text = getText();
        if (inside > 0 && !text.isEmpty()) {
            final String fullText = super.getElementsDeclarationInside().get(0).getStart().getFullText();
            return new TextPosition(fullText, getStart().getStart(), getEnd().getEnd());
        }
        return null;
    }

    @Override
    public boolean isComplex() {
        return false;
    }

    @Override
    public String toString() {
        return String.format("JoinedTextDeclarations [start=%s, end=%s]", getStart(), getEnd());
    }
}
