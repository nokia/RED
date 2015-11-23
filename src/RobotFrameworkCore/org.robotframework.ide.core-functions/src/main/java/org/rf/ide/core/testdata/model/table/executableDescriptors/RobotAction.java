/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.executableDescriptors;

import java.util.List;

import org.rf.ide.core.testdata.model.table.executableDescriptors.ast.mapping.IElementDeclaration;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class RobotAction {

    private final RobotToken originalToken;
    private final List<IElementDeclaration> elementsOfToken;


    public RobotAction(final RobotToken originalToken,
            final List<IElementDeclaration> elementsOfToken) {
        this.originalToken = originalToken;
        this.elementsOfToken = elementsOfToken;
    }


    public List<IElementDeclaration> getLineElements() {
        return elementsOfToken;
    }


    public boolean isPresent() {
        return (originalToken != null && !originalToken.getFilePosition()
                .isNotSet())
                && (elementsOfToken != null && !elementsOfToken.isEmpty());
    }


    public RobotToken getToken() {
        RobotToken tokenToReturn = originalToken;
        if (tokenToReturn == null) {
            tokenToReturn = new RobotToken();
        }
        return tokenToReturn;
    }
}
