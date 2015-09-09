/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.setting;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class VariablesImport extends AImported {

    private final List<RobotToken> arguments = new LinkedList<>();


    public VariablesImport(final RobotToken variablesDeclaration) {
        super(Type.VARIABLES, variablesDeclaration);
    }


    public List<RobotToken> getArguments() {
        return Collections.unmodifiableList(arguments);
    }


    public void addArgument(final RobotToken argument) {
        this.arguments.add(argument);
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
            tokens.addAll(getArguments());
            tokens.addAll(getComment());
        }

        return tokens;
    }
}
