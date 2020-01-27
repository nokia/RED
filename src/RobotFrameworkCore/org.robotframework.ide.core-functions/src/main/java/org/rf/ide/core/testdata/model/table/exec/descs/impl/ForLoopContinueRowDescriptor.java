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

public class ForLoopContinueRowDescriptor<T> implements IExecutableRowDescriptor<T> {

    private final RobotExecutableRow<T> row;

    private RobotToken continueAction = new RobotToken();

    private final List<VariableUse> createdVariables = new ArrayList<>();

    private final List<VariableUse> usedVariables = new ArrayList<>();

    private RobotToken keywordAction = new RobotToken();

    private final List<RobotToken> keywordArguments = new ArrayList<>(0);

    private int forLoopStartRowIndex = -1;

    private final List<BuildMessage> messages = new ArrayList<>();


    public ForLoopContinueRowDescriptor(final RobotExecutableRow<T> row) {
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

    void addCreatedVariables(final List<VariableUse> variables) {
        createdVariables.addAll(variables);
    }

    @Override
    public RobotToken getAction() {
        return continueAction;
    }

    int getForLoopStartRowIndex() {
        return forLoopStartRowIndex;
    }

    void setForLoopStartRowIndex(final int forLoopStartRowIndex) {
        this.forLoopStartRowIndex = forLoopStartRowIndex;
    }

    void setAction(final RobotToken continueAction) {
        this.continueAction = continueAction;
    }

    @Override
    public RobotToken getKeywordAction() {
        return keywordAction;
    }

    void setKeywordAction(final RobotToken keywordAction) {
        this.keywordAction = keywordAction;
    }

    @Override
    public List<VariableUse> getUsedVariables() {
        return Collections.unmodifiableList(usedVariables);
    }

    void addUsedVariables(final List<VariableUse> variables) {
        usedVariables.addAll(variables);
    }

    @Override
    public List<RobotToken> getKeywordArguments() {
        return Collections.unmodifiableList(keywordArguments);
    }

    void addKeywordArguments(final List<RobotToken> arguments) {
        keywordArguments.addAll(arguments);
    }

    @Override
    public List<BuildMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    void addMessages(final List<BuildMessage> msgs) {
        messages.addAll(msgs);
    }

    @Override
    public RowType getRowType() {
        return RowType.FOR_CONTINUE;
    }

    @Override
    public RobotExecutableRow<T> getRow() {
        return row;
    }
}
