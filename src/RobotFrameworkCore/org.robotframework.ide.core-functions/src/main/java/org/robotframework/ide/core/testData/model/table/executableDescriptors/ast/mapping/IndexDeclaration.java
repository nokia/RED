/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping;

import org.robotframework.ide.core.testData.model.table.executableDescriptors.TextPosition;


public class IndexDeclaration extends AContainerOperation {

    private IElementDeclaration levelUpElement;

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
    public TextPosition getStart() {
        return indexBegin;
    }


    @Override
    public TextPosition getEnd() {
        return indexEnd;
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
