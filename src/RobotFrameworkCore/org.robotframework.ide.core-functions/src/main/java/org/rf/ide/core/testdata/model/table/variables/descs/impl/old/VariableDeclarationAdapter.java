/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl.old;

import java.util.Set;

import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.descs.VariableUse;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


class VariableDeclarationAdapter implements VariableUse {

    private final VariableDeclaration declaration;

    private final boolean isPlain;

    private final boolean isPlainAssign;

    VariableDeclarationAdapter(final VariableDeclaration declaration, final boolean isPlain,
            final boolean isPlainAssign) {
        this.declaration = declaration;
        this.isPlain = isPlain;
        this.isPlainAssign = isPlainAssign;
    }

    @Override
    public VariableType getType() {
        return declaration.getType();
    }

    @Override
    public String getName() {
        return declaration.getName();
    }

    @Override
    public RobotToken asToken() {
        return declaration.asToken();
    }

    @Override
    public FileRegion getRegion() {
        return declaration.getRegion();
    }

    @Override
    public boolean isDefinedIn(final Set<String> variableDefinitions) {
        return declaration.isDefinedIn(variableDefinitions);
    }

    @Override
    public boolean isDynamic() {
        return declaration.isDynamic();
    }

    @Override
    public boolean isIndexed() {
        return declaration.isIndexed();
    }

    @Override
    public boolean isPlainVariable() {
        return isPlain;
    }

    @Override
    public boolean isPlainVariableAssign() {
        return isPlainAssign;
    }
}
