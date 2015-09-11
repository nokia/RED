/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.setting;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class LibraryAlias extends AModelElement<LibraryImport> {

    private final RobotToken libraryAliasDeclaration;
    private RobotToken libraryAlias;


    public LibraryAlias(final RobotToken aliasDeclaration) {
        this.libraryAliasDeclaration = aliasDeclaration;
    }


    public RobotToken getLibraryAlias() {
        return libraryAlias;
    }


    public void setLibraryAlias(RobotToken libraryAlias) {
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
        List<RobotToken> tokens = new LinkedList<>();
        if (isPresent()) {
            tokens.add(getLibraryAliasDeclaration());
            if (getLibraryAlias() != null) {
                tokens.add(getLibraryAlias());
            }
        }

        return tokens;
    }
}
