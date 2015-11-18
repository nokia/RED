/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFileOutput.BuildMessage;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.IExecutableRowDescriptor;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.RobotAction;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping.IElementDeclaration;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping.VariableDeclaration;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class SimpleRowDescriptor<T> implements IExecutableRowDescriptor<T> {

    private final List<VariableDeclaration> createdVariables = new LinkedList<>();
    private RobotAction action = new RobotAction(new RobotToken(),
            new LinkedList<IElementDeclaration>());;
    private final List<VariableDeclaration> usedVariables = new LinkedList<>();
    private final List<IElementDeclaration> textParameters = new LinkedList<>();
    private IRowType type = ERowType.UNKONWN;
    private final List<BuildMessage> messages = new LinkedList<>();
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
    public List<VariableDeclaration> getUsedVariables() {
        return Collections.unmodifiableList(usedVariables);
    }


    public void addUsedVariable(final VariableDeclaration usedVar) {
        usedVariables.add(usedVar);
    }


    public void addUsedVariables(final List<VariableDeclaration> usedVars) {
        for (VariableDeclaration usedVar : usedVars) {
            addUsedVariable(usedVar);
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
        for (IElementDeclaration text : texts) {
            addTextParameter(text);
        }
    }


    @Override
    public List<BuildMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }


    public void addMessages(final List<BuildMessage> msgs) {
        for (BuildMessage bm : msgs) {
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
        return String
                .format("SimpleRowDescriptor [createdVariables=%s, action=%s, rowType=%s, usedVariables=%s, textParameters=%s, messages=%s, row=%s]",
                        createdVariables, action, type, usedVariables,
                        textParameters, messages, row);
    }
}
