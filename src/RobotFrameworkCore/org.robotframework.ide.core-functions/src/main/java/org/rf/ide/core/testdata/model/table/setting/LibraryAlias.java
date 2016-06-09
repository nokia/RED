/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class LibraryAlias extends AModelElement<LibraryImport> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final RobotToken libraryAliasDeclaration;

    private RobotToken libraryAlias;

    public LibraryAlias(final RobotToken aliasDeclaration) {
        fixForTheType(aliasDeclaration, RobotTokenType.SETTING_LIBRARY_ALIAS, true);
        this.libraryAliasDeclaration = aliasDeclaration;
    }

    public RobotToken getLibraryAlias() {
        return libraryAlias;
    }

    public void setLibraryAlias(final RobotToken libraryAlias) {
        fixForTheType(libraryAlias, RobotTokenType.SETTING_LIBRARY_ALIAS_VALUE, true);
        this.libraryAlias = libraryAlias;
    }

    public RobotToken getLibraryAliasDeclaration() {
        return libraryAliasDeclaration;
    }

    @Override
    public boolean isPresent() {
        return (libraryAliasDeclaration != null);
    }

    @Override
    public ModelType getModelType() {
        return ModelType.LIBRARY_IMPORT_ALIAS_SETTING;
    }

    @Override
    public FilePosition getBeginPosition() {
        return getLibraryAliasDeclaration().getFilePosition();
    }

    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> tokens = new ArrayList<>();
        if (isPresent()) {
            tokens.add(getLibraryAliasDeclaration());
            if (getLibraryAlias() != null) {
                tokens.add(getLibraryAlias());
            }
        }

        return tokens;
    }

    @Override
    public RobotToken getDeclaration() {
        return libraryAliasDeclaration;
    }
}
