/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.setting;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class ResourceImport extends AImported {

    private final List<RobotToken> unexpectedTrashArguments = new LinkedList<>();


    public ResourceImport(final RobotToken resourceDeclaration) {
        super(Type.RESOURCE, resourceDeclaration);
    }


    public List<RobotToken> getUnexpectedTrashArguments() {
        return Collections.unmodifiableList(unexpectedTrashArguments);
    }


    public void addUnexpectedTrashArgument(final RobotToken trashArgument) {
        this.unexpectedTrashArguments.add(trashArgument);
    }


    @Override
    public boolean isPresent() {
        return (getDeclaration() != null);
    }


    @Override
    public List<RobotToken> getElementTokens() {
        List<RobotToken> tokens = new LinkedList<>();
        if (isPresent()) {
            tokens.add(getDeclaration());
            RobotToken pathOrName = getPathOrName();
            if (pathOrName != null) {
                tokens.add(pathOrName);
            }
            tokens.addAll(getUnexpectedTrashArguments());
            tokens.addAll(getComment());
        }

        return tokens;
    }
}
