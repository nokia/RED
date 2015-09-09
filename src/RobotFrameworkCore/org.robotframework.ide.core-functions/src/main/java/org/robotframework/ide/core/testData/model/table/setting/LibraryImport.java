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


public class LibraryImport extends AImported {

    private final List<RobotToken> arguments = new LinkedList<>();
    private LibraryAlias alias = new LibraryAlias(null);


    public LibraryImport(final RobotToken libraryDeclaration) {
        super(Type.LIBRARY, libraryDeclaration);
    }


    public LibraryAlias getAlias() {
        return alias;
    }


    public void setAlias(LibraryAlias alias) {
        this.alias = alias;
    }


    public List<RobotToken> getArguments() {
        return Collections.unmodifiableList(arguments);
    }


    public void addArgument(final RobotToken argument) {
        this.arguments.add(argument);
    }


    @Override
    public boolean isPresent() {
        return true; // TODO: check if correct imported
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
            tokens.addAll(getAlias().getElementTokens());
            tokens.addAll(getComment());
        }

        return tokens;
    }
}
