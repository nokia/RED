/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class LibraryImport extends AImported implements Serializable {

    private static final long serialVersionUID = 1L;

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

    public LibraryAlias newAlias() {
        RobotToken dec = new RobotToken();
        dec.setText(RobotTokenType.SETTING_LIBRARY_ALIAS
                .getTheMostCorrectOneRepresentation(getParent().getParent().getParent().getRobotVersion())
                .getRepresentation());

        LibraryAlias libAlias = new LibraryAlias(dec);
        setAlias(libAlias);

        return libAlias;
    }

    public List<RobotToken> getArguments() {
        return Collections.unmodifiableList(arguments);
    }
    
    public void setArguments(final int index, final String argument) {
        updateOrCreateTokenInside(arguments, index, argument, RobotTokenType.SETTING_LIBRARY_ARGUMENT);
    }
    
    public void setArguments(final int index, final RobotToken argument) {
        updateOrCreateTokenInside(arguments, index, argument, RobotTokenType.SETTING_LIBRARY_ARGUMENT);
    }
    
    public void addArgument(final String argument) {
        RobotToken rt = new RobotToken();
        rt.setText(argument);

        addArgument(rt);
    }

    public void addArgument(final RobotToken argument) {
        fixForTheType(argument, RobotTokenType.SETTING_LIBRARY_ARGUMENT);
        this.arguments.add(argument);
    }

    public void removeArgument(final int index) {
        this.arguments.remove(index);
    }

    @Override
    public boolean isPresent() {
        return true;
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
