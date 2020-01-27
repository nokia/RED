/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.postfixes;

import java.util.List;
import java.util.stream.Stream;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.variables.descs.VariableUse;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

class SettingDescriptor<T> implements IExecutableRowDescriptor<T> {

    final AModelElement<T> line;

    SettingDescriptor(final AModelElement<T> line) {
        this.line = line;
    }

    @Override
    public boolean isCreatingVariables() {
        return false;
    }

    @Override
    public Stream<RobotToken> getCreatingVariables() {
        return Stream.empty();
    }

    @Override
    public RobotToken getAction() {
        return null;
    }

    @Override
    public RobotToken getKeywordAction() {
        return null;
    }

    @Override
    public List<VariableUse> getUsedVariables() {
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
    public RowType getRowType() {
        return RowType.SETTING;
    }

    @Override
    public AModelElement<T> getRow() {
        return line;
    }

}
