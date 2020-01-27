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

public class ForLoopDeclarationRowDescriptor<T> implements IExecutableRowDescriptor<T> {

    private final RobotExecutableRow<T> row;

    private RobotToken forAction = new RobotToken();

    private final List<VariableUse> createdVariables = new ArrayList<>();

    private RobotToken inAction = new RobotToken();

    private final List<VariableUse> usedVariables = new ArrayList<>();

    private final List<BuildMessage> messages = new ArrayList<>();


    public ForLoopDeclarationRowDescriptor(final RobotExecutableRow<T> row) {
        this.row = row;
    }

    @Override
    public boolean isCreatingVariables() {
        return !createdVariables.isEmpty();
    }

    @Override
    public Stream<RobotToken> getCreatingVariables() {
        return createdVariables.stream().map(VariableUse::asToken);
    }

    void addCreatedVariables(final List<? extends VariableUse> variables) {
        createdVariables.addAll(variables);
    }

    @Override
    public RobotToken getAction() {
        return forAction;
    }

    void setAction(final RobotToken forAction) {
        this.forAction = forAction;
    }

    @Override
    public RobotToken getKeywordAction() {
        return getAction();
    }

    public RobotToken getInAction() {
        return inAction;
    }

    void setInAction(final RobotToken inAction) {
        this.inAction = inAction;
    }

    @Override
    public List<VariableUse> getUsedVariables() {
        return Collections.unmodifiableList(usedVariables);
    }

    void addUsedVariables(final List<? extends VariableUse> variables) {
        usedVariables.addAll(variables);
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
        return RowType.FOR;
    }

    @Override
    public RobotExecutableRow<T> getRow() {
        return row;
    }

    @Override
    public List<RobotToken> getKeywordArguments() {
        return Collections.unmodifiableList(new ArrayList<>(0));
    }
}
