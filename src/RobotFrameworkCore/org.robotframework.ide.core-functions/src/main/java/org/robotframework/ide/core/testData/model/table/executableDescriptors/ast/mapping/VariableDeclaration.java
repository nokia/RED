/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping;

import java.util.List;

import org.robotframework.ide.core.testData.model.table.executableDescriptors.TextPosition;


public class VariableDeclaration extends AContainerOperation {

    private IElementDeclaration levelUpElement;
    private TextPosition escape;
    private TextPosition variableIdentificator;
    private final TextPosition variableStart;
    private final TextPosition variableEnd;


    public VariableDeclaration(final TextPosition variableStart,
            final TextPosition variableEnd) {
        this.variableStart = variableStart;
        this.variableEnd = variableEnd;
    }


    public boolean isEscaped() {
        return (escape != null);
    }


    public TextPosition getEscape() {
        return escape;
    }


    public void setEscape(final TextPosition escape) {
        this.escape = escape;
    }


    public TextPosition getTypeIdentficator() {
        return variableIdentificator;
    }


    public void setTypeIdentificator(final TextPosition variableIdentficator) {
        this.variableIdentificator = variableIdentficator;
    }


    @Override
    public TextPosition getStart() {
        return variableStart;
    }


    public TextPosition getVariableName() {
        TextPosition varName = null;
        if (!isDynamic()) {
            JoinedTextDeclarations nameJoined = new JoinedTextDeclarations();
            List<IElementDeclaration> elementsDeclarationInside = super
                    .getElementsDeclarationInside();
            for (IElementDeclaration elem : elementsDeclarationInside) {
                if (elem.isComplex()) {
                    break;
                } else {
                    nameJoined.addElementDeclarationInside(elem);
                }
            }

            varName = nameJoined.join();
        }

        return varName;
    }


    /**
     * check if variable depends on other variables
     * 
     * @return
     */
    public boolean isDynamic() {
        boolean result = false;
        List<IElementDeclaration> elementsDeclarationInside = super
                .getElementsDeclarationInside();
        for (IElementDeclaration iElementDeclaration : elementsDeclarationInside) {
            if (iElementDeclaration instanceof VariableDeclaration) {
                result = true;
                break;
            }
        }

        return result;
    }


    @Override
    public TextPosition getEnd() {
        return variableEnd;
    }


    public void setLevelUpElement(final IElementDeclaration levelUpElement) {
        this.levelUpElement = levelUpElement;
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
