/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.IElementDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class SettingDescriptor<T> implements IExecutableRowDescriptor<T>{
    
    final AModelElement<T> line;

    public SettingDescriptor(final AModelElement<T> line) {
        this.line = line;
    }

    @Override
    public List<VariableDeclaration> getCreatedVariables() {
        return null;
    }

    @Override
    public RobotAction getAction() {
        return null;
    }

    @Override
    public RobotAction getKeywordAction() {
        return null;
    }

    @Override
    public List<VariableDeclaration> getUsedVariables() {
        return null;
    }

    @Override
    public List<VariableDeclaration> getCommentedVariables() {
        return null;
    }

    @Override
    public List<IElementDeclaration> getTextParameters() {
        return null;
    }

    @Override
    public List<BuildMessage> getMessages() {
        return null;
    }

    @Override
    public List<RobotToken> getKeywordArguments() {
        return null;
    }

    @Override
    public IRowType getRowType() {
        return ERowType.SETTING;
    }

    @Override
    public AModelElement<T> getRow() {
        return line;
    }

    
}
