/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping;

import org.robotframework.ide.core.testData.model.table.executableDescriptors.TextPosition;


public class JoinedTextDeclarations extends AContainerOperation {

    private IElementDeclaration parent;


    @Override
    public TextPosition getStart() {
        TextPosition pos = null;
        if (!super.elementsDeclaredInside.isEmpty()) {
            pos = super.elementsDeclaredInside.get(0).getStart();
        }

        return pos;
    }


    @Override
    public TextPosition getEnd() {
        TextPosition pos = null;
        if (!super.elementsDeclaredInside.isEmpty()) {
            pos = super.elementsDeclaredInside.get(
                    super.elementsDeclaredInside.size() - 1).getEnd();
        }

        return pos;
    }


    @Override
    public void setLevelUpElement(IElementDeclaration levelUpElement) {
        this.parent = levelUpElement;
    }


    @Override
    public IElementDeclaration getLevelUpElement() {
        return parent;
    }


    public String getText() {
        StringBuilder text = new StringBuilder();
        int end = 0;
        int inside = super.elementsDeclaredInside.size();
        for (int index = 0; index < inside; index++) {
            IElementDeclaration t = super.elementsDeclaredInside.get(index);
            if (index > 0) {
                if (end != t.getStart().getStart()) {
                    throw new IllegalStateException(
                            "No chain connection between "
                                    + super.elementsDeclaredInside.get(
                                            index - 1).getEnd() + " and "
                                    + t.getStart());
                }
            }
            end = t.getEnd().getEnd() + 1;
            text.append(t.getStart().getFullText()
                    .substring(t.getStart().getStart(), end));
        }

        return text.toString();
    }


    public TextPosition join() {
        TextPosition t = null;
        String fullText = null;
        int inside = super.elementsDeclaredInside.size();
        String text = getText();
        if (inside > 0 && !text.isEmpty()) {
            fullText = super.getElementsDeclarationInside().get(0).getStart()
                    .getFullText();
            t = new TextPosition(fullText, getStart().getStart(), getEnd()
                    .getEnd());
        }
        return t;
    }


    @Override
    public String toString() {
        return String.format("JoinedTextDeclarations [start=%s, end=%s]",
                getStart(), getEnd());
    }


    @Override
    public boolean isComplex() {
        return false;
    }
}
