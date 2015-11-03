/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping;

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
        return null;
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
