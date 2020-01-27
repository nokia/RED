/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.variables.descs.VariableUse;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class SimpleRowDescriptor<T> implements IExecutableRowDescriptor<T> {

    private final RobotExecutableRow<T> row;

    private final List<VariableUse> createdVariables = new ArrayList<>();

    private final List<VariableUse> usedVariables = new ArrayList<>();

    private RobotToken action = new RobotToken();

    private final List<RobotToken> keywordArguments = new ArrayList<>(0);

    private final List<BuildMessage> messages = new ArrayList<>();


    public SimpleRowDescriptor(final RobotExecutableRow<T> row) {
        this.row = row;
    }

    @Override
    public boolean isCreatingVariables() {
        return !createdVariables.isEmpty();
    }

    List<VariableUse> getCreatedVariables() {
        return createdVariables;
    }

    @Override
    public Stream<RobotToken> getCreatingVariables() {
        return createdVariables.stream().map(VariableUse::asToken);
    }

    void addCreatedVariables(final List<? extends VariableUse> createdVars) {
        createdVariables.addAll(createdVars);
    }

    @Override
    public RobotToken getAction() {
        return action;
    }

    void setAction(final RobotToken action) {
        this.action = action;
    }

    @Override
    public RobotToken getKeywordAction() {
        return getAction();
    }

    @Override
    public List<VariableUse> getUsedVariables() {
        return Collections.unmodifiableList(usedVariables);
    }

    void addUsedVariables(final List<? extends VariableUse> usedVars) {
        usedVariables.addAll(usedVars);
    }

    @Override
    public List<RobotToken> getKeywordArguments() {
        return Collections.unmodifiableList(keywordArguments);
    }

    void addKeywordArgument(final RobotToken argument) {
        keywordArguments.add(argument);
    }

    @Override
    public List<BuildMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    void addMessage(final BuildMessage msg) {
        messages.add(msg);
    }

    @Override
    public RowType getRowType() {
        return row.isExecutable() ? RowType.SIMPLE : RowType.COMMENTED_HASH;
    }

    @Override
    public RobotExecutableRow<T> getRow() {
        return row;
    }

    @Override
    public String toString() {
        return String.format(
                "SimpleRowDescriptor [createdVariables=%s, action=%s, rowType=%s, usedVariables=%s, messages=%s, row=%s]",
                createdVariables, action, getRowType(), usedVariables, messages, row);
    }
}
