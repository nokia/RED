/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors;

import java.util.List;

import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping.IElementDeclaration;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


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


    public RobotToken getToken() {
        return originalToken;
    }
}
