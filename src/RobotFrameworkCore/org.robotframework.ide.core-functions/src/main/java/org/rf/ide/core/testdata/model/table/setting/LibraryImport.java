/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class LibraryImport extends AImported {

    private final List<RobotToken> arguments = new ArrayList<>();

    private LibraryAlias alias = new LibraryAlias(null);

    public LibraryImport(final RobotToken libraryDeclaration) {
        super(Type.LIBRARY, libraryDeclaration);
    }

    public LibraryAlias getAlias() {
        return alias;
    }

    public void setAlias(final LibraryAlias alias) {
        alias.setParent(this);
        this.alias = alias;
    }

    public List<RobotToken> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public void addArgument(final RobotToken argument) {
        this.arguments.add(argument);
    }

    public void removeArgument(final int index) {
        this.arguments.remove(index);
    }

    @Override
    public boolean isPresent() {
        return true; // TODO: check if correct imported
    }

    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> tokens = new ArrayList<>();
        if (isPresent()) {
            tokens.add(getDeclaration());
            final RobotToken pathOrName = getPathOrName();
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
