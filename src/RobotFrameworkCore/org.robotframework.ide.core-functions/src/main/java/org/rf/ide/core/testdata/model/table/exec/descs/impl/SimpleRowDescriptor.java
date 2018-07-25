/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.RobotAction;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.IElementDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class SimpleRowDescriptor<T> implements IExecutableRowDescriptor<T> {

    private final List<VariableDeclaration> createdVariables = new ArrayList<>();

    private RobotAction action = new RobotAction(new RobotToken(), new ArrayList<IElementDeclaration>());

    private final List<VariableDeclaration> commentedVariables = new ArrayList<>();

    private final List<VariableDeclaration> usedVariables = new ArrayList<>();

    private final List<IElementDeclaration> textParameters = new ArrayList<>();

    private final List<RobotToken> keywordArguments = new ArrayList<>(0);

    private final IRowType type = ERowType.UNKONWN;

    private final List<BuildMessage> messages = new ArrayList<>();

    private final RobotExecutableRow<T> row;

    public SimpleRowDescriptor(final RobotExecutableRow<T> row) {
        this.row = row;
    }

    @Override
    public List<VariableDeclaration> getCreatedVariables() {
        return Collections.unmodifiableList(createdVariables);
    }

    public void addCreatedVariable(final VariableDeclaration variable) {
        this.createdVariables.add(variable);
    }

    @Override
    public RobotAction getAction() {
        return action;
    }

    public void setAction(final RobotAction action) {
        this.action = action;
    }

    @Override
    public RobotAction getKeywordAction() {
        return getAction();
    }

    public void moveCreatedVariablesToUsedVariables() {
        usedVariables.addAll(createdVariables);
        createdVariables.clear();
    }

    @Override
    public List<VariableDeclaration> getUsedVariables() {
        return Collections.unmodifiableList(usedVariables);
    }

    public void addUsedVariable(final VariableDeclaration usedVar) {
        usedVariables.add(usedVar);
    }

    public void addUsedVariables(final List<VariableDeclaration> usedVars) {
        for (final VariableDeclaration usedVar : usedVars) {
            addUsedVariable(usedVar);
        }
    }

    @Override
    public List<VariableDeclaration> getCommentedVariables() {
        return Collections.unmodifiableList(commentedVariables);
    }

    public void addCommentedVariable(final VariableDeclaration variable) {
        this.commentedVariables.add(variable);
    }

    public void addCommentedVariables(final List<VariableDeclaration> commentedVars) {
        for (final VariableDeclaration var : commentedVars) {
            addCommentedVariable(var);
        }
    }

    @Override
    public List<IElementDeclaration> getTextParameters() {
        return Collections.unmodifiableList(textParameters);
    }

    public void addTextParameter(final IElementDeclaration text) {
        textParameters.add(text);
    }

    public void addTextParameters(final List<IElementDeclaration> texts) {
        for (final IElementDeclaration text : texts) {
            addTextParameter(text);
        }
    }

    @Override
    public List<RobotToken> getKeywordArguments() {
        return Collections.unmodifiableList(keywordArguments);
    }

    public void addKeywordArgument(final RobotToken argument) {
        keywordArguments.add(argument);
    }

    public void addKeywordArguments(final List<RobotToken> arguments) {
        for (final RobotToken keywordArgument : arguments) {
            addKeywordArgument(keywordArgument);
        }
    }

    @Override
    public List<BuildMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public void addMessages(final List<BuildMessage> msgs) {
        for (final BuildMessage bm : msgs) {
            addMessage(bm);
        }
    }

    public void addMessage(final BuildMessage bm) {
        messages.add(bm);
    }

    @Override
    public IRowType getRowType() {
        IRowType correctType = type;
        if (correctType == ERowType.UNKONWN) {
            if (row.isExecutable()) {
                correctType = ERowType.SIMPLE;
            } else {
                correctType = ERowType.COMMENTED_HASH;
            }
        }

        return correctType;
    }

    @Override
    public RobotExecutableRow<T> getRow() {
        return row;
    }

    @Override
    public String toString() {
        return String.format(
                "SimpleRowDescriptor [createdVariables=%s, action=%s, rowType=%s, usedVariables=%s, textParameters=%s, messages=%s, row=%s]",
                createdVariables, action, type, usedVariables, textParameters, messages, row);
    }
}
